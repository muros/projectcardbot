package com.comtrade.gcb.server.controller;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by muros on 22.8.2016.
 */
@Controller
@EnableAutoConfiguration
public class StripeController {

    @Autowired
    StripePaymentClient stripe;

    @RequestMapping(path="/stripe/pay", method= RequestMethod.POST)
    public String stripePay(@RequestParam("stripeToken") String stripeToken, Model model) {
        model.addAttribute("stripetoken", stripeToken);
        String chargeId = "";
        //chargeId = stripe.checkout(stripeToken, 2500, "usd", "$25");

        return "stripepayed";
    }


}
