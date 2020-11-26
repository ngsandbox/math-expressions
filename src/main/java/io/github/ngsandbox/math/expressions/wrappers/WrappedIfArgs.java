package io.github.ngsandbox.math.expressions.wrappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionException;

import static io.github.ngsandbox.math.expressions.ExpressionUtils.getByIndex;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Lazy Number for IF function created for lazily evaluated IF condition
 */
@Slf4j
public class WrappedIfArgs extends AbstractedWrapper {
    private final List<WrappedValue> args;
    private WrappedValue result;

    public WrappedIfArgs(@NonNull List<WrappedValue> args) {
        this.args = unmodifiableList(args);
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
            log.trace("Evaluate IF with parameters: {}", args);
            BigDecimal first = getByIndex(args, 0).eval()
                    .unwrap()
                    .orElseThrow(() -> new ExpressionException(format("First argument of IF expression (%s) must not be NULL", args)));
            result = unwrapEval(first.compareTo(BigDecimal.ZERO) != 0
                    ? getByIndex(args, 1).eval()
                    : getByIndex(args, 2).eval());
            log.debug("Evaluation IF result with parameters: `{}`: `{}`", args, result);
        }
        return result;
    }

    @Override
    public String getExpression() {
        return getByIndex(args, 0).getExpression();
    }

    @Override
    public WrappedValue copy() {
        return new WrappedIfArgs(args.stream().map(WrappedValue::copy).collect(toList()));
    }

    @Override
    public String toString() {
        return "IF{" + args + '}';
    }
}