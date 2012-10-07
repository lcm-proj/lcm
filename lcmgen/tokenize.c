#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <ctype.h>
#include <assert.h>

#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

#include "tokenize.h"

static const char single_char_toks[] = "();\",:\'[]"; // no '.' so that name spaces are one token
static const char op_chars[] = "!~<>=&|^%*+=/";

#define MAX_TOKEN_LEN 1024
#define MAX_LINE_LEN  1024

static char unescape(char c)
{
    switch (c)
    {
    case 'n':
        return 10;
    case 'r':
        return 13;
    case 't':
        return 9;
    default:
        return c;
    }
}

tokenize_t *tokenize_create(const char *path)
{
    tokenize_t *t = (tokenize_t*) calloc(1, sizeof(tokenize_t));
    t->path = strdup(path);
    t->f = fopen(path, "r");

    if (t->f == NULL) {
        free(t->path);
        free(t);
        return NULL;
    }

    t->token = (char*) calloc(1, MAX_TOKEN_LEN);
    t->buffer = (char*) calloc(1, MAX_LINE_LEN);

    // Line and column indices for the last returned token
    t->token_line = -1;
    t->token_column = -1;

    t->hasnext = 0;

    t->unget_char = -1; // no currently saved char.

    return t;
}

void tokenize_destroy(tokenize_t *t)
{
    free(t->buffer);
    free(t->token);

    fclose(t->f);
    free(t->path);
    free(t);
}

/** get the next character, counting line numbers, and columns. **/
int tokenize_next_char(tokenize_t *t)
{
    // return an unget() char if one is stored
    if (t->unget_char >= 0) {

        t->current_char = t->unget_char;
        t->current_line = t->unget_line;
        t->current_column = t->unget_column;
        t->unget_char = -1; // mark the unget as consumed.

        return t->current_char;
    }

    // otherwise, get a character from the input stream

    // read the next line if we're out of data
    if (t->buffer_column == t->buffer_len) {
        // reminder: fgets will store the newline in the buffer.
        if (fgets(t->buffer, MAX_LINE_LEN, t->f)==NULL)
            return EOF;
        t->buffer_len = strlen(t->buffer);
        t->buffer_line++;
        t->buffer_column = 0;
    }

    t->current_char = t->buffer[t->buffer_column];
    t->current_line = t->buffer_line;
    t->current_column = t->buffer_column;

    t->buffer_column++;

    return t->current_char;
}

int tokenize_ungetc(tokenize_t *t, int c)
{
    t->unget_char = t->current_char;
    t->unget_column = t->current_column;
    t->unget_line = t->current_line;

    return 0;
}

void tokenize_flush_line(tokenize_t *t)
{
    int c;
    do {
        c = tokenize_next_char(t);
    } while (c!=EOF && c!='\n');
}

/** chunkify tokens. **/
int tokenize_next_internal(tokenize_t *t)
{
    int c;
    int pos = 0; // output char pos

skip_white:
    c = tokenize_next_char(t);

    if (c == EOF)
        return EOF;

    if (isspace(c))
        goto skip_white;

    // a token is starting. mark its position.
    t->token_line = t->current_line;
    t->token_column = t->current_column;

    // is a character literal?
    if (c=='\'') {
        t->token[pos++] = c;
        c = tokenize_next_char(t);
        if (c=='\\')
            c = unescape(tokenize_next_char(t));
        if (c == EOF)
            return -4;
        t->token[pos++] = c;
        c = tokenize_next_char(t);
        if (c!='\'')
            return -5;
        t->token[pos++] = c;
        goto end_tok;
    }

    // is a string literal?
    if (c=='\"') {
        int escape = 0;

        // add the initial quote
        t->token[pos++] = c;

        // keep reading until close quote
        while (1) {
            if (pos >= MAX_TOKEN_LEN)
                return -2;

            c = tokenize_next_char(t);

            if (c == EOF)
                goto end_tok;

            if (escape) {
                escape = 0;
                c = unescape(c);

                continue;
            }

            if (c=='\"') {
                t->token[pos++] = c;
                goto end_tok;
            }
            if (c=='\\') {
                escape = 1;
                continue;
            }

            t->token[pos++] = c;
        }
        goto end_tok;
    }

    // is an operator?
    if (strchr(op_chars, c)!=NULL) {
        while (strchr(op_chars, c)!=NULL) {
            if (pos >= MAX_TOKEN_LEN)
                return -2;
            t->token[pos++] = c;
            c = tokenize_next_char(t);
        }
        tokenize_ungetc(t, c);
        goto end_tok;
    }

    // otherwise, all tokens are alpha-numeric blobs
in_tok:
    if (pos >= MAX_TOKEN_LEN)
        return -2;

    t->token[pos++] = c;

    if (strchr(single_char_toks,c)!=NULL)
        goto end_tok;

    c = tokenize_next_char(t);
    if (strchr(single_char_toks,c)!=NULL ||
        strchr(op_chars,c)!=NULL) {
        tokenize_ungetc(t, c);
        goto end_tok;
    }

    if (!isspace(c) && c != EOF)
        goto in_tok;

end_tok:
    t->token[pos] = 0;

    return pos;
}

/** remove comments **/
int tokenize_next(tokenize_t *t)
{
    int res;

    if (t->hasnext) {
        t->hasnext = 0;
        return 0;
    }

restart:
    res = tokenize_next_internal(t);

    if (res == EOF)
        return EOF;

    // block comment?
    if (!strncmp(t->token,"/*",2)) {
        while (1) {
            res = tokenize_next_internal(t);
            if (res == EOF)
                return -10;
            int len = strlen(t->token);
            if (len >= 2 && !strncmp(&t->token[len-2],"*/", 2))
                break;
        }
        goto restart;
    }

    // end of line comment? If so, instantly consume the rest of this line.
    if (!strncmp(t->token,"//", 2)) {
        tokenize_flush_line(t);
        goto restart;
    }

    return res;
}

int tokenize_peek(tokenize_t *t)
{
    int res = 0;

    if (t->hasnext)
        return res;

    res = tokenize_next(t);
    t->hasnext = 1;

    return res;
}
