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

        TestUtil.assertSharesAndAction(result, "IBM", 66.67, "BUY");
        TestUtil.assertSharesAndAction(result, "ORCL", -45.45, "SELL");
        TestUtil.assertSharesAndAction(result, "MSFT", 0, "HOLD");
        TestUtil.assertSharesAndAction(result, "AAPL", 0, "HOLD");
        TestUtil.assertSharesAndAction(result, "HD", 0, "HOLD");
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

        TestUtil.assertSharesAndAction(result, "A", 4000, "BUY");
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

        TestUtil.assertSharesAndAction(result, "A", 1000000, "BUY");
        TestUtil.assertSharesAndAction(result, "C", -681818.18, "SELL");
        TestUtil.assertSharesAndAction(result, "B", 0, "HOLD");
        TestUtil.assertSharesAndAction(result, "D", 0, "HOLD");
        TestUtil.assertSharesAndAction(result, "E", 0, "HOLD");
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

        TestUtil.assertSharesAndAction(result, "A", -2000, "SELL");
    }

    //Test 8 - decimal unit price
    @Test
    void nonRoundUnitPrice() {
        List<Position> positions = List.of(new Position("A", 20, 10, 137.53));
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        TestUtil.assertSharesAndAction(result, "A", 72.71, "BUY");
    }

    //Test 9 zero total asset
    @Test
    void zeroTotalAssetAccountNotYetVested() {
        List<Position> positions = List.of(new Position("A", 20, 0, 100));
        List<RebalanceResult> result = Rebalancer.rebalance(0, positions);

        TestUtil.assertSharesAndAction(result, "A", 0, "HOLD");
    }

    //Test 10 - very small variance that it round downs to zero
    @Test
    void varianceRoundsDownToZeroShares() {
        List<Position> positions = List.of(new Position("A", 20.0001, 20, 1000));
        List<RebalanceResult> result = Rebalancer.rebalance(100000, positions);

        TestUtil.assertSharesAndAction(result, "A", 0, "HOLD");
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
