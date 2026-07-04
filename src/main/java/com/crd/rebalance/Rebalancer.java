package com.crd.rebalance;

import java.util.ArrayList;
import java.util.List;

public class Rebalancer {

    public static List<RebalanceResult> rebalance(double totalAsset, List<Position> positions) {
        if (totalAsset < 0) {
            throw new IllegalArgumentException("Invalid total asset: " + totalAsset);
        }

        List<RebalanceResult> results = new ArrayList<>();

        for (Position p : positions) {
            if (p.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Invalid unit price for " + p.getSymbol());
            }
            if (p.getTargetPct() < 0 || p.getTargetPct() > 100 || p.getCurrentPct() < 0 || p.getCurrentPct() > 100) {
                throw new IllegalArgumentException("Invalid target/current percentage for " + p.getSymbol());
            }

            double variance = p.getCurrentPct() - p.getTargetPct();

            double dollarDiff = (p.getTargetPct() - p.getCurrentPct()) / 100.0 * totalAsset;

            double shares = round2(dollarDiff / p.getUnitPrice());

            String action;
            if (shares == 0) {
                action = "HOLD";
            } else if (shares > 0) {
                action = "BUY";
            } else {
                action = "SELL";
            }

            results.add(new RebalanceResult(p.getSymbol(), variance, shares, action));
        }

        return results;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
