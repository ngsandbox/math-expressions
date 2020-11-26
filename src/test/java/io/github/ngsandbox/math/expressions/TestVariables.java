package io.github.ngsandbox.math.expressions;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.NULL_CONST;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestVariables {

    @Test
    public void testVars() {
        assertEquals("6.28", new Expression("3.14*2.0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("7", new Expression("ROUND(3*(x/(3*(3+0))),0)")
                .with("x", new BigDecimal("21"))
                .eval()
                .unwrap()
                .map(BigDecimal::toString).orElse(null));
        assertEquals(
                "20",
                new Expression("(a^2)+(b^2)")
                        .with("a", new BigDecimal("2"))
                        .with("b", new BigDecimal("4"))
                        .eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
        assertEquals(
                "256",
                new Expression("a^(1+b)^3")
                        .with("a", "2")
                        .with("b", "1").eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
        assertEquals(
                "1",
                new Expression("MIN(a,b)")
                        .with("a", "1")
                        .with("b", "2").eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
        assertEquals(
                "2",
                new Expression("MAX(a,b)")
                        .with("a", "1")
                        .with("b", "2").eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
    }

    @Test
    public void testSubstitution() {
        Expression exp = new Expression("x+y");

        assertEquals("2", exp.with("x", "+1").with("y", "1").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("1", exp.with("y", "0").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("-9", exp.with("y", "-10").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", exp.with("x", "+10").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
    }

    @Test
    public void testStrings() {
        Expression exp = new Expression("y == \"ABC\"");

        assertEquals("0", exp.with("y", "1").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", exp.with("y", "\"BCA\"").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("1", exp.with("y", "\"ABC\"").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("1", exp.with("y", "'ABC'").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", exp.with("y", "'FGT'").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
    }

    @Test
    public void testWith() {
        assertEquals("21",
                new Expression("3*x").with("x", new BigDecimal("7"))
                        .eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("21",
                new Expression("3*x")
                        .with("x", "3+4")
                        .eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals(
                "20",
                new Expression("(a^2)+(b^2)")
                        .with("a", new BigDecimal("2"))
                        .with("b", new BigDecimal("4")).eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
        assertEquals(
                "68719480000",
                new Expression("a^(2+b)^2")
                        .with("a", "2")
                        .with("b", "4").eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));

        assertEquals(
                "68719480000",
                new Expression("a^(2+b)^2")
                        .with("a", "2")
                        .with("b", "4").eval()
                        .unwrap()
                        .map(BigDecimal::toPlainString)
                        .orElse(null));
    }

    @Test
    public void testNames() {
        assertEquals("21",
                new Expression("3*longname")
                        .with("longname", new BigDecimal("7"))
                        .eval().unwrap().map(BigDecimal::toString).orElse(null));

        assertEquals("21",
                new Expression("3*longname1")
                        .with("longname1", new BigDecimal("7"))
                        .eval().unwrap().map(BigDecimal::toString).orElse(null));

        assertEquals("21",
                new Expression("3*longname1").with("longname1", new BigDecimal("7"))
                        .eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void failsIfVariableDoesNotExist() {
        Assertions.assertThrows(ExpressionException.class,
                () -> new Expression("3*unknown").eval());
    }

    @Test
    public void testNullVariable() {
        Expression exp = new Expression("a").with("a", NULL_CONST);
        assertNull(exp.eval().unwrap().orElse(null));

        exp = exp.with("a", (BigDecimal) null);
        assertNull(exp.eval().unwrap().orElse(null));

        String err = "";
        try {
            new Expression("a+1").with("a", NULL_CONST).eval();
        } catch (Exception ex) {
            err = ex.getMessage();
        }
        assertEquals("First operand of `+` must not be null", err);
    }

    @Test
    public void testProcessedExpression() {
        Expression exp = new Expression("(a+(b+c))/d")
                .with("a", valueOf(10))
                .with("b", valueOf(10))
                .with("c", valueOf(10))
                .with("d", valueOf(3));
        assertEquals(10d, exp
                        .eval()
                        .unwrap()
                        .map(BigDecimal::doubleValue)
                        .orElse(0d)
                , 0.001);
        exp.with("a", valueOf(40));
        assertEquals(20d, exp
                        .eval()
                        .unwrap()
                        .map(BigDecimal::doubleValue)
                        .orElse(0d)
                , 0.001);
        exp.with("c", valueOf(40));
        assertEquals(30d, exp
                        .eval()
                        .unwrap()
                        .map(BigDecimal::doubleValue)
                        .orElse(0d)
                , 0.001);
    }
}