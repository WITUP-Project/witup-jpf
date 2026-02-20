package br.ufpe.cin.witup.jpf;

/**
 * JPF driver for symbolic execution of Account.debit().
 * Uses symbolic.method config to make run(balance, value) parameters symbolic.
 * When run under jpf-symbc, balance and value are symbolic; PCChoiceGenerator
 * captures path conditions (e.g. value > balance) when exceptions occur.
 */
public class AccountJPFDriverSymbolic {

    public static void main(String[] args) {
        run(0.0, 0.0);
    }

    /**
     * Entry point for symbolic execution. Parameters become symbolic when
     * symbolic.method=br.ufpe.cin.witup.jpf.AccountJPFDriverSymbolic.run(sym#sym)
     */
    public static void run(double balance, double value) {
        Account account = new Account(balance);
        account.debit(value);
    }
}
