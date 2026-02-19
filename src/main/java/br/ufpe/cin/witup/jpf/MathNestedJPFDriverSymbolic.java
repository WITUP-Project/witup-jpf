package br.ufpe.cin.witup.jpf;

/**
 * JPF driver for symbolic execution of Math.requireBothNonNegativeNested(a, b).
 * Uses symbolic.method config to make run(a, b) parameters symbolic.
 * When run under jpf-symbc, a and b are symbolic; PCChoiceGenerator
 * captures path conditions (e.g. a < 0 && b < 0) when exceptions occur.
 */
public class MathNestedJPFDriverSymbolic {

    public static void main(String[] args) {
        run(0, 0);
    }

    /**
     * Entry point for symbolic execution. Parameters become symbolic when
     * symbolic.method=br.ufpe.cin.witup.jpf.MathNestedJPFDriverSymbolic.run(sym#sym)
     */
    public static void run(int a, int b) {
        Math.requireBothNonNegativeNested(a, b);
    }
}
