package br.ufpe.cin.witup.jpf;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        try {
            Account account = new Account(initialBalance);
            if (!account.debit(100)) {
                throw new Exception("Debit operation failed");
            }
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }
}
