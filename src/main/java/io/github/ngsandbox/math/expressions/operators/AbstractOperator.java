package io.github.ngsandbox.math.expressions.operators;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.wrappers.WrappedOperatorArgs;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

/**
 * Abstract implementation of an operator.
 */
@Slf4j
@Getter
@ToString
public abstract class AbstractOperator implements Operator {
    /**
     * This operators name (pattern).
     */
    private final String oper;
    /**
     * Operators priority.
     */
    private final int priority;
    /**
     * Operator is left associative.
     */
    private final boolean leftAssoc;
    /**
     * Whether this operator is boolean or not.
     */
    private final boolean booleanOperator;

    private final boolean assertArgs;

    /**
     * Creates a new operator.
     *
     * @param oper            The operator name (pattern).
     * @param priority        The operators priority.
     * @param leftAssoc       <code>true</code> if the operator is left associative,
     *                        else <code>false</code>.
     * @param booleanOperator Whether this operator is boolean.
     */
    protected AbstractOperator(@NonNull String oper,
                               int priority,
                               boolean leftAssoc,
                               boolean booleanOperator,
                               boolean assertArgs) {
        log.trace("Initialize operator {} ", oper);
        this.oper = oper;
        this.priority = priority;
        this.leftAssoc = leftAssoc;
        this.booleanOperator = booleanOperator;
        this.assertArgs = assertArgs;
    }

    public WrappedValue eval(final WrappedValue v1, final WrappedValue v2) {
        log.debug("Evaluate operator `{}` for values: `{}`, `{}`", oper, v1, v2);
        return new WrappedOperatorArgs(this, v1, v2);
    }
}
