package io.github.ngsandbox.math.expressions.functions;

import java.util.List;

import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

/**
 * Base interface which is required for all directly evaluated functions.
 */
public interface Function {

    /**
     * Gets the name of this function.<br>
     * <br>
     * The name is use to invoke this function in the expression.
     *
     * @return The name of this function.
     */
    String getName();

    /**
     * Gets the number of parameters this function accepts.<br>
     * <br>
     * A value of <code>-1</code> denotes that this function accepts a variable number of parameters.
     *
     * @return The number of parameters this function accepts.
     */
    int getNumParams();

    /**
     * Gets whether the number of accepted parameters varies.<br>
     * <br>
     * That means that the function does accept an undefined amount of parameters.
     *
     * @return <code>true</code> if the number of accepted parameters varies.
     */
    boolean numParamsVaries();

    /**
     * Gets whether this function evaluates to a boolean expression.
     *
     * @return <code>true</code> if this function evaluates to a boolean
     * expression.
     */
    boolean isBooleanFunction();

    /**
     * Evaluate this function.
     *
     * @param lazyParams The accepted parameters.
     * @return The lazy result of this function.
     */
    WrappedValue calc(List<WrappedValue> lazyParams);
}
