package io.github.ngsandbox.math.expressions.tokens;

import java.util.Map;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.operators.Operator;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.DECIMAL_SEPARATOR;
import static io.github.ngsandbox.math.expressions.operators.UnaryOperator.UNARY_OPERATOR_SUFFIX;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.COMMA;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.OPEN_PAREN;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.OPERATOR;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.UNARY_OPERATOR;
import static java.lang.Character.isDigit;

@Slf4j
public class TokenParser {
    private final String expression;
    private final int expLength;
    private final Token previousToken;
    private final Map<String, Operator> operators;
    private final StringBuilder surface = new StringBuilder();
    private final Supplier<Token> nextSupplier;
    private final int prevPos;
    private TokenType type;
    @Getter
    private int currentPosition;
    private char ch;

    /**
     * Parse token from the specified position till the next character (operator, function, etc)
     * @param expression Full math expression
     * @param previousToken previous parsed token
     * @param operators list of available operators
     * @param currentPosition keep current position of the processed character
     */
    public TokenParser(String expression,
                       Token previousToken,
                       Map<String, Operator> operators,
                       int currentPosition,
                       Supplier<Token> nextSupplier) {
        this.expression = expression;
        this.expLength = expression.length();
        this.previousToken = previousToken;
        this.operators = operators;
        this.nextSupplier = nextSupplier;
        this.prevPos = currentPosition;
        this.currentPosition = currentPosition;
    }

    public Token parse() {
        skipWhitespaces();
        if (isDigit(ch) || (ch == DECIMAL_SEPARATOR && isDigit(peekNextChar()))) {
            parseLiteral();
        } else if (ch == '"' || ch == '\'') {
            char quote = ch;
            log.trace("Start decode string parameter with quote `{}` from position {}", quote, currentPosition);
            currentPosition++;
            if (previousToken == null || previousToken.getType() != TokenType.STRINGPARAM) {
                parseStringParam(quote);
            } else {
                return nextSupplier.get();
            }
        } else if (Character.isLetter(ch)) {
            parseLiteralOperator();
        } else if (isFunctionalChars()) {
            parseFunction();
        } else {
            parseOperator();
        }

        return new Token(surface.toString(), type, prevPos);
    }

    private void parseOperator() {
        int initialPos = currentPosition;
        ch = expression.charAt(currentPosition);
        int validOperatorSeenUntil = -1;
        StringBuilder greedyMatch = new StringBuilder();
        while (isNotFinish() && !isLetterOrDigit() && !isFunctionalChars()) {
            greedyMatch.append(ch);
            currentPosition++;
            if (operators.containsKey(greedyMatch.toString())) {
                validOperatorSeenUntil = currentPosition;
            }
            ch = isNotFinish() ? expression.charAt(currentPosition) : 0;
        }
        if (validOperatorSeenUntil != -1) {
            surface.append(expression, initialPos, validOperatorSeenUntil);
            currentPosition = validOperatorSeenUntil;
        } else {
            surface.append(greedyMatch);
        }

        if (previousToken == null || !isAllowedTypeForOperator(previousToken.getType())) {
            surface.append(UNARY_OPERATOR_SUFFIX);
            type = TokenType.UNARY_OPERATOR;
        } else {
            type = TokenType.OPERATOR;
        }
    }

    private void parseLiteral() {
        log.trace("Start decode numeric parameter from position {}", currentPosition);
        while ((isDigit(ch) || ch == DECIMAL_SEPARATOR) && isNotFinish()) {
            surface.append(expression.charAt(currentPosition++));
            ch = currentPosition == expLength ? 0 : expression.charAt(currentPosition);
        }
        type = TokenType.LITERAL;
    }

    private boolean isNotFinish() {
        return currentPosition < expLength;
    }

    private void parseStringParam(char quote) {
        ch = expression.charAt(currentPosition);
        while (ch != quote) {
            surface.append(expression.charAt(currentPosition++));
            ch = currentPosition == expLength ? 0 : expression.charAt(currentPosition);
        }
        currentPosition++;
        type = TokenType.STRINGPARAM;
    }

    private void parseLiteralOperator() {
        log.trace("Start decode operator parameter from position {}", currentPosition);
        while (Character.isLetter(ch) || isDigit(ch)) {
            surface.append(expression.charAt(currentPosition++));
            ch = isNotFinish() ? expression.charAt(currentPosition) : 0;
        }
        // Remove optional white spaces after function or variable name
        if (Character.isWhitespace(ch)) {
            while (Character.isWhitespace(ch) && isNotFinish()) {
                ch = expression.charAt(currentPosition++);
            }
            currentPosition--;
        }

        if (operators.containsKey(surface.toString())) {
            type = TokenType.OPERATOR;
        } else if (ch == '(') {
            type = TokenType.FUNCTION;
        } else {
            type = TokenType.VARIABLE;
        }
    }

    /**
     * Peek at the next character, without advancing the iterator.
     *
     * @return The next character or character 0, if at end of string.
     */
    private char peekNextChar() {
        return currentPosition < expLength - 1 ? expression.charAt(currentPosition + 1) : 0;
    }

    private void parseFunction() {
        log.trace("Start decode parentheses/comma `{}` parameter from position {}", ch, currentPosition);
        if (ch == '(') {
            type = TokenType.OPEN_PAREN;
        } else if (ch == ')') {
            type = TokenType.CLOSE_PAREN;
        } else {
            type = COMMA;
        }
        surface.append(ch);
        currentPosition++;
    }

    private boolean isLetterOrDigit() {
        return Character.isLetter(ch)
                || isDigit(ch)
                || Character.isWhitespace(ch);
    }

    public static boolean isAllowedTypeForOperator(TokenType type) {
        return type != OPERATOR && type != COMMA && type != OPEN_PAREN && type != UNARY_OPERATOR;
    }

    private boolean isFunctionalChars() {
        return ch == '(' || ch == ')' || ch == ',';
    }

    private void skipWhitespaces() {
        ch = expression.charAt(currentPosition);
        while (Character.isWhitespace(ch) && isNotFinish()) {
            ch = expression.charAt(++currentPosition);
        }
    }
}
