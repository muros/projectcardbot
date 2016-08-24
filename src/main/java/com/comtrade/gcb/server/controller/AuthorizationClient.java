package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.Country;
import com.comtrade.gcb.data.jpa.GiftProvider;
import com.comtrade.gcb.data.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by muros on 12.8.2016.
 */
@Component
public class AuthorizationClient {

    @Autowired
    UserRepository userRepo;

    @Autowired
    ProviderRepository providerRepo;

    public boolean isUserAuthorized(String locale) {
        if ("en_US".equals(locale)) {
            return true;
        } else {
            return false;
        }
    }
}
