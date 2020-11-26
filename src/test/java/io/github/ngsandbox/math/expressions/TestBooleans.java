package io.github.ngsandbox.math.expressions;

import java.math.BigDecimal;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ngsandbox.math.expressions.tokens.Token;
import io.github.ngsandbox.math.expressions.tokens.TokenType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBooleans {

    @Test
    public void testEmptyFailIsBoolean() {
        assertThrows(ExpressionException.class,
                () -> new Expression("").eval());
    }

    @Test
    public void testMissedArgumentIsBoolean() {
        assertThrows(ExpressionException.class,
                () -> new Expression("1-").eval());
    }

    @Test
    public void testTooManyArgumentsIsBoolean() {
        assertThrows(ExpressionException.class,
                () -> new Expression("ROUND(1,2,3)").eval());
    }

    @Test
    public void testIsBoolean() {
        assertTrue(new Expression("1==1").isBoolean());
        assertTrue(new Expression("a==b").with("a", "1").with("b", "2").isBoolean());
        assertTrue(new Expression("(1==1)||(c==a+b)").isBoolean());
        assertTrue(new Expression("(z+z==x-y)||(c==a+b)").isBoolean());
        assertTrue(new Expression("(z+z==(x-y==-1))||(c==a+b)").isBoolean());
        assertTrue(new Expression("NOT(a+b)").isBoolean());
        assertFalse(new Expression("a+b").isBoolean());
        assertFalse(new Expression("(a==b)+(b==c)").isBoolean());
        assertFalse(new Expression("IF(a==b,x+y,x-y)").isBoolean());
        assertTrue(new Expression("IF(a==b,x==y,a==b)").isBoolean());
    }

    @Test
    public void testAndTokenizer() {
        Expression exp = new Expression("1&&0");
        Iterator<Token> i = exp.getExpressionTokenizer();

        assertToken("1", TokenType.LITERAL, i.next());
        assertToken("&&", TokenType.OPERATOR, i.next());
        assertToken("0", TokenType.LITERAL, i.next());
    }

    @Test
    public void testAndEval() {
        assertEquals("0", new Expression("1&&0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1 AND 0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1&&1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1 AND 1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0&&0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0 AND 0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0&&1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0 AND 1").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testOrEval() {
        assertEquals("1", new Expression("1||0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1 OR 0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1||1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0 OR 0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("0||0").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("0||1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("0 OR 1").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testCompare() {
        assertEquals("1", new Expression("2>1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("2<1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1>2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1<2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1==2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1==1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1>=1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1.1>=1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1>=2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1<=1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1.1<=1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1<=2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1==2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1==1").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1!=2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("1!=1").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testCompareCombined() {
        assertEquals("1", new Expression("(2>1)||(1==0)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("(2>3)||(1==0)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("(2>3)||(1==0)||(1&&1)").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testMixed() {
        assertEquals("0", new Expression("1.5 * 7 == 3").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("1.5 * 7 == 10.5").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testNot() {
        assertEquals("0", new Expression("not(1)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("not(0)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("not(1.5 * 7 == 3)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("not(1.5 * 7 == 10.5)").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testConstants() {
        assertEquals("1", new Expression("TRUE!=FALSE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("TRUE==2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("null==2").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("2==null").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("null==null").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("NOT(TRUE)==FALSE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("NOT(FALSE)==TRUE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("0", new Expression("TRUE && FALSE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("TRUE || FALSE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("FALSE || TRUE").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("TRUE OR FALSE").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testIf() {
        assertEquals("5", new Expression("if(TRUE, 5, 3)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("3", new Expression("IF(FALSE, 5, 3)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("5.35", new Expression("If(2<5, 5.35, 3)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("1", new Expression("If(2<5, +1, -1)").eval().unwrap().map(BigDecimal::toString).orElse(null));
        assertEquals("-1", new Expression("If(2>5, +1, -1)").eval().unwrap().map(BigDecimal::toString).orElse(null));
    }

    @Test
    public void testDecimals() {
        assertEquals("0", new Expression("if(0.0, 1, 0)").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", new Expression("0.0 || 0.0").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", new Expression("0.0 OR 0.0").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("1", new Expression("not(0.0)").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
        assertEquals("0", new Expression("0.0 && 0.0").eval()
                .unwrap()
                .map(BigDecimal::toPlainString)
                .orElse(null));
    }

    private void assertToken(String surface, TokenType type, Token actual) {
        assertEquals(surface, actual.getSurface());
        assertEquals(type, actual.getType());
    }
}