package br.ufpe.cin.witup.jpf;

import gov.nasa.jpf.vm.Verify;

/**
 * JPF driver for concrete exploration of Account.debit() using Verify.getInt().
 * Explores (balance, value) pairs via IntChoiceGenerator.
 * For symbolic execution with path conditions, use AccountJPFDriverSymbolic + AccountTestSymbolic.jpf.
 */
public class AccountJPFDriver {

    public static void main(String[] args) {
        int balance = Verify.getInt(0, 100);
        int value = Verify.getInt(0, 100);

        try {
            Account account = new Account(balance);
            account.debit(value);
            // System.out.print("OK");
        } catch (RuntimeException e) {
            //System.out.print("EXCEPTION");
        }
    }
}
