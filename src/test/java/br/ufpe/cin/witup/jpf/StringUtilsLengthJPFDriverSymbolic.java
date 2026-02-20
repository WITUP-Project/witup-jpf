package br.ufpe.cin.witup.jpf;

/**
 * JPF driver for symbolic execution of StringUtils.requireLengthZero(length).
 * Uses int parameter - works with standard numeric symbolic execution (no string solver).
 * Demonstrates the same pattern: x = length, if (x > 0) throw.
 */
public class StringUtilsLengthJPFDriverSymbolic {

    public static void main(String[] args) {
        run(0);
    }

    /**
     * Entry point for symbolic execution. Parameter becomes symbolic when
     * symbolic.method=br.ufpe.cin.witup.jpf.StringUtilsLengthJPFDriverSymbolic.run(sym)
     */
    public static void run(int length) {
        StringUtils.requireLengthZero(length);
    }
}
