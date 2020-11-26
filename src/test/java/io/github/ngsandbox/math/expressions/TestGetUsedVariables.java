package io.github.ngsandbox.math.expressions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetUsedVariables {

    @Test
    public void testVars() {
        Expression ex = new Expression("a/2*3+MIN(4, b )");
        Set<String> usedVars = ex.getUsedVariables();
        assertEquals(2, usedVars.size());
        assertTrue(usedVars.contains("a"));
        assertTrue(usedVars.contains("b"));
    }

    @Test
    public void testVarsLongNames() {
        Expression ex = new Expression("var1/2*3.14+MIN(var2, var3)");
        Set<String> usedVars = ex.getUsedVariables();
        assertEquals(3, usedVars.size());
        assertTrue(usedVars.contains("var1"));
        assertTrue(usedVars.contains("var2"));
        assertTrue(usedVars.contains("var3"));
    }

    @Test
    public void testVarsNothing() {
        Expression ex = new Expression("1/2");
        Set<String> usedVars = ex.getUsedVariables();
        assertEquals(0, usedVars.size());
    }
}