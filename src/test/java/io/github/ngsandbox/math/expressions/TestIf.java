package io.github.ngsandbox.math.expressions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

import static io.github.ngsandbox.math.expressions.ExpressionConstants.NULL_CONST;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIf {

    @Test
    public void testLazyIf() {
        Expression expression = new Expression("if(a==0,0,12/a)")
                .with("a", ZERO);
        assertEquals(ZERO, expression
                .eval()
                .unwrap()
                .orElse(null));
    }

    @Test
    public void testLazyIfWithNestedFunction() {
        Expression expression = new Expression("if(a==0,0,abs(12/a))")
                .with("a", ZERO);
        assertEquals(ZERO, expression
                .eval()
                .unwrap()
                .orElse(null));
    }

    @Test
    public void testLazyIfWithNestedSuccessIf() {
        Expression expression = new Expression("if(a==0,0,if(5/a>3,2,4))")
                .with("a", ZERO);
        assertEquals(ZERO, expression
                .eval()
                .unwrap()
                .orElse(null));
    }

    @Test
    public void testLazyIfWithStringResult() {
        Expression expression = new Expression("if(a==0,\"ERR\",if(5/a>3,2,4))")
                .with("a", ZERO);
        assertEquals("ERR", expression
                .eval().getExpression());
    }

    @Test
    public void testLazyIfWithNestedFailingIf() {
        Assertions.assertThrows(ExpressionException.class,
                () -> new Expression("if(a==0,if(5/a>3,2,4),0)")
                        .with("a", ZERO).eval());
    }

    @Test
    public void testLazyIfWithNull() {
        String err = "";
        WrappedValue a = null;
        try {
            a = new Expression("if(a,0,12/a)").with("a", NULL_CONST).eval();
        } catch (Exception ex) {
            err = ex.getMessage();
        }

        assertEquals("First argument of IF expression ([VAR{a}, Decimal{0}, Operator{Decimal{12}/VAR{a}}]) must not be NULL", err);
    }

}