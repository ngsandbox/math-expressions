package io.github.ngsandbox.math.expressions.operators;

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
public final class UnaryOperator extends AbstractOperator {

    public static final String UNARY_OPERATOR_SUFFIX = "u";

    private final java.util.function.UnaryOperator<WrappedValue> internalEval;

    /**
     * Creates a new unary operator.
     *
     * @param oper     The operator name (pattern).
     * @param priority The operators priority.
     */
    public UnaryOperator(char oper,
                         int priority,
                         @NonNull java.util.function.UnaryOperator<WrappedValue> internalEval) {
        super(String.valueOf(oper), priority, false, false, true);
        this.internalEval = internalEval;
    }

    @Override
    public WrappedValue eval(WrappedValue v1, WrappedValue v2) {
        log.trace("Unary evaluation {} for values: `{}`, `{}`", getOper(), v1, v2);
        if (!ExpressionUtils.isNull(v2)) {
            throw new ExpressionException(format("Second operand of `%s` must not be provided for unary operation", getOper()));
        }

        if (ExpressionUtils.isNull(v1) || ExpressionUtils.isNull(v1.eval())) {
            throw new ExpressionException(format("First operand of `%s` must not be null", getOper()));
        }

        WrappedValue result = internalEval.apply(v1);
        log.trace("Evaluation result for operator {} with values: `{}`, `{}`: '{}'", getOper(), v1, v2, result);
        return result;
    }
}
