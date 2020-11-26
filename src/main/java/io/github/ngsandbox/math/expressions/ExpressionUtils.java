package io.github.ngsandbox.math.expressions;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static io.github.ngsandbox.math.expressions.wrappers.WrappedNull.WRAPPED_NULL;

@Slf4j
public final class ExpressionUtils {
    public static <T> T getByIndex(List<T> list, int index) {
        if (list == null || list.size() < index + 1) {
            throw new ExpressionException("The size of parameters less than " + index);
        }

        T result = list.get(index);
        if (result == null) {
            throw new ExpressionException("Index parameter [" + index + "] may not be null");
        }

        return result;
    }

    public static <T> boolean isNull(T v1) {
        return v1 == null || WRAPPED_NULL.equals(v1);
    }

    /**
     * Is the string a number?
     *
     * @param str The string.
     * @return <code>true</code>, if the input string is a number.
     */
    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.chars().allMatch(Character::isDigit);
    }
}
