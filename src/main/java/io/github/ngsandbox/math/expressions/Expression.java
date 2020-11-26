package io.github.ngsandbox.math.expressions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.github.ngsandbox.math.expressions.functions.Function;
import io.github.ngsandbox.math.expressions.operators.Operator;
import io.github.ngsandbox.math.expressions.tokens.ShuntingYardParser;
import io.github.ngsandbox.math.expressions.tokens.Token;
import io.github.ngsandbox.math.expressions.tokens.Tokenizer;
import io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal;
import io.github.ngsandbox.math.expressions.wrappers.WrappedExpression;
import io.github.ngsandbox.math.expressions.wrappers.WrappedFunctionArgs;
import io.github.ngsandbox.math.expressions.wrappers.WrappedOperatorArgs;
import io.github.ngsandbox.math.expressions.wrappers.WrappedString;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;
import io.github.ngsandbox.math.expressions.wrappers.WrappedVariable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.FALSE_CONST;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.IF_FUNC;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.NULL_CONST;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.TRUE_CONST;
import static io.github.ngsandbox.math.expressions.ExpressionUtils.isNumber;
import static io.github.ngsandbox.math.expressions.functions.Functions.buildFunctions;
import static io.github.ngsandbox.math.expressions.operators.Operators.buildOperators;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.FUNCTION;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.OPERATOR;
import static io.github.ngsandbox.math.expressions.tokens.TokenType.VARIABLE;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ONE;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ZERO;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.PARAMS_START;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;

@Slf4j
@EqualsAndHashCode(of = {"expressionString"})
@ToString(of = {"expressionString"})
public class Expression {

    /**
     * The {@link MathContext} to use for calculations.
     */
    private final MathContext mc;

    /**
     * The characters (other than letters and digits) allowed as the first character in a variable.
     */
    private final ExpressionSettings settings;

    /**
     * The current infix expression, with optional variable substitutions.
     */
    @Getter
    private final String expressionString;

    /**
     * All defined operators with name and implementation.
     */
    private final Map<String, Operator> operators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * All defined functions with name and implementation.
     */
    private final Map<String, Function> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * All defined variables with name and value.
     */
    private final Variables variables;

    private final static class Variables {
        private final Map<String, WrappedValue> vars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private List<Token> reversePolishNotation = null;

    /**
     * Creates a new expression instance from an expression string with a given default match context
     * of {@link MathContext#DECIMAL32}.
     *
     * @param expression The expression. E.g. <code>"2.4*MIN(3,5)/(2-4)"</code> or
     *                   <code>"LOG(y)>0 & max(z, 3)>3"</code>
     */
    public Expression(String expression) {
        this(expression, ExpressionSettings.builder().build());
    }

    /**
     * Creates a new expression instance from an expression string with a given default match
     * context.
     *
     * @param expression The expression. E.g. <code>"2.4*MIN(3,5)/(2-4)"</code> or
     *                   <code>"LOG(y)>0 & max(z, 3)>3"</code>
     * @param settings   The {@link MathContext} to use by default.
     */
    public Expression(String expression, ExpressionSettings settings) {
        this(expression, settings,
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                new Variables());
    }

    /**
     * Creates a new expression instance from an expression string with given settings.
     *
     * @param expression         The expression. E.g. <code>"2.4*MIN(3,5)/(2-4)"</code> or
     *                           <code>"LOG(y)>0 & max(z, 3)>3"</code>
     * @param expressionSettings The {@link ExpressionSettings} to use by default.
     */
    public Expression(String expression,
                      @NonNull ExpressionSettings expressionSettings,
                      @NonNull Map<String, Operator> operators,
                      @NonNull Map<String, Function> functions,
                      Variables variables
    ) {
        this.operators.putAll(operators);
        this.functions.putAll(functions);
        this.variables = variables;
        this.mc = expressionSettings.buildMathContext();
        this.settings = expressionSettings;
        this.expressionString = expression;
        if (this.operators.isEmpty()) {
            this.operators.putAll(buildOperators(settings));
        }

        if (this.functions.isEmpty()) {
            this.functions.putAll(buildFunctions(settings));
        }

        if (this.variables.vars.isEmpty()) {
            buildVariables();
        }
    }

    private void buildVariables() {
        variables.vars.put(NULL_CONST, WRAPPED_NULL);
        variables.vars.put(TRUE_CONST, WRAPPED_ONE);
        variables.vars.put(FALSE_CONST, WRAPPED_ZERO);
    }

    public Expression copy() {
        Expression expression = new Expression(expressionString,
                settings,
                this.operators,
                functions,
                variables);
        expression.reversePolishNotation = this.reversePolishNotation != null
                ? new ArrayList<>(this.reversePolishNotation)
                : null;
        return expression;
    }

    /**
     * Evaluates the expression with provided list of variables
     *
     * @return The wrapper result of the expression
     */
    public WrappedValue eval() {
        Deque<WrappedValue> stack = new ArrayDeque<>();
        for (final Token token : getReversePolishNotation()) {
            evaluateToken(stack, token);
        }

        return stack.pop().eval();
    }

    private void evaluateToken(Deque<WrappedValue> stack, Token token) {
        log.trace("Evaluate token `{}` with expression `{}`", token.getType(), token.getSurface());
        switch (token.getType()) {
        case UNARY_OPERATOR:
            stack.push(new WrappedOperatorArgs(getOperator(token.getSurface()), stack.pop(), null));
            break;
        case OPERATOR:
            WrappedValue value1 = stack.pop();
            WrappedValue value2 = stack.pop();
            WrappedValue wrappedValueOp = new WrappedOperatorArgs(getOperator(token.getSurface()), value2, value1);
            stack.push(wrappedValueOp);
            break;
        case VARIABLE:
            WrappedValue variable = getVariable(token.getSurface());
            stack.push(new WrappedVariable(token.getSurface(), variable));
            break;
        case FUNCTION:
            evaluateFunctionToken(stack, token);
            break;
        case OPEN_PAREN:
            stack.push(PARAMS_START);
            break;
        case LITERAL:
            evaluateLiteralToken(stack, token);
            break;
        case STRINGPARAM:
            stack.push(new WrappedString(token.getSurface()));
            break;
        default:
            throw new ExpressionException("Unexpected token " + token.getSurface(), token.getPos());
        }
    }

    private void evaluateLiteralToken(Deque<WrappedValue> stack, Token token) {
        if (NULL_CONST.equalsIgnoreCase(token.getSurface())) {
            stack.push(WRAPPED_NULL);
        } else {
            BigDecimal value = new BigDecimal(token.getSurface(), mc);
            stack.push(WrappedBigDecimal.wrapBigDecimal(token.getSurface(), value));
        }
    }

    private void evaluateFunctionToken(Deque<WrappedValue> stack, Token token) {
        Function function = getFunction(token.getSurface().toUpperCase(Locale.ROOT));
        List<WrappedValue> params = new ArrayList<>(!function.numParamsVaries() ? function.getNumParams() : 0);
        // pop parameters off the stack until we hit the start of this function's parameter list
        while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
            params.add(0, stack.pop());
        }

        if (stack.peek() == PARAMS_START) {
            stack.pop();
        }

        WrappedFunctionArgs wrappedFunc = new WrappedFunctionArgs(function, params);
        stack.push(wrappedFunc);
    }

    private WrappedValue getVariable(String key) {
        if (!variables.vars.containsKey(key)) {
            log.error("Variable `{}` not found. Available list: ```{}```", key, variables.vars.keySet());
            throw new ExpressionException("Variable does not exist " + key);
        }

        return variables.vars.get(key);
    }

    private Operator getOperator(String key) {
        Operator result = operators.get(key);
        if (result == null) {
            log.error("Operator `{}` not found. Available list: ```{}```", key, operators.keySet());
            throw new ExpressionException("Operator does not exist " + key);
        }

        return result;
    }

    private Function getFunction(String key) {
        Function result = functions.get(key);
        if (result == null) {
            log.error("Function `{}` not found. Available list: ```{}```", key, functions.keySet());
            throw new ExpressionException("Function does not exist " + key);
        }

        return result;
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    private Expression setVariable(String variable, WrappedValue value) {
        variables.vars.put(variable, value);
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    private Expression setVariable(String variable, String value) {
        log.debug("Set variable `{}` value `{}`", variable, value);
        if (value == null || value.isEmpty() || value.equalsIgnoreCase(NULL_CONST)) {
            variables.vars.put(variable, WRAPPED_NULL);
        } else if (isNumber(value)) {
            variables.vars.put(variable, WrappedBigDecimal.wrapBigDecimal(new BigDecimal(value, mc)));
        } else {
            final Expression expression = new Expression(value, settings, operators, functions, variables);
            variables.vars.put(variable, new WrappedExpression(expression));
            reversePolishNotation = null;
        }
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, BigDecimal value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, double value) {
        return setVariable(variable, BigDecimal.valueOf(value));
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    private Expression setVariable(String variable, BigDecimal value) {
        return setVariable(variable, WrappedBigDecimal.wrapBigDecimal(value));
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, WrappedValue value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, String value) {
        return setVariable(variable, value);
    }

    /**
     * Get an iterator for this expression, allows iterating over an expression token by token.
     *
     * @return A new iterator instance for this expression.
     */
    public Iterator<Token> getExpressionTokenizer() {
        return new Tokenizer(this.expressionString, operators);
    }

    /**
     * Cached access to the RPN notation of this expression, ensures only one calculation of the RPN
     * per expression instance. If no cached instance exists, a new one will be created and put to the
     * cache.
     *
     * @return The cached RPN instance.
     */
    private List<Token> getReversePolishNotation() {
        if (reversePolishNotation == null) {
            ShuntingYardParser parser = new ShuntingYardParser(this.expressionString, operators, functions);
            reversePolishNotation = parser.shuntingYard();
        }
        return reversePolishNotation;
    }

    /**
     * Returns a list of the variables in the expression.
     *
     * @return A list of the variable names in this expression.
     */
    public Set<String> getUsedVariables() {
        Set<String> result = new HashSet<>();
        Tokenizer tokenizer = new Tokenizer(expressionString, operators);
        while (tokenizer.hasNext()) {
            Token nextToken = tokenizer.next();
            String token = nextToken.getSurface();
            if (nextToken.getType() == VARIABLE
                    && !token.equalsIgnoreCase(NULL_CONST)
                    && !token.equalsIgnoreCase(TRUE_CONST)
                    && !token.equalsIgnoreCase(FALSE_CONST)) {
                result.add(token);
            }
        }
        return result;
    }

    /**
     * Checks whether the expression is a boolean expression. An expression is considered a boolean
     * expression, if the last operator or function is boolean.
     *
     * @return <code>true</code> if the last operator/function was a boolean.
     * @implSpec The IF function is handled special. If the third parameter is boolean,
     * then the IF is also considered boolean, else non-boolean.
     */
    public boolean isBoolean() {
        List<Token> rpnList = getReversePolishNotation();
        for (int i = rpnList.size() - 1; i >= 0; i--) {
            Token token = rpnList.get(i);
            if (!IF_FUNC.equalsIgnoreCase(token.getSurface())) {
                if (token.getType() == FUNCTION) {
                    return getFunction(token.getSurface()).isBooleanFunction();
                } else if (token.getType() == OPERATOR) {
                    return getOperator(token.getSurface()).isBooleanOperator();
                }
            }
        }
        return false;
    }

    /**
     * Check if the expression contains required (not null) arguments {@link Operator#isAssertArgs()}
     */
    public boolean hasRequiredArgs() {
        return getReversePolishNotation().stream()
                .filter(t -> OPERATOR == t.getType())
                .map(Token::getSurface)
                .map(this::getOperator)
                .anyMatch(Operator::isAssertArgs);
    }
}
