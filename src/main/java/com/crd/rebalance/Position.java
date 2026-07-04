package com.crd.rebalance;

public class Position {

    private final String symbol;
    private final double targetPct;
    private final double currentPct;
    private final double unitPrice;

    public Position(String symbol, double targetPct, double currentPct, double unitPrice) {
        this.symbol = symbol;
        this.targetPct = targetPct;
        this.currentPct = currentPct;
        this.unitPrice = unitPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getTargetPct() {
        return targetPct;
    }

    public double getCurrentPct() {
        return currentPct;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
