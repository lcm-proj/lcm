#ifndef _TOKENIZE_H
#define _TOKENIZE_H

#include <stdio.h>

typedef struct tokenize tokenize_t;

/** Tokenizer incrementally tokenizes an input stream. **/
struct tokenize
{
	// current token
	char *token;

	// position where last token started
	int line, column;

	// position where next token starts
	int nextline, nextcolumn;

	// used to track accurate positions wrt ungetc
	int save_line, save_column;
	int unget_char; // the char that was unget'd, or -1

	// current position of parser (end of last token, usually)
	int in_line, in_column;

	// the current line
	char *line_buffer;
	int  line_pos, line_len;
	
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
