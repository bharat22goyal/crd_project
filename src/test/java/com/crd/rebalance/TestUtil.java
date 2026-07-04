package com.crd.rebalance;

import java.util.List;
import java.util.NoSuchElementException;

public class TestUtil {

    public static RebalanceResult findBySymbol(List<RebalanceResult> list, String symbol) {
        for (RebalanceResult r : list) {
            if (r.getSymbol().equals(symbol)) {
                return r;
            }
        }
        throw new NoSuchElementException("No result found for symbol: " + symbol);
    }
}
