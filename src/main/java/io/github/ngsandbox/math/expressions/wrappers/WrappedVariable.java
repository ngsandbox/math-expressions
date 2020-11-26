package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.NonNull;

import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;
import static java.util.Optional.ofNullable;

public class WrappedVariable extends AbstractedWrapper {
    private final String expression;
    private final WrappedValue variable;
    private WrappedValue result;

    public WrappedVariable(@NonNull String expression,
                           WrappedValue variable) {
        this.expression = expression;
        this.variable = variable;
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
        return expression;
    }

    @Override
    public String toString() {
        return "VAR{" + expression + '}';
    }

    @Override
    public int compareToValues(WrappedValue obj) {
        return 0;
    }

    @Override
    public WrappedValue copy() {
        return new WrappedVariable(expression,
                ofNullable(variable)
                        .map(WrappedValue::eval)
                        .orElse(WRAPPED_NULL));
    }

    private WrappedValue getResult() {
        if (result == null) {
            result = ofNullable(variable)
                    .map(WrappedValue::eval)
                    .orElse(WRAPPED_NULL);
        }
        return result;
    }
}
