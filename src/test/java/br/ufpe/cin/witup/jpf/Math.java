package br.ufpe.cin.witup.jpf;

public class Math {

    /**
     * Returns the square root of the given value.
     *
     * @param value a non-negative integer
     * @return the square root of value
     * @throws RuntimeException if value is non-positive (value <= 0)
     */
    public static double sqrt(int value) {
        if (value <= 0) {
            throw new RuntimeException("Value must be positive");
        }
        return java.lang.Math.sqrt(value);
    }

    /**
     * Returns the sum of two integers.
     *
     * @param a first operand
     * @param b second operand
     * @return a + b
     * @throws RuntimeException if the sum is negative (a + b < 0)
     */
    public static int sum(int a, int b) {
        if (a + b < 0) {
            throw new RuntimeException("Sum must be non-negative");
        }
        return a + b;
    }

    /**
     * Validates that at least one of a or b is non-negative.
     * Uses a single if with conjunction: if (a < 0 && b < 0) throw.
     *
     * @param a first value
     * @param b second value
     * @throws RuntimeException if both a and b are negative
     */
    public static void requireBothNonNegative(int a, int b) {
        if (a < 0 && b < 0) {
            throw new RuntimeException("Both values must be non-negative");
        }
    }

    /**
     * Validates that at least one of a or b is non-negative.
     * Uses two nested ifs: if (a < 0) { if (b < 0) { throw } }.
     *
     * @param a first value
     * @param b second value
     * @throws RuntimeException if both a and b are negative
     */
    public static void requireBothNonNegativeNested(int a, int b) {
        if (a < 0) {
            if (b < 0) {
                throw new RuntimeException("Both values must be non-negative");
            }
        }
    }
}
