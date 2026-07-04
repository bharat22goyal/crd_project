package com.crd.rebalance;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {

    public static RebalanceResult findBySymbol(List<RebalanceResult> list, String symbol) {
        for (RebalanceResult r : list) {
            if (r.getSymbol().equals(symbol)) {
                return r;
            }
        }
        throw new NoSuchElementException("No result found for symbol: " + symbol);
    }

    public static void assertSharesAndAction(List<RebalanceResult> list, String symbol, double expectedShares, String expectedAction) {
        RebalanceResult r = findBySymbol(list, symbol);
        assertEquals(expectedShares, r.getShares(), 0.01);
        assertEquals(expectedAction, r.getAction());
    }
}
