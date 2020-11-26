package io.github.ngsandbox.math.expressions.operators;

import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

/**
 * Base interface which is required for all operators.
 */
public interface Operator {

    /**
     * Gets the String that is used to denote the operator in the expression.
     *
     * @return The String that is used to denote the operator in the expression.
     */
    String getOper();

    /**
     * Gets the priority value of this operator.
     *
     * @return the priority value of this operator.
     */
    int getPriority();

    /**
     * Gets whether this operator is left associative (<code>true</code>) or if
     * this operator is right associative (<code>false</code>).
     *
     * @return <code>true</code> if this operator is left associative.
     */
    boolean isLeftAssoc();

    /**
     * Gets whether this operator evaluates to a boolean expression.
     *
     * @return <code>true</code> if this operator evaluates to a boolean
     * expression.
     */
    boolean isBooleanOperator();

    /**
     * @return <code>true</code> if provided arguments should not be NULL.
     */
    boolean isAssertArgs();

    /**
     * Implementation for this operator.
     *
     * @param v1 Operand 1.
     * @param v2 Operand 2.
     * @return The result of the operation.
     */
    WrappedValue eval(WrappedValue v1, WrappedValue v2);
}
