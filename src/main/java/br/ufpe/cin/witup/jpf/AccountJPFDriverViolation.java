package br.ufpe.cin.witup.jpf;

import gov.nasa.jpf.vm.Verify;

/**
 * Same as AccountJPFDriver (concrete via Verify.getInt) but lets the exception propagate.
 * JPF reports it as a property violation when debit(value) is called with value > balance.
 * For symbolic execution, use AccountJPFDriverSymbolic + AccountTestSymbolic.jpf.
 */
public class AccountJPFDriverViolation {

    public static void main(String[] args) {
        int balance = Verify.getInt(0, 100);
        int value = Verify.getInt(0, 100);

        Account account = new Account(balance);
        account.debit(value);  // throws RuntimeException when value > balance
        System.out.print("OK");
    }
}
