package io.github.ngsandbox.math.expressions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplicationMainTest {

    @Test
    public void testNotValidArgs() {
        ApplicationMain app = new ApplicationMain();
        assertFalse(app.processArguments(new String[] {}).isPresent());
        assertFalse(app.processArguments(new String[] {"(a+(b+c))/d", "a"}).isPresent());
        assertFalse(app.processArguments(new String[] {"(a+(b+c))/d", "a", "10", "b"}).isPresent());
    }

    @Test
    public void testValidArgs() {
        ApplicationMain app = new ApplicationMain();
        Assertions.assertEquals("10",
                app.processArguments(new String[] {"(a+(b+c))/d", "a", "10", "b", "10", "c", "10", "d", "3"})
                        .map(WrappedValue::getExpression).orElse(null));
        Assertions.assertEquals("4",
                app.processArguments(new String[] {"(a+(b+c))/d", "a", "10", "b", "f", "c", "10", "d", "10", "f", "20"})
                        .map(WrappedValue::getExpression).orElse(null));
    }
}
