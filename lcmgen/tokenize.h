#ifndef _TOKENIZE_H
#define _TOKENIZE_H

#include <stdio.h>

typedef struct tokenize tokenize_t;

/** Tokenizer incrementally tokenizes an input stream. **/
struct tokenize
{
	// current token
	char *token;
	int token_line, token_column;

    // info about the last returned character from next_char.
    int current_char;
    int current_line, current_column;

    // If there is an ungetc() pending, unget_char >0 and contains the
    // char. unget_line and unget_column are the line and column of
    // the unget'd char.
    int unget_char;
    int unget_line, unget_column;

	// the current line, and our position in the input stream.
    // (ignoring the occurence of ungets.)
	char *buffer;
    int buffer_line, buffer_column;
    int buffer_len;

	char *path;
	FILE *f;

	// do we have a token ready?
	int hasnext;
};

tokenize_t *tokenize_create(const char *path);
void tokenize_destroy(tokenize_t *t);
int tokenize_next(tokenize_t *t);
int tokenize_peek(tokenize_t *t);

#endif
