package io.github.ngsandbox.math.expressions;

/**
 * The expression evaluators exception class.
 */
public class ExpressionException extends RuntimeException {

    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(String message, int characterPosition) {
        super(message + " at character position " + characterPosition);
    }
}
