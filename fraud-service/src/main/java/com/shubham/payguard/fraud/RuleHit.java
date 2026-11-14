package com.shubham.payguard.fraud;

/** A rule that fired, with the risk points it contributes. */
public record RuleHit(String rule, int score) {
}
