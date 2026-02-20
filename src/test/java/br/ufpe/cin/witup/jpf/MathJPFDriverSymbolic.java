package br.ufpe.cin.witup.jpf;

/**
 * JPF driver for symbolic execution of Math.sqrt().
 * Uses symbolic.method config to make run(value) parameter symbolic.
 * When run under jpf-symbc, value is symbolic; PCChoiceGenerator
 * captures path conditions (e.g. value <= 0) when exceptions occur.
 */
public class MathJPFDriverSymbolic {

    public static void main(String[] args) {
        run(1);
    }

    /**
     * Entry point for symbolic execution. Parameter becomes symbolic when
     * symbolic.method=br.ufpe.cin.witup.jpf.MathJPFDriverSymbolic.run(sym)
     */
    public static void run(int value) {
        Math.sqrt(value);
    }
}
