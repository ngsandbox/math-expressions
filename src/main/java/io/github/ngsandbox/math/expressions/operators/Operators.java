package io.github.ngsandbox.math.expressions.operators;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.ngsandbox.math.expressions.ExpressionSettings;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.FIRST_ARG_NULL_ERROR;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.MINUS_SIGN;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_ADDITIVE;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_AND;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_COMPARISON;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_EQUALITY;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_MULTIPLICATIVE;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_OR;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_POWER;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.OPERATOR_PRIORITY_UNARY;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.PLUS_SIGN;
import static io.github.ngsandbox.math.expressions.ExpressionConstants.SECOND_ARG_NULL_ERROR;
import static io.github.ngsandbox.math.expressions.operators.UnaryOperator.UNARY_OPERATOR_SUFFIX;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_MINUS_ONE;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ONE;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.WRAPPED_ZERO;
import static io.github.ngsandbox.math.expressions.wrappers.WrappedBigDecimal.wrapBigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

@Slf4j
public final class Operators {

    public static Map<String, Operator> buildOperators(@NonNull ExpressionSettings settings) {
        return new OperatorsBuilder(settings).build();
    }

    public static class OperatorsBuilder {

        /**
         * The {@link MathContext} to use for calculations.
         */
        private final MathContext mc;
        private final Map<String, Operator> operators;

        public OperatorsBuilder(ExpressionSettings settings) {
            this.mc = settings.buildMathContext();
            this.operators = new HashMap<>();
        }

        private Map<String, Operator> build() {
            addOperator(new ExpressionOperator(PLUS_SIGN, OPERATOR_PRIORITY_ADDITIVE, true, this::plusOperator));
            addOperator(new ExpressionOperator(MINUS_SIGN, OPERATOR_PRIORITY_ADDITIVE, true, this::minusOperator));
            addOperator(new ExpressionOperator("*", OPERATOR_PRIORITY_MULTIPLICATIVE, true, this::multiplyOperator));
            addOperator(new ExpressionOperator("/", OPERATOR_PRIORITY_MULTIPLICATIVE, true, this::divideOperator));
            addOperator(new ExpressionOperator("%", OPERATOR_PRIORITY_MULTIPLICATIVE, true, this::remaiderOperator));
            addOperator(new ExpressionOperator("^", OPERATOR_PRIORITY_POWER, false, this::powerOperator));
            addOperator(new ExpressionOperator("&&", OPERATOR_PRIORITY_AND, false, true, this::priorityAnd));
            addOperator(new ExpressionOperator("AND", OPERATOR_PRIORITY_AND, false, true, this::priorityAnd));
            addOperator(new ExpressionOperator("||", OPERATOR_PRIORITY_OR, false, true, this::priorityOr));
            addOperator(new ExpressionOperator("OR", OPERATOR_PRIORITY_OR, false, true, this::priorityOr));
            addOperator(new ExpressionOperator(">", OPERATOR_PRIORITY_COMPARISON, false, true, true,
                    (v1, v2) -> v1.compareTo(v2) > 0 ? WRAPPED_ONE : WRAPPED_ZERO));
            addOperator(new ExpressionOperator(">=", OPERATOR_PRIORITY_COMPARISON, false, true, true,
                    (v1, v2) -> v1.compareTo(v2) >= 0 ? WRAPPED_ONE : WRAPPED_ZERO));
            addOperator(new ExpressionOperator("<", OPERATOR_PRIORITY_COMPARISON, false, true, true,
                    (v1, v2) -> v1.compareTo(v2) < 0 ? WRAPPED_ONE : WRAPPED_ZERO));
            addOperator(new ExpressionOperator("<=", OPERATOR_PRIORITY_COMPARISON, false, true, true,
                    (v1, v2) -> v1.compareTo(v2) <= 0 ? WRAPPED_ONE : WRAPPED_ZERO));
            addOperator(new ExpressionOperator("==", OPERATOR_PRIORITY_EQUALITY, false, true, this::priorityEquality));
            addOperator(new ExpressionOperator("!=", OPERATOR_PRIORITY_EQUALITY, false, true, this::priorityNotEquality));
            addOperator(new ExpressionOperator("<>", OPERATOR_PRIORITY_EQUALITY, false, true, this::priorityNotEquality));
            addOperator(new UnaryOperator(PLUS_SIGN, OPERATOR_PRIORITY_UNARY, v -> multiplyOperator(v, WRAPPED_ONE)));
            addOperator(new UnaryOperator(MINUS_SIGN, OPERATOR_PRIORITY_UNARY, v -> multiplyOperator(v, WRAPPED_MINUS_ONE)));
            return operators;
        }

        private WrappedValue priorityNotEquality(WrappedValue v1, WrappedValue v2) {
            return Objects.equals(priorityEquality(v1, v2), WRAPPED_ONE) ? WRAPPED_ZERO : WRAPPED_ONE;
        }

        private WrappedValue priorityEquality(WrappedValue v1, WrappedValue v2) {
            if (Objects.equals(v1, v2)) {
                return WRAPPED_ONE;
            }
            if (v1 == null || v2 == null) {
                return WRAPPED_ZERO;
            }
            return v1.compareTo(v2) == 0 ? WRAPPED_ONE : WRAPPED_ZERO;
        }

        private WrappedValue priorityOr(WrappedValue v1, WrappedValue v2) {
            BigDecimal var1 = unwrap(v1, true);
            if (ONE.equals(var1)) {
                return WRAPPED_ONE;
            }

            BigDecimal var2 = unwrap(v2, true);
            if (ONE.equals(var2)) {
                return WRAPPED_ONE;
            }

            return WRAPPED_ZERO;
        }

        private WrappedValue priorityAnd(WrappedValue v1, WrappedValue v2) {
            BigDecimal var1 = unwrap(v1, true);
            if (ZERO.equals(var1)) {
                return WRAPPED_ZERO;
            }

            BigDecimal var2 = unwrap(v2, true);
            if (ZERO.equals(var2)) {
                return WRAPPED_ZERO;
            }

            return WRAPPED_ONE;
        }

        private WrappedValue powerOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal var1 = unwrap(v1.eval(), true);
            BigDecimal var2 = unwrap(v2.eval(), false);
            int signOf2 = var2.signum();
            double dn1 = var1.doubleValue();
            var2 = var2.multiply(BigDecimal.valueOf(signOf2)); // n2 is now positive
            BigDecimal remainderOf2 = var2.remainder(ONE);
            BigDecimal n2IntPart = var2.subtract(remainderOf2);
            BigDecimal intPow = var1.pow(n2IntPart.intValueExact(), mc);
            BigDecimal doublePow = BigDecimal.valueOf(Math.pow(dn1, remainderOf2.doubleValue()));

            BigDecimal result = intPow.multiply(doublePow, mc);
            if (signOf2 == -1) {
                result = ONE.divide(result, mc.getPrecision(), RoundingMode.HALF_UP);
            }
            return wrapBigDecimal(result);
        }

        /**
         * Adds an operator to the list of supported operators.
         *
         * @param operator The operator to add.
         */
        private <O extends Operator> void addOperator(O operator) {
            String key = operator.getOper();
            if (operator instanceof UnaryOperator) {
                key += UNARY_OPERATOR_SUFFIX;
            }
            operators.put(key, operator);
        }

        private WrappedValue plusOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal value = unwrap(v1.eval(), true)
                    .add(unwrap(v2.eval(), false));
            return wrapBigDecimal(value);
        }

        private WrappedValue minusOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal value = unwrap(v1.eval(), true)
                    .subtract(unwrap(v2.eval(), false));
            return wrapBigDecimal(value);
        }

        private WrappedValue multiplyOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal value = unwrap(v1.eval(), true)
                    .multiply(unwrap(v2.eval(), false));
            return wrapBigDecimal(value);
        }

        private WrappedValue divideOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal var1 = unwrap(v1.eval(), true);
            BigDecimal var2 = unwrap(v2.eval(), false);
            if (BigDecimal.ZERO.equals(var2)) {
                throw new io.github.ngsandbox.math.expressions.ExpressionException("Division by zero from the second argument " + v2);
            }
            return wrapBigDecimal(var1.divide(var2, mc));
        }

        private WrappedValue remaiderOperator(WrappedValue v1, WrappedValue v2) {
            BigDecimal var1 = unwrap(v1.eval(), true);
            BigDecimal var2 = unwrap(v2.eval(), false);
            if (BigDecimal.ZERO.equals(var2)) {
                throw new io.github.ngsandbox.math.expressions.ExpressionException("Division by zero from the second argument " + v2);
            }
            return wrapBigDecimal(var1.remainder(var2, mc));
        }

        private BigDecimal unwrap(WrappedValue value, boolean first) {
            return value.unwrap()
                    .orElseThrow(() -> new io.github.ngsandbox.math.expressions.ExpressionException(
                            (first ? FIRST_ARG_NULL_ERROR : SECOND_ARG_NULL_ERROR) + " `" + value + "`"));
        }
    }
}
