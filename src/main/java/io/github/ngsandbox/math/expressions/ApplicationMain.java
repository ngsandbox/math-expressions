package io.github.ngsandbox.math.expressions;

import java.util.Optional;

import io.github.ngsandbox.math.expressions.utils.Color;
import io.github.ngsandbox.math.expressions.wrappers.WrappedValue;

public class ApplicationMain {

    public static void main(String... args) {
        ApplicationMain app = new ApplicationMain();
        app.processArguments(args)
                .ifPresent(w -> System.out.println(Color.YELLOW + "Result: " + w.getExpression() + Color.RESET));
    }

    public Optional<WrappedValue> processArguments(String[] args) {
        if (args.length == 0) {
            printHelp();
            return Optional.empty();
        }

        if (args.length > 1 && ((args.length - 1) % 2 != 0)) {
            System.out.println(Color.RED + "Count of variables with values has to be even!");
            printHelp();
            return Optional.empty();
        }

        Expression expression = new Expression(args[0]);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                String variable = args[i];
                String value = args[++i];
                try {
                    double dbl = Double.parseDouble(value);
                    expression.with(variable, dbl);
                } catch (NumberFormatException ex) {
                    expression.with(variable, value);
                }
            }
        }

        return Optional.of(expression.eval());
    }

    private static void printHelp() {
        System.out.println(Color.YELLOW + "Expression and/or list of variables with values are expected. Example:");
        System.out.println(Color.YELLOW + ">         \"(a+(b+c))/d\" a 10 b 20 c 30 d 40");
        System.out.print(Color.RESET);
    }

}
