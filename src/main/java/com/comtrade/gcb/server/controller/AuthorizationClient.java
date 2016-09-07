package com.comtrade.gcb.server.controller;

import org.springframework.stereotype.Component;

/**
 * Simple authorization based on locale.
 * Fixed to US only! Dummy implementation.
 */
@Component
public class AuthorizationClient {

    public static final String FIX_LOCALE = "en_US";

    public boolean isUserAuthorized(String locale) {
        if (FIX_LOCALE.equals(locale)) {
            return true;
        } else {
            return false;
        }
    }
}
