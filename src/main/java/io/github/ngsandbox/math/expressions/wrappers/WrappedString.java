package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

import lombok.NonNull;

import static java.util.Comparator.nullsLast;

public class WrappedString extends AbstractedWrapper {
    public static final Comparator<String> COMPARATOR = nullsLast(String::compareTo);

    private final String value;

    public WrappedString(@NonNull String value) {
        this.value = value;
    }

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
        return value;
    }

    @Override
    public String toString() {
        return "STR{" + value + '}';
    }

    @Override
    public int compareToValues(WrappedValue wrapper) {
        if (wrapper instanceof WrappedString) {
            return COMPARATOR.compare(value, ((WrappedString) wrapper).value);
        }
        return COMPARATOR.compare(getExpression(), wrapper.getExpression());
    }

    @Override
    public WrappedValue copy() {
        return this;
    }
}
