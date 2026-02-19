package br.ufpe.cin.witup.jpf;

public class Account {

    private double balance;

    public Account(double balance) {
        this.balance = balance;
    }

    public void debit(double value) throws RuntimeException {
        if (value > balance) {
            throw new RuntimeException("Insufficient balance");
        }
        balance -= value;
    }

    public boolean transfer(Account other, double value) {
        try {
            this.debit(value);
            other.balance += value;
            return true;
        }catch (RuntimeException e) {
            return false;
        }
    }
}
