package io.github.ngsandbox.math.expressions;

public final class ExpressionConstants {

    /**
     * Or operator priority: ||
     */
    public static final int OPERATOR_PRIORITY_OR = 2;

    /**
     * And operator priority: &&
     */
    public static final int OPERATOR_PRIORITY_AND = 4;

    /**
     * Equality operators priority: ==, !=. <>
     */
    public static final int OPERATOR_PRIORITY_EQUALITY = 7;

    /**
     * Comparative operators priority: <,>,<=,>=
     */
    public static final int OPERATOR_PRIORITY_COMPARISON = 10;

    /**
     * Additive operators priority: + and -
     */
    public static final int OPERATOR_PRIORITY_ADDITIVE = 20;

    /**
     * Multiplicative operators priority: *,/,%
     */
    public static final int OPERATOR_PRIORITY_MULTIPLICATIVE = 30;

    /**
     * Power operator priority: ^
     */
    public static final int OPERATOR_PRIORITY_POWER = 40;

    /**
     * Unary operators priority: + and - as prefix
     */
    public static final int OPERATOR_PRIORITY_UNARY = 60;

    /**
     * Exception message for missing operators.
     */
    public static final String MISSING_PARAMETERS_FOR_OPERATOR = "Missing parameter(s) for operator ";
    /**
     * What character to use for decimal separators.
     */
    public static final char DECIMAL_SEPARATOR = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    public static final char MINUS_SIGN = '-';
    /**
     * What character to use for plus sign (positive values).
     */
    public static final char PLUS_SIGN = '+';

    public static final String ARG_NULL_ERROR = " argument must be numeric, but ";
    public static final String FIRST_ARG_NULL_ERROR = "First argument must be numeric, but ";
    public static final String SECOND_ARG_NULL_ERROR = "Second argument must be numeric, but ";

    /**
     * NULL constant
     */
    public static final String NULL_CONST = "NULL";

    /**
     * TRUE constant
     */
    public static final String TRUE_CONST = "TRUE";

    /**
     * FALSE constant
     */
    public static final String FALSE_CONST = "FALSE";

    /**
     * IF function
     */
    public static final String IF_FUNC = "IF";
}
