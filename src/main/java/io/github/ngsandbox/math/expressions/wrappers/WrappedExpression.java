package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.NonNull;

import io.github.ngsandbox.math.expressions.Expression;

public class WrappedExpression extends AbstractedWrapper {
    private final Expression value;

    private WrappedValue result;

    public WrappedExpression(@NonNull Expression expression) {
        this.value = expression;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public WrappedValue eval() {
        return getResult();
    }

    @Override
    public Optional<BigDecimal> unwrap() {
        return getResult().unwrap();
    }

    @Override
    public String getExpression() {
        return value.getExpressionString();
    }

    @Override
    public WrappedValue copy() {
        return new WrappedExpression(value.copy());
    }

    @Override
    public String toString() {
        return "Expr{" + value.getExpressionString() + '}';
    }

    private WrappedValue getResult() {
        if (result == null) {
            result = unwrapEval(value.eval());
        }
        return result;
    }
}
