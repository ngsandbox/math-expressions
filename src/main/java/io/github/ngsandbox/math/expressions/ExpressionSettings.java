package io.github.ngsandbox.math.expressions;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.function.Supplier;

import lombok.Builder;

import static java.math.MathContext.DECIMAL32;

/**
 * Expression settings can be used to set certain defaults, when creating a new expression. Settings
 * are read only and can be created using a {@link io.github.ngsandbox.math.expressions.ExpressionSettings#builder()}.
 *
 * @see io.github.ngsandbox.math.expressions.Expression ( String , io.github.ngsandbox.math.expressions.ExpressionSettings )
 */
@Builder
public final class ExpressionSettings {

    /**
     * The math context to use. Default is {@link MathContext#DECIMAL32}.
     */
    private final MathContext mathContext;

    /**
     * The number of digits to be used for an operation.  A value of 0
     * indicates that unlimited precision (as many digits as are
     * required) will be used.  Note that leading zeros (in the
     * coefficient of a number) are never significant.
     *
     * <p>{@code precision} will always be non-negative.
     *
     * @serial
     */
    private final Integer precision;

    /**
     * The rounding algorithm to be used for an operation.
     *
     * @see RoundingMode
     */
    private final RoundingMode roundingMode;

    /**
     * Current date supplier mostly for Mocks
     */
    private final Supplier<Date> currentDateSupplier;

    public Supplier<Date> getCurrentDateSupplier() {
        return currentDateSupplier == null
                ? Date::new
                : currentDateSupplier;
    }

    public MathContext buildMathContext() {
        if (mathContext != null) {
            return mathContext;
        }

        if (precision != null && roundingMode != null) {
            return new MathContext(precision, roundingMode);
        }

        if (roundingMode != null) {
            return new MathContext(DECIMAL32.getPrecision(), roundingMode);
        }

        if (precision != null) {
            return new MathContext(precision);
        }

        return DECIMAL32;
    }
}
