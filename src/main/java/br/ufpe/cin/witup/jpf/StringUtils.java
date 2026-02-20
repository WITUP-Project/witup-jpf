package br.ufpe.cin.witup.jpf;

/**
 * String validation utilities for JPF symbolic execution examples.
 */
public class StringUtils {

    /**
     * Requires the argument to be an empty string.
     * Assigns argument.length() to a local variable x, then throws if x > 0.
     *
     * @param argument the string to validate
     * @throws RuntimeException if the string has length greater than 0
     */
    public static void requireEmpty(String argument) {
        int x = argument.length();
        if (x > 0) {
            throw new RuntimeException("String must be empty");
        }
    }

    /**
     * Validates that the given length (e.g. from argument.length()) is zero.
     * Same pattern as requireEmpty but takes int directly - works with standard
     * numeric symbolic execution (no string solver required).
     *
     * @param length the length value to validate (e.g. from someString.length())
     * @throws RuntimeException if length > 0
     */
    public static void requireLengthZero(int length) {
        int x = length;
        if (x > 0) {
            throw new RuntimeException("Length must be zero");
        }
    }
}
