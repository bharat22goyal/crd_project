package com.crd.rebalance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                new Position("A", 20, 20, 100),
                new Position("B", 30, 30, 50),
                new Position("C", 50, 50, 200)
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
        List<Position> positions = List.of(new Position("A", 100, 0, 50));
        List<RebalanceResult> result = Rebalancer.rebalance(200000, positions);

        assertEquals(4000.0, TestUtil.findBySymbol(result, "A").getShares());
        assertEquals("BUY", TestUtil.findBySymbol(result, "A").getAction());
    }

    //Test 4 : Large total asset value across a mixed over/under/on-target portfolio
    @Test
    void largeTotalAssetValueAcrossPortfolio() {
        List<Position> positions = List.of(
                new Position("A", 30, 15, 150),
                new Position("B", 10, 10, 90),
                new Position("C", 15, 30, 220),
                new Position("D", 25, 25, 450),
                new Position("E", 20, 20, 70)
        );

        List<RebalanceResult> result = Rebalancer.rebalance(1000000000, positions);

        assertEquals(1000000, TestUtil.findBySymbol(result, "A").getShares(), 0.01);
        assertEquals("BUY", TestUtil.findBySymbol(result, "A").getAction());

        assertEquals(-681818.18, TestUtil.findBySymbol(result, "C").getShares(), 0.01);
        assertEquals("SELL", TestUtil.findBySymbol(result, "C").getAction());

        assertEquals(0, TestUtil.findBySymbol(result, "B").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "B").getAction());

        assertEquals(0, TestUtil.findBySymbol(result, "D").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "D").getAction());

        assertEquals(0, TestUtil.findBySymbol(result, "E").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "E").getAction());
    }

    // Test 5 and 6 Zero/negative unit price must be rejected
    @ParameterizedTest
    @ValueSource(doubles = {0, -10})
    void zeroOrNegativeUnitPriceMustBeRejected(double price) {
        List<Position> positions = List.of(new Position("A", 20, 10, price));

        assertThrows(IllegalArgumentException.class,
                () -> Rebalancer.rebalance(100000, positions));
    }

    //Test 7 - current position 100 percent but taget is 0
    @Test
    void currentPosition100PercentButTargetIsZero() {
        List<Position> positions = List.of(new Position("A", 0, 100, 50));
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        assertEquals(-2000.0, TestUtil.findBySymbol(result, "A").getShares());
        assertEquals("SELL", TestUtil.findBySymbol(result, "A").getAction());
    }

    //Test 8 - decimal unit price
    @Test
    void nonRoundUnitPrice() {
        List<Position> positions = List.of(new Position("A", 20, 10, 137.53));
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        assertEquals(72.71, TestUtil.findBySymbol(result, "A").getShares() , 0.01);
        assertEquals("BUY", TestUtil.findBySymbol(result, "A").getAction());
    }

    //Test 9 zero total asset
    @Test
    void zeroTotalAssetAccountNotYetVested() {
        List<Position> positions = List.of(new Position("A", 20, 0, 100));
        List<RebalanceResult> result = Rebalancer.rebalance(0, positions);

        assertEquals(0, TestUtil.findBySymbol(result, "A").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "A").getAction());
    }

    //Test 10 - very small variance that it round downs to zero
    @Test
    void varianceRoundsDownToZeroShares() {
        List<Position> positions = List.of(new Position("A", 20.0001, 20, 1000));
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        assertEquals(0.0, TestUtil.findBySymbol(result, "A").getShares());
        assertEquals("HOLD", TestUtil.findBySymbol(result, "A").getAction());
    }

    //Test 11 - Multiple valid securities plus one with an invalid price
    @Test
    void multipleValidSecuritiesPlusOneInvalidPrice() {
        List<Position> positions = List.of(
                new Position("A", 20, 10, 150),
                new Position("B", 20, 20, 90),
                new Position("C", 20, 10, 0)
        );

        assertThrows(IllegalArgumentException.class,
                () -> Rebalancer.rebalance(100000, positions));
    }

    //Test 12 - total asset is negative
    @Test
    void negativeTotalAsset() {
        List<Position> positions = List.of(new Position("A", 20, 10, 100));
        assertThrows(IllegalArgumentException.class,
                () -> Rebalancer.rebalance(-100000, positions));
    }

    //Test 13 - target percent outside 0 to 100
    @ParameterizedTest
    @ValueSource(doubles = {-5, 150})
    void targetPercentOutsideZeroTo100(double targetPct) {
        List<Position> positions = List.of(new Position("A", targetPct, 10, 100));
        assertThrows(IllegalArgumentException.class,
                () -> Rebalancer.rebalance(100000, positions));
    }

    //Test 14 - current percent outside 0 to 100
    @ParameterizedTest
    @ValueSource(doubles = {-5, 150})
    void currentPercentOutsideZeroTo100(double currentPct) {
        List<Position> positions = List.of(new Position("A", 20, currentPct, 100));
        assertThrows(IllegalArgumentException.class,
                () -> Rebalancer.rebalance(100000, positions));
    }

    //test 15 - empty list
    @Test
    void emptyPositionList() {
        List<RebalanceResult> result = Rebalancer.rebalance(100000, List.of());
        assertEquals(0, result.size());
    }
}
