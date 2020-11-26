package io.github.ngsandbox.math.expressions.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionException;
import io.github.ngsandbox.math.expressions.ExpressionSettings;
import io.github.ngsandbox.math.expressions.wrappers.WrappedIfArgs;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.ARG_NULL_ERROR;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.IF_FUNC;
import static io.github.ngsandbox.math.expressions.ExpressionUtils.getByIndex;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ONE;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ZERO;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.wrapBigDecimal;

@Slf4j
public final class Functions {

    public static Map<String, Function> buildFunctions(@NonNull ExpressionSettings settings) {
        return new FunctionsBuilder(settings).build();
    }

    private static final class FunctionsBuilder {
        /**
         * The {@link MathContext} to use for calculations.
         */
        private final MathContext mc;
        private final Map<String, Function> functions;

        private FunctionsBuilder(ExpressionSettings settings) {
            this.mc = settings.buildMathContext();
            this.functions = new HashMap<>();
        }

        private Map<String, Function> build() {
            addFunction(new ExpressionFunction("NOT", 1, true, this::processNot));

            addFunction(new ExpressionFunction(IF_FUNC, 3, WrappedIfArgs::new));
            addFunction(new ExpressionFunction("MAX", -1, this::processMax));
            addFunction(new ExpressionFunction("MIN", -1, this::processMin));
            addFunction(new ExpressionFunction("ABS", 1, this::processAbs));
            addFunction(new ExpressionFunction("ROUND", 2, this::processRound));
            addFunction(new ExpressionFunction("FLOOR", 1, this::processFloor));
            addFunction(new ExpressionFunction("CEILING", 1, this::processCeiling));
            return functions;
        }

        private WrappedValue processRound(List<WrappedValue> parameters) {
            log.debug("Round first parameter of list: {}", parameters);
            BigDecimal toRound = unwrap(parameters, 0);
            int precision = unwrap(parameters, 1).intValue();
            return wrapBigDecimal(toRound.setScale(precision, mc.getRoundingMode()));
        }

        private WrappedValue processMin(List<WrappedValue> parameters) {
            log.debug("Get min value from parameters: {}", parameters);
            if (parameters.isEmpty()) {
                throw new ExpressionException("MIN requires at least one parameter");
            }
            BigDecimal min = null;
            for (int i = 0; i < parameters.size(); i++) {
                BigDecimal parameter = unwrap(parameters, i);
                if (min == null || parameter.compareTo(min) < 0) {
                    min = parameter;
                }
            }
            return wrapBigDecimal(min);
        }

        private WrappedValue processMax(List<WrappedValue> parameters) {
            log.debug("Get max value from parameters: {}", parameters);
            if (parameters.isEmpty()) {
                throw new ExpressionException("MAX requires at least one parameter");
            }
            BigDecimal max = null;
            for (int i = 0; i < parameters.size(); i++) {
                BigDecimal parameter = unwrap(parameters, i);
                if (max == null || parameter.compareTo(max) > 0) {
                    max = parameter;
                }
            }

            return wrapBigDecimal(max);
        }

        private WrappedValue processAbs(List<WrappedValue> parameters) {
            return wrapBigDecimal(unwrap(parameters, 0).abs(mc));
        }

        private WrappedValue processFloor(List<WrappedValue> parameters) {
            BigDecimal result = unwrap(parameters, 0).setScale(0, RoundingMode.FLOOR);
            log.trace("Floor function result from {}: {}", parameters, result);
            return wrapBigDecimal(result);
        }

        private WrappedValue processCeiling(List<WrappedValue> parameters) {
            BigDecimal result = unwrap(parameters, 0).setScale(0, RoundingMode.CEILING);
            log.trace("Ceiling function result from {}: {}", parameters, result);
            return wrapBigDecimal(result);
        }

        private WrappedValue processNot(List<WrappedValue> parameters) {
            boolean result = unwrap(parameters, 0).compareTo(BigDecimal.ZERO) == 0;
            log.trace("NOT function result from {}: {}", parameters, result);
            return result ? WRAPPED_ONE : WRAPPED_ZERO;
        }

        private BigDecimal unwrap(List<WrappedValue> parameters, int index) {
            WrappedValue value = getByIndex(parameters, index);
            return value.unwrap()
                    .orElseThrow(() -> new ExpressionException(index + ARG_NULL_ERROR + " `" + value + "`"));
        }

        /**
         * Adds a function to the list of supported functions
         *
         * @param function The function to add.
         */
        private void addFunction(Function function) {
            functions.put(function.getName(), function);
        }
    }
}
