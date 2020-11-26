package io.github.ngsandbox.math.expressions.wrappers;

import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;

public abstract class AbstractedWrapper implements WrappedValue {

    @Override
    public int compareToValues(WrappedValue wrapper) {
        return 0;
    }

    protected WrappedValue unwrapEval(WrappedValue value) {
        if (value == null) {
            return WRAPPED_NULL;
        }

        if (value.isPrimitive()) {
            return value;
        }

        return unwrapEval(value.eval());
    }

    @Override
    public final int compareTo(WrappedValue obj) {
        if (this == obj) {
            return 0;
        }

        if (obj == null) {
            return 1;
        }

        WrappedValue objEval = obj.eval();
        WrappedValue thisEval = eval();
        if (WRAPPED_NULL.equals(objEval)) {
            return 1;
        }

        if (WRAPPED_NULL.equals(thisEval)) {
            return -1;
        }

        return thisEval == this
                ? thisEval.compareToValues(objEval)
                : thisEval.compareTo(objEval);
    }
}
