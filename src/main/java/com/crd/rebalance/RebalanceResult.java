package com.crd.rebalance;

public class RebalanceResult {

    private final String symbol;
    private final double targetVariance;
    private final double shares;
    private final String action;

    public RebalanceResult(String symbol, double targetVariance, double shares, String action) {
        this.symbol = symbol;
        this.targetVariance = targetVariance;
        this.shares = shares;
        this.action = action;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getShares() {
        return shares;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return symbol + ": variance=" + targetVariance + ", shares=" + shares + ", action=" + action;
    }
}
