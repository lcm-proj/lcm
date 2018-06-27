#include <assert.h>
#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

#include "tokenize.h"

static const char single_char_toks[] = "();\",:\'[]";  // no '.' so that name spaces are one token
static const char op_chars[] = "!~<>=&|^%*+=";

#define MAX_LINE_LEN 1024

#define TOK_ERR_MEMORY_INSUFFICIENT -2

static char unescape(char c)
{
    switch (c) {
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
    tokenize_t *t = (tokenize_t *) calloc(1, sizeof(tokenize_t));
    t->path = strdup(path);
    t->f = fopen(path, "r");

    if (t->f == NULL) {
        free(t->path);
        free(t);
        return NULL;
    }

    t->token_capacity = 1024;
    t->token = (char *) calloc(1, t->token_capacity);
    t->buffer = (char *) calloc(1, MAX_LINE_LEN);

    // Line and column indices for the last returned token
    t->token_line = -1;
    t->token_column = -1;

    t->hasnext = 0;

    t->unget_char = -1;  // no currently saved char.

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
        t->unget_char = -1;  // mark the unget as consumed.

        return t->current_char;
    }

    // otherwise, get a character from the input stream

    // read the next line if we're out of data
    if (t->buffer_column == t->buffer_len) {
        // reminder: fgets will store the newline in the buffer.
        if (fgets(t->buffer, MAX_LINE_LEN, t->f) == NULL)
            return EOF;
        t->buffer_len = (int) strlen(t->buffer);
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
    } while (c != EOF && c != '\n');
}

static int ensure_token_capacity(tokenize_t *t, int pos)
{
    if (pos >= t->token_capacity) {
        t->token_capacity *= 2;
        t->token = (char *) realloc(t->token, t->token_capacity);
        if (!t->token) {
            return 0;
        }
    }
    return 1;
}

static int add_char_to_token(tokenize_t *t, int pos, char c)
{
    if (!ensure_token_capacity(t, pos)) {
        return 0;
    }
    t->token[pos] = c;
    return 1;
}

int tokenize_extended_comment(tokenize_t *t)
{
    int pos = 0;

    // So far, the tokenizer has processed "/*"
    int comment_finished = 0;

    while (!comment_finished) {
        int pos_line_start = pos;

        // Go through leading whitespace.
        int c;
        while (1) {
            c = tokenize_next_char(t);
            if (c != EOF && (c == ' ' || c == '\t')) {
                if (!add_char_to_token(t, pos, c)) {
                    return TOK_ERR_MEMORY_INSUFFICIENT;
                }
                pos++;
            } else {
                break;
            }
        }

        // Go through asterisks
        int got_asterisk = 0;
        while (c == '*') {
            if (!add_char_to_token(t, pos, c)) {
                return TOK_ERR_MEMORY_INSUFFICIENT;
            }
            pos++;
            got_asterisk = 1;
            c = tokenize_next_char(t);
        }

        // Strip out leading comment characters in the line.
        if (got_asterisk) {
            pos = pos_line_start;
            if (c == '/') {
                // End of comment?
                comment_finished = 1;
                break;
            } else if (c == ' ') {
                // If a space immediately followed the leading asterisks, then
                // skip it.
                c = tokenize_next_char(t);
            }
        }

        // The rest of the line is comment content.
        while (!comment_finished && c != EOF && c != '\n') {
            int last_c = c;

            if (!add_char_to_token(t, pos, c)) {
                return TOK_ERR_MEMORY_INSUFFICIENT;
            }
            pos++;
            c = tokenize_next_char(t);

            if (last_c == '*' && c == '/') {
                comment_finished = 1;
                pos--;
            }
        }

        if (!comment_finished) {
            if (c == EOF) {
                printf("%s : EOF reached while parsing comment\n", t->path);
                return EOF;
            }

            assert(c == '\n');
            if (pos_line_start != pos) {
                if (!add_char_to_token(t, pos, c)) {
                    return TOK_ERR_MEMORY_INSUFFICIENT;
                }
                pos++;
            }
        }
    }

    t->token[pos] = 0;
    t->token_type = LCM_TOK_COMMENT;

    return pos;
}

/** chunkify tokens. **/
int tokenize_next_internal(tokenize_t *t)
{
    int c;
    int pos = 0;  // output char pos

    t->token_type = LCM_TOK_INVALID;

    // Repeatedly read characters until EOF or a non-whitespace character is
    // reached.
    do {
        c = tokenize_next_char(t);

        if (c == EOF) {
            t->token_type = LCM_TOK_EOF;
            return EOF;
        }
    } while (isspace(c));

    // a token is starting. mark its position.
    t->token_line = t->current_line;
    t->token_column = t->current_column;

    // is a character literal?
    if (c == '\'') {
        t->token[pos++] = c;
        c = tokenize_next_char(t);
        if (c == '\\')
            c = unescape(tokenize_next_char(t));
        if (c == EOF)
            return -4;
        t->token[pos++] = c;
        c = tokenize_next_char(t);
        if (c != '\'')
            return -5;
        t->token[pos++] = c;
        t->token_type = LCM_TOK_OTHER;
        goto end_tok;
    }

    // is a string literal?
    if (c == '\"') {
        int escape = 0;

        // add the initial quote
        t->token[pos++] = c;

        // keep reading until close quote
        while (1) {
            if (!ensure_token_capacity(t, pos)) {
                return TOK_ERR_MEMORY_INSUFFICIENT;
            }

            c = tokenize_next_char(t);
            if (c == EOF)
                goto end_tok;

            if (escape) {
                escape = 0;
                c = unescape(c);

                continue;
            }

            if (c == '\"') {
                t->token[pos++] = c;
                goto end_tok;
            }
            if (c == '\\') {
                escape = 1;
                continue;
            }

            t->token[pos++] = c;
        }
        t->token_type = LCM_TOK_OTHER;
        goto end_tok;
    }

    // is an operator?
    if (strchr(op_chars, c) != NULL) {
        while (strchr(op_chars, c) != NULL) {
            if (!ensure_token_capacity(t, pos)) {
                return TOK_ERR_MEMORY_INSUFFICIENT;
            }
            t->token[pos++] = c;
            c = tokenize_next_char(t);
        }
        t->token_type = LCM_TOK_OTHER;
        tokenize_ungetc(t, c);
        goto end_tok;
    }

    // Is a comment?
    if (c == '/') {
        if (!ensure_token_capacity(t, pos)) {
            return TOK_ERR_MEMORY_INSUFFICIENT;
        }
        t->token[pos++] = c;

        c = tokenize_next_char(t);
        if (c == EOF) {
            t->token_type = LCM_TOK_OTHER;
            goto end_tok;
        }

        // Extended comment '/* ... */'
        if (c == '*') {
            return tokenize_extended_comment(t);
        }

        // Single-line comment
        if (c == '/') {
            t->token_type = LCM_TOK_COMMENT;
            c = tokenize_next_char(t);

            // Strip out leading '/' characters
            while (c == '/') {
                c = tokenize_next_char(t);
            }

            // Strip out leading whitespace.
            while (c != EOF && c == ' ') {
                c = tokenize_next_char(t);
            }

            pos = 0;

            // Place the rest of the line into a comment token.
            while (c != EOF && c != '\n') {
                if (!ensure_token_capacity(t, pos)) {
                    return TOK_ERR_MEMORY_INSUFFICIENT;
                }
                t->token[pos++] = c;
                c = tokenize_next_char(t);
            };
            tokenize_ungetc(t, c);
            goto end_tok;
        }

        // If the '/' is not followed by a '*' or a '/', then treat it like an
        // operator
        t->token_type = LCM_TOK_OTHER;
        tokenize_ungetc(t, c);
        goto end_tok;
    }

    // otherwise, all tokens are alpha-numeric blobs
    do {
        if (!ensure_token_capacity(t, pos)) {
            return TOK_ERR_MEMORY_INSUFFICIENT;
        }

        t->token[pos++] = c;

        t->token_type = LCM_TOK_OTHER;

        if (strchr(single_char_toks, c) != NULL)
            goto end_tok;

        c = tokenize_next_char(t);
        if (strchr(single_char_toks, c) != NULL || strchr(op_chars, c) != NULL) {
            tokenize_ungetc(t, c);
            goto end_tok;
        }

    } while (!isspace(c) && c != EOF);

end_tok:
    t->token[pos] = 0;

    return pos;
}

int tokenize_next(tokenize_t *t)
{
    int res;

    if (t->hasnext) {
        t->hasnext = 0;
        return 0;
    }

    res = tokenize_next_internal(t);

    if (res == EOF)
        return EOF;

    return res;
}

int tokenize_peek(tokenize_t *t)
{
    int res = 0;

    if (t->hasnext)
        return res;

    res = tokenize_next(t);
    if (res != EOF)
        t->hasnext = 1;

    return res;
}
