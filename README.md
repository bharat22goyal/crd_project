# Account Rebalancer

Computes how many shares to buy or sell per security so the account's
current allocation matches its target allocation.

## Problem

Given an account's total assets, and per-security target %, current %,
and unit price, work out the target variance and the number of shares
to buy (if underweight) or sell (if overweight) to bring variance to zero.

## Formula

```
target_variance   = current_pct - target_pct           (+ve = sell, -ve = buy)
dollar_diff        = (target_pct - current_pct) / 100 * total_asset
shares_to_buy_sell = dollar_diff / unit_price
```

Positive result: BUY. Negative result: SELL. Zero: HOLD.

## Question : What do you have to do to get to zero target variance?

Buy or sell shares so each security's dollar value matches its target %
of the $100,000 total. The dollar amount to trade is
`(target% - current%) / 100 * total_asset`, converted to shares by
dividing by the unit price.

| Security | Target % | Current % | Target Variance | Unit Price | Shares to Buy/Sell |
|---|---|---|---|---|---|
| IBM | 20 | 10 | -10 | 150 | Buy 66.67 |
| MSFT | 20 | 20 | 0 | 90 | 0 (hold) |
| ORCL | 20 | 30 | 10 | 220 | Sell 45.45 |
| AAPL | 20 | 20 | 0 | 450 | 0 (hold) |
| HD | 20 | 20 | 0 | 70 | 0 (hold) |

Buy ~67 shares of IBM, sell ~45 shares of ORCL. MSFT, AAPL, and HD are
already at target, no trade needed.

## Assumptions

- Fractional shares are allowed. The result is rounded to 2 decimal places
  instead of being floored to whole shares. If the broker only trades whole
  shares, floor/ceil the number before submitting the order (this leaves a
  bit of variance uncorrected until the next rebalance).
- `unitPrice` must be greater than 0. Zero or negative prices are rejected
  as invalid input.
- `totalAsset` must be zero or positive. A negative total asset is rejected.
- `targetPercent` and `currentPercent` must each be between 0 and 100. Values
  outside that range (e.g. -5 or 150) aren't meaningful for a single
  security's share of an account and are rejected.
- Target percentages should sum to 100 across the account, but that's not
  checked. Each security is computed on its own, so a bad account (targets
  that don't add up to 100) still returns a result per security instead of
  failing.

## Project layout

```
src/main/java/com/crd/rebalance/
  Position.java          input: one security's symbol, target%, current%, unit price
  RebalanceResult.java    output: one security's variance, shares to trade, action
  Rebalancer.java         core rebalancing logic

src/test/java/com/crd/rebalance/
  RebalancerTest.java     JUnit 5 test suite
  TestUtil.java           test-only helper for looking up a result by symbol

manual-test-cases.md      manual test case table, alongside the automated suite
```

## Build & test

Requires JDK 17+ and Maven.

```bash
mvn test
```
