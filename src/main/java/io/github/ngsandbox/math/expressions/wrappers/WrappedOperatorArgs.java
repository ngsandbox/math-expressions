package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.NonNull;

import io.github.ngsandbox.math.expressions.operators.Operator;

import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;
import static java.util.Optional.ofNullable;

public class WrappedOperatorArgs extends AbstractedWrapper {
    private final Operator operator;
    private final WrappedValue value1;
    private final WrappedValue value2;
    private WrappedValue result;

    public WrappedOperatorArgs(@NonNull Operator operator,
                               WrappedValue value1,
                               WrappedValue value2) {
        this.operator = operator;
        this.value1 = value1;
        this.value2 = value2;
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

    private WrappedValue getResult() {
        if (result == null) {
            result = unwrapEval(operator.eval(
                    ofNullable(value1).map(WrappedValue::eval).orElse(WRAPPED_NULL),
                    ofNullable(value2).map(WrappedValue::eval).orElse(WRAPPED_NULL)));
        }
        return result;
    }

    @Override
    public String getExpression() {
        return operator.getOper();
    }

    @Override
    public WrappedValue copy() {
        return new WrappedOperatorArgs(operator,
                ofNullable(value1).map(WrappedValue::copy).orElse(WRAPPED_NULL),
                ofNullable(value2).map(WrappedValue::copy).orElse(WRAPPED_NULL)
        );
    }

    @Override
    public String toString() {
        return "Operator{" + value1 + operator.getOper() + value2 + "}";
    }
}
