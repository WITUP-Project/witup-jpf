package br.ufpe.cin.witup.jpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MathTest {

    @Test
    public void testSqrtPositive() {
        double result = Math.sqrt(4);
        assertEquals(2.0, result, 1e-10);
    }

    @Test
    public void testSqrtThrowsWhenNonPositive() {
        assertThrows(RuntimeException.class, () -> Math.sqrt(0));
        assertThrows(RuntimeException.class, () -> Math.sqrt(-1));
    }

    @Test
    public void testSumPositive() {
        assertEquals(5, Math.sum(2, 3));
        assertEquals(0, Math.sum(0, 0));
    }

    @Test
    public void testSumThrowsWhenNegative() {
        assertThrows(RuntimeException.class, () -> Math.sum(-1, -1));
        assertThrows(RuntimeException.class, () -> Math.sum(-10, 5));
    }

    @Test
    public void testRequireBothNonNegativeOk() {
        Math.requireBothNonNegative(0, 0);
        Math.requireBothNonNegative(1, -1);
        Math.requireBothNonNegative(-1, 1);
    }

    @Test
    public void testRequireBothNonNegativeThrows() {
        assertThrows(RuntimeException.class, () -> Math.requireBothNonNegative(-1, -1));
    }

    @Test
    public void testRequireBothNonNegativeNestedOk() {
        Math.requireBothNonNegativeNested(0, 0);
        Math.requireBothNonNegativeNested(1, -1);
        Math.requireBothNonNegativeNested(-1, 1);
    }

    @Test
    public void testRequireBothNonNegativeNestedThrows() {
        assertThrows(RuntimeException.class, () -> Math.requireBothNonNegativeNested(-1, -1));
    }
}
