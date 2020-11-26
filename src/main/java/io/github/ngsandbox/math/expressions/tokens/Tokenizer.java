package io.github.ngsandbox.math.expressions.tokens;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionException;
import io.github.ngsandbox.math.expressions.operators.Operator;

/**
 * Expression tokenizer that allows to iterate over a {@link String} expression token by token.
 * Blank characters will be skipped.
 */
@Slf4j
public class Tokenizer implements Iterator<Token> {

    /**
     * The original input expression.
     */
    private final String input;
    private final Map<String, Operator> operators;
    /**
     * Actual position in expression string.
     */
    private int pos = 0;
    /**
     * The previous token or <code>null</code> if none.
     */
    private Token previousToken;

    /**
     * Creates a new tokenizer for an expression.
     *
     * @param input The expression string.
     */
    public Tokenizer(String input,
                     Map<String, Operator> operators) {
        this.input = input.trim();
        this.operators = operators;
    }

    @Override
    public boolean hasNext() {
        return pos < input.length();
    }

    @Override
    public Token next() {
        if (pos >= input.length()) {
            throw new NoSuchElementException("No more tokens available");
        }

        TokenParser parser = new TokenParser(input, previousToken, operators, pos, this::next);
        Token token = parser.parse();
        pos = parser.getCurrentPosition();
        previousToken = token;
        return token;
    }

    @Override
    public void remove() {
        throw new ExpressionException("remove() not supported");
    }

}
