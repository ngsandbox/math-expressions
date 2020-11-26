package io.github.ngsandbox.math.expressions.operators;

import java.util.function.BinaryOperator;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionException;
import io.github.ngsandbox.math.expressions.ExpressionUtils;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

import static java.lang.String.format;

/**
 * Abstract definition of a supported operator. An operator is defined by its name (pattern),
 * priority and if it is left- or right associative.
 */
@Slf4j
public final class ExpressionOperator extends AbstractOperator {

    private final BinaryOperator<WrappedValue> internalEval;

    /**
     * Creates a new operator.
     *
     * @param oper            The operator name (pattern).
     * @param priority        The operators priority.
     * @param leftAssoc       <code>true</code> if the operator is left associative,
     *                        else <code>false</code>.
     * @param booleanOperator Whether this operator is boolean.
     */
    public ExpressionOperator(String oper,
                              int priority,
                              boolean leftAssoc,
                              boolean booleanOperator,
                              boolean assertArgs,
                              @NonNull BinaryOperator<WrappedValue> internalEval) {
        super(oper, priority, leftAssoc, booleanOperator, assertArgs);
        this.internalEval = internalEval;
    }

    /**
     * Creates a new operator.
     *
     * @param oper            The operator name (pattern).
     * @param priority        The operators priority.
     * @param leftAssoc       <code>true</code> if the operator is left associative,
     *                        else <code>false</code>.
     * @param booleanOperator Whether this operator is boolean.
     */
    public ExpressionOperator(String oper,
                              int priority,
                              boolean leftAssoc,
                              boolean booleanOperator,
                              BinaryOperator<WrappedValue> internalEval) {
        this(oper, priority, leftAssoc, booleanOperator, false, internalEval);
    }

    /**
     * Creates a new operator.
     *
     * @param oper      The operator name (pattern).
     * @param priority  The operators priority.
     * @param leftAssoc <code>true</code> if the operator is left associative,
     *                  else <code>false</code>.
     */
    public ExpressionOperator(String oper,
                              int priority,
                              boolean leftAssoc,
                              BinaryOperator<WrappedValue> internalEval) {
        this(oper, priority, leftAssoc, false, true, internalEval);
    }

    /**
     * Creates a new operator.
     *
     * @param oper      The operator name (pattern).
     * @param priority  The operators priority.
     * @param leftAssoc <code>true</code> if the operator is left associative,
     *                  else <code>false</code>.
     */
    public ExpressionOperator(char oper,
                              int priority,
                              boolean leftAssoc,
                              BinaryOperator<WrappedValue> internalEval) {
        this(String.valueOf(oper), priority, leftAssoc, false, true, internalEval);
    }

    @Override
    public WrappedValue eval(WrappedValue v1, WrappedValue v2) {
        log.trace("Evaluate {} for values: `{}`, `{}`", getOper(), v1, v2);
        if (isAssertArgs()) {
            if (ExpressionUtils.isNull(v1) || ExpressionUtils.isNull(v1.eval())) {
                throw new ExpressionException(format("First operand of `%s` must not be null", getOper()));
            }
            if (ExpressionUtils.isNull(v2) || ExpressionUtils.isNull(v2.eval())) {
                throw new ExpressionException(format("Second operand of `%s` must not be null", getOper()));
            }
        }
        WrappedValue result = internalEval.apply(v1, v2);
        log.trace("Evaluation result for operator {} with values: `{}`, `{}`: '{}'", getOper(), v1, v2, result);
        return result;
    }
}
