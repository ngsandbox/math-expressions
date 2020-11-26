package io.github.ngsandbox.math.expressions.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionException;
import io.github.ngsandbox.math.expressions.functions.Function;
import io.github.ngsandbox.math.expressions.operators.Operator;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.MISSING_PARAMETERS_FOR_OPERATOR;
import static io.github.ngsandbox.math.expressions.tokens.TokenParser.isAllowedTypeForOperator;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.CLOSE_PAREN;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.COMMA;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.FUNCTION;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.LITERAL;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.OPEN_PAREN;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.OPERATOR;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.UNARY_OPERATOR;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.VARIABLE;

/**
 * Implementation of the <i>Shunting Yard</i> algorithm to transform an infix expression to a RPN
 * expression.
 */
@Slf4j
public class ShuntingYardParser {
    /**
     * The current infix expression, with optional variable substitutions.
     */
    @Getter
    private final String expressionString;

    private final Map<String, Operator> operators;
    private final Map<String, Function> functions;
    private final Tokenizer tokenizer;
    private final Stack<Token> parserStack = new Stack<>();
    private final List<Token> tokens = new ArrayList<>();
    private Token lastFunction = null;
    private Token prevToken = null;

    public ShuntingYardParser(@NonNull String expressionString,
                              @NonNull Map<String, Operator> operators,
                              @NonNull Map<String, Function> functions) {
        this.expressionString = expressionString;
        this.operators = operators;
        this.functions = functions;
        tokenizer = new Tokenizer(this.expressionString, operators);
    }

    /**
     * Transform an infix expression to a RPN expression.
     *
     * @return A RPN representation of the expression, with each token as a list member.
     */
    public List<Token> shuntingYard() {
        log.debug("Run Shunting Yard algorithm for expression {}", expressionString);
        while (tokenizer.hasNext()) {
            Token token = tokenizer.next();
            switch (token.getType()) {
            case STRINGPARAM:
                parserStack.push(token);
                break;
            case LITERAL:
                if (prevToken != null && prevToken.getType() == LITERAL) {
                    throw new ExpressionException("Missing operator", token.getPos());
                }
                tokens.add(token);
                break;
            case VARIABLE:
                tokens.add(token);
                break;
            case FUNCTION:
                parserStack.push(token);
                lastFunction = token;
                break;
            case COMMA:
                processComma(token);
                break;
            case OPERATOR:
                processOperator(token);
                break;
            case UNARY_OPERATOR:
                processUnaryOperator(token);
                break;
            case OPEN_PAREN:
                processOpenParentheses(token);
                break;
            case CLOSE_PAREN:
                processCloseParentheses();
                break;
            default:
                throw new ExpressionException("Unknown token type: " + token.getType());
            }
            prevToken = token;
        }

        while (!parserStack.isEmpty()) {
            Token element = parserStack.pop();
            if (element.getType() == OPEN_PAREN || element.getType() == CLOSE_PAREN) {
                throw new ExpressionException("Mismatched parentheses");
            }
            tokens.add(element);
        }
        validate();
        return tokens;
    }

    private void processUnaryOperator(Token token) {
        if (prevToken != null
                && isAllowedTypeForOperator(prevToken.getType())) {
            throw new ExpressionException("Invalid position for unary operator " + token, token.getPos());
        }
        Operator o1 = operators.get(token.getSurface());
        if (o1 == null) {
            throw new ExpressionException("Unknown unary operator " + token.getSurface(), token.getPos() + 1);
        }

        shuntOperators(o1);
        parserStack.push(token);
    }

    private void processOperator(Token token) {
        if (prevToken != null
                && (prevToken.getType() == COMMA || prevToken.getType() == OPEN_PAREN)) {
            throw new ExpressionException(
                    MISSING_PARAMETERS_FOR_OPERATOR + token, token.getPos());
        }
        Operator operator = operators.get(token.getSurface());
        if (operator == null) {
            throw new ExpressionException("Unknown operator " + token, token.getPos() + 1);
        }

        shuntOperators(operator);
        parserStack.push(token);
    }

    private void processComma(Token token) {
        if (prevToken != null && prevToken.getType() == OPERATOR) {
            throw new ExpressionException(MISSING_PARAMETERS_FOR_OPERATOR + prevToken, prevToken.getPos());
        }
        while (!parserStack.isEmpty() && parserStack.peek().getType() != OPEN_PAREN) {
            tokens.add(parserStack.pop());
        }
        if (parserStack.isEmpty()) {
            if (lastFunction == null) {
                throw new ExpressionException("Unexpected comma", token.getPos());
            } else {
                throw new ExpressionException("Parse error for function " + lastFunction, token.getPos());
            }
        }
    }

    private void processOpenParentheses(Token token) {
        if (prevToken != null) {
            if (prevToken.getType() == LITERAL || prevToken.getType() == CLOSE_PAREN || prevToken.getType() == VARIABLE) {
                // Implicit multiplication, e.g. 23(a+b) or (a+b)(a-b)
                parserStack.push(new Token("*", OPERATOR, 0));
            }
            // if the ( is preceded by a valid function, then it denotes the start of a parameter list
            if (prevToken.getType() == FUNCTION) {
                tokens.add(token);
            }
        }
        parserStack.push(token);
    }

    private void processCloseParentheses() {
        if (prevToken != null && prevToken.getType() == OPERATOR) {
            throw new ExpressionException(MISSING_PARAMETERS_FOR_OPERATOR + prevToken, prevToken.getPos());
        }
        while (!parserStack.isEmpty() && parserStack.peek().getType() != OPEN_PAREN) {
            tokens.add(parserStack.pop());
        }
        if (parserStack.isEmpty()) {
            throw new ExpressionException("Mismatched parentheses");
        }
        parserStack.pop();
        if (!parserStack.isEmpty() && parserStack.peek().getType() == FUNCTION) {
            tokens.add(parserStack.pop());
        }
    }

    private void shuntOperators(Operator o1) {
        Token nextToken = parserStack.isEmpty() ? null : parserStack.peek();
        while (nextToken != null
                && isOperator(nextToken.getType())
                && (isLeftAndPrior(o1, nextToken) || isPrior(o1, nextToken))
        ) {
            tokens.add(parserStack.pop());
            nextToken = parserStack.isEmpty() ? null : parserStack.peek();
        }
    }

    private boolean isPrior(Operator o1, Token nextToken) {
        return o1.getPriority() < operators.get(nextToken.getSurface()).getPriority();
    }

    private boolean isLeftAndPrior(Operator o1, Token nextToken) {
        return o1.isLeftAssoc() && o1.getPriority() <= operators.get(nextToken.getSurface()).getPriority();
    }

    private boolean isOperator(TokenType type) {
        return type == OPERATOR || type == UNARY_OPERATOR;
    }

    /**
     * Check that the expression has enough numbers and variables to fit the requirements of the
     * operators and functions, also check for only 1 result stored at the end of the evaluation.
     */
    private void validate() {
        log.debug("Run validation or expression {}", expressionString);
        Stack<Integer> stack = new Stack<>();
        // push the 'global' scope
        stack.push(0);

        for (final Token token : tokens) {
            Integer value = stack.peek();
            switch (token.getType()) {
            case UNARY_OPERATOR:
                if (value < 1) {
                    throw new ExpressionException(MISSING_PARAMETERS_FOR_OPERATOR + token);
                }
                break;
            case OPERATOR:
                if (value < 2) {
                    throw new ExpressionException(MISSING_PARAMETERS_FOR_OPERATOR + token);
                }
                // pop the operator's 2 parameters and add the result
                stack.set(stack.size() - 1, value - 2 + 1);
                break;
            case FUNCTION:
                validateFunctionToken(stack, token);
                break;
            case OPEN_PAREN:
                stack.push(0);
                break;
            default:
                stack.set(stack.size() - 1, value + 1);
            }
        }

        if (stack.size() > 1) {
            throw new ExpressionException("Too many unhandled function parameter lists");
        } else if (stack.peek() > 1) {
            throw new ExpressionException("Too many numbers or variables");
        } else if (stack.peek() < 1) {
            throw new ExpressionException("Empty expression");
        }
    }

    private void validateFunctionToken(Stack<Integer> stack, //NOSONAR
                                       Token token) {
        Function func = getFunction(token.getSurface().toUpperCase(Locale.ROOT));
        int numParams = stack.pop();
        if (!func.numParamsVaries() && numParams != func.getNumParams()) {
            throw new ExpressionException("Function " + token + " expected " + func.getNumParams() + " parameters, got " + numParams);
        }
        if (stack.isEmpty()) {
            throw new ExpressionException("Too many function calls, maximum scope exceeded");
        }
        // push the result of the function
        stack.set(stack.size() - 1, stack.peek() + 1);
    }

    private Function getFunction(String key) {
        Function result = functions.get(key);
        if (result == null) {
            log.error("Function `{}` not found. Available list: ```{}```", key, functions.keySet());
            throw new ExpressionException("Function does not exist " + key);
        }

        return result;
    }

}
