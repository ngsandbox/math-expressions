package io.github.ngsandbox.math.expressions.functions;

import java.util.List;
import java.util.function.Function;

import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

/**
 * Abstract definition of a supported expression function. A function is defined by a name, the
 * number of parameters and the actual processing implementation.
 */
public final class ExpressionFunction extends AbstractFunction {
    private final Function<List<WrappedValue>, WrappedValue> eval;

    public ExpressionFunction(String name, int numParams,
                              Function<List<WrappedValue>, WrappedValue> eval) {
        this(name, numParams, false, eval);
    }

    public ExpressionFunction(String name, int numParams, boolean booleanFunction,
                              Function<List<WrappedValue>, WrappedValue> eval) {
        super(name, numParams, booleanFunction);
        this.eval = eval;
    }

    @Override
    public WrappedValue calc(List<WrappedValue> parameters) {
        return eval.apply(parameters);
    }
}
