package br.ufpe.cin.witup.jpf;

/**
 * JPF driver for symbolic execution of StringUtils.requireEmpty(argument).
 * Uses symbolic.method config to make run(argument) parameter symbolic.
 * When run under jpf-symbc with string support, argument is symbolic;
 * PCChoiceGenerator captures path conditions (e.g. length > 0) when exceptions occur.
 */
public class StringUtilsJPFDriverSymbolic {

    public static void main(String[] args) {
        run("");
    }

    /**
     * Entry point for symbolic execution. Parameter becomes symbolic when
     * symbolic.method=br.ufpe.cin.witup.jpf.StringUtilsJPFDriverSymbolic.run(sym)
     */
    public static void run(String argument) {
        StringUtils.requireEmpty(argument);
    }
}
