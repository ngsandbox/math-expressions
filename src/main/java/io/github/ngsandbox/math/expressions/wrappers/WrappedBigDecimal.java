package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

import lombok.Getter;

import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Optional.ofNullable;

public final class WrappedBigDecimal extends AbstractedWrapper {

    public static final Comparator<BigDecimal> COMPARATOR = nullsLast(comparing(BigDecimal::doubleValue));
    public static final WrappedValue WRAPPED_ZERO = new WrappedBigDecimal("0", BigDecimal.ZERO);
    public static final WrappedValue WRAPPED_ONE = new WrappedBigDecimal("1", BigDecimal.ONE);
    public static final WrappedValue WRAPPED_MINUS_ONE = new WrappedBigDecimal("-1", BigDecimal.valueOf(-1));

    @Getter
    private final String expression;
    @Getter
    private final BigDecimal value;

    private WrappedBigDecimal(String expression, BigDecimal value) {
        this.expression = expression;
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
        return ofNullable(value).map(BigDecimal::stripTrailingZeros);
    }

    @Override
    public String toString() {
        return "Decimal{" + expression + '}';
    }

    @Override
    public int compareToValues(WrappedValue wrapper) {
        return ofNullable(wrapper)
                .flatMap(WrappedValue::unwrap)
                .map(t -> COMPARATOR.compare(value, t))
                .orElse(1);
    }

    @Override
    public WrappedValue copy() {
        return this;
    }

    public static WrappedValue wrapBigDecimal(String text, BigDecimal value) {
        if (value == null) {
            return WRAPPED_NULL;
        } else if (BigDecimal.ZERO.equals(value)) {
            return WRAPPED_ZERO;
        } else if (BigDecimal.valueOf(-1).equals(value)) {
            return WRAPPED_MINUS_ONE;
        } else if (BigDecimal.ONE.equals(value)) {
            return WRAPPED_ONE;
        }

        return new WrappedBigDecimal(text, value);
    }

    public static WrappedValue wrapBigDecimal(BigDecimal value) {
        return wrapBigDecimal(value == null ? null : value.toPlainString(), value);
    }

    public static WrappedValue wrapBigDecimal(Double value) {
        return wrapBigDecimal(value == null ? null : BigDecimal.valueOf(value));
    }
}
