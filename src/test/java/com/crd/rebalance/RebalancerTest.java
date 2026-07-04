package com.crd.rebalance;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RebalancerTest {

    // Test 1: Mixed over/under/on-target portfolio
    @Test
    void mixedOverUnderOnTargetPortfolio() {
        List<Position> positions = List.of(
            new Position("IBM", 20, 10, 150),
            new Position("MSFT", 20, 20, 90),
            new Position("ORCL", 20, 30, 220),
            new Position("AAPL", 20, 20, 450),
            new Position("HD", 20, 20, 70)
        );

        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        assertEquals(66.67, TestUtil.findBySymbol(result, "IBM").getShares(), 0.01);
        assertEquals("BUY", TestUtil.findBySymbol(result, "IBM").getAction());

        assertEquals(-45.45, TestUtil.findBySymbol(result, "ORCL").getShares(), 0.01);
        assertEquals("SELL", TestUtil.findBySymbol(result, "ORCL").getAction());

        assertEquals(0.0, TestUtil.findBySymbol(result, "MSFT").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "MSFT").getAction());

        assertEquals(0.0, TestUtil.findBySymbol(result, "AAPL").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "AAPL").getAction());

        assertEquals(0.0, TestUtil.findBySymbol(result, "HD").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "HD").getAction());
    }

    //Test-2 : Current % already equals target % across portfolio
    @Test
    void currentPercentEqualsTargetPercentAcrossPortfolio() {
        List<Position> positions = List.of(
                new Position("X", 20, 20, 100),
                new Position("Y", 30, 30, 50),
                new Position("Z", 50, 50, 200)
        );
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);
        for (RebalanceResult r : result) {
            assertEquals(0, r.getShares());
            assertEquals("HOLD", r.getAction());
        }
    }

    //Test 3 One security holding 0% - it should buy
    @Test
    void OneShareHoldingZeroPercent() {
        List<Position> positions = List.of(new Position("X", 100, 0, 50));
        List<RebalanceResult> result = Rebalancer.rebalance(200000, positions);
        assertEquals(4000.0, result.get(0).getShares());
        assertEquals("BUY", result.get(0).getAction());
    }


}
