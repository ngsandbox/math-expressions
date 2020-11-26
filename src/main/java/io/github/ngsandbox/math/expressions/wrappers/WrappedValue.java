package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Wrapper for atomics and functional arguments
 */
public interface WrappedValue extends Comparable<WrappedValue> {

    boolean isPrimitive();

    /**
     * Evaluate wrapped value if this is a complex expression,
     * otherwise (NULL, BigDecimal, String, etc) return itself reference
     */
    WrappedValue eval();

    /**
     * Calculate and unwrap expression and return as as {@link BigDecimal } value
     * @implSpec for non numeric types the result will be {@link Optional#empty()}
     */
    Optional<BigDecimal> unwrap();

    /**
     * Get string representation of the wrapped value
     */
    String getExpression();

    /**
     * Compare wrapped values
     */
    int compareToValues(WrappedValue wrapper);

    /**
     * Create new instance of this object
     * @implSpec for immutable object (NULL, STRING, BigDecimal, etc...) the same object reference will be returned
     */
    WrappedValue copy();
}
