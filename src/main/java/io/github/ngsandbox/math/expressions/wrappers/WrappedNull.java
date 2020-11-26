package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Optional;

public final class WrappedNull extends AbstractedWrapper {

    /**
     * The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters.
     */
    public static final WrappedValue PARAMS_START = new WrappedNull();

    public static final WrappedValue WRAPPED_NULL = PARAMS_START;

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public WrappedValue eval() {
        return this;
    }

    @Override
    public Optional<BigDecimal> unwrap() {
        return Optional.empty();
    }

    @Override
    public String getExpression() {
        return null;
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @Override
    public int compareToValues(WrappedValue wrapper) {
        return WRAPPED_NULL.equals(wrapper) ? 0 : -1;
    }

    @Override
    public WrappedValue copy() {
        return this;
    }

}
