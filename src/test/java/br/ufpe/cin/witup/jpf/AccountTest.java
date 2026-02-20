package br.ufpe.cin.witup.jpf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AccountTest {

    private double initialBalance = 100;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testDebitOperation() {
        Account account = new Account(initialBalance);
        assertDoesNotThrow(() -> account.debit(50));
        assertTrue(account.transfer(new Account(0), 50));
    }
}
