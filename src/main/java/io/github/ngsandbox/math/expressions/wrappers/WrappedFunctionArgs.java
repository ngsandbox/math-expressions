package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.functions.Function;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class WrappedFunctionArgs extends AbstractedWrapper {
    private final Function function;
    private final List<WrappedValue> params;
    private WrappedValue result;

    public WrappedFunctionArgs(@NonNull Function function,
                               @NonNull List<WrappedValue> params) {
        this.function = function;
        this.params = unmodifiableList(params);
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
        return function.getName();
    }

    @Override
    public WrappedValue copy() {
        return new WrappedFunctionArgs(function,
                params.stream().map(WrappedValue::copy).collect(toList()));
    }

    private WrappedValue getResult() {
        if (result == null) {
            result = unwrapEval(function.calc(params));
        }
        return result;
    }

    @Override
    public String toString() {
        return "Func{" + function.getName() + '}';
    }
}
