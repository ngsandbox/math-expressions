package io.github.ngsandbox.math.expressions.tokens;

import lombok.Data;

@Data
public class Token {

    private final String surface;
    private final TokenType type;
    private final int pos;

    public Token(String surface, TokenType type, int pos) {
        this.surface = surface;
        this.type = type;
        this.pos = pos;
    }
}
