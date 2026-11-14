package com.shubham.payguard.common;

/** Origination channel. BRANCH is attended; API is treated as unattended. */
public enum Channel {
    BRANCH,
    ONLINE,
    MOBILE,
    API
}
