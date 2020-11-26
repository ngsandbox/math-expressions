package io.github.ngsandbox.math.expressions.functions;

import java.util.Locale;

import lombok.Getter;
import lombok.ToString;

/**
 * Abstract implementation of a direct function.<br>
 * <br>
 * This abstract implementation does implement lazyEval so that it returns
 * the result of eval.
 */
@Getter
@ToString
public abstract class AbstractFunction implements Function {

    /**
     * Name of this function.
     */
    private final String name;
    /**
     * Number of parameters expected for this function. <code>-1</code>
     * denotes a variable number of parameters.
     */
    private final int numParams;

    /**
     * Whether this function is a boolean function.
     */
    private final boolean booleanFunction;

    /**
     * Creates a new function with given name and parameter count.
     *
     * @param name            The name of the function.
     * @param numParams       The number of parameters for this function.
     *                        <code>-1</code> denotes a variable number of parameters.
     * @param booleanFunction Whether this function is a boolean function.
     */
    protected AbstractFunction(String name, int numParams, boolean booleanFunction) {
        this.name = name.toUpperCase(Locale.ROOT);
        this.numParams = numParams;
        this.booleanFunction = booleanFunction;
    }

    public boolean numParamsVaries() {
        return numParams < 0;
    }
}