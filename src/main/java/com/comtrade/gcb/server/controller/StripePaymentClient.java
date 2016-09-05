package com.comtrade.gcb.server.controller;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by muros on 23.8.2016.
 */
@Component
public class StripePaymentClient {

    @Value("${stripe.apiKey}")
    private String apiKey;

    public String pay() {
        String stripeToken = "";

        return stripeToken;
    }

    public String checkout(String stripeToken, int amount, String currency, String description,
                           String messageId, String recipientId) {
        String chargeId = "";
        // Set your secret key: remember to change this to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/account/apikeys
        Stripe.apiKey = apiKey;

        // Create a charge: this will charge the user's card
        try {
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", amount);
            chargeParams.put("currency", currency);
            chargeParams.put("source", stripeToken);
            chargeParams.put("description", description);
            Map<String, String> initialMetadata = new HashMap<String, String>();
            initialMetadata.put("message_id", messageId);
            initialMetadata.put("recipient_id", recipientId);
            chargeParams.put("metadata", initialMetadata);

            Charge charge = Charge.create(chargeParams);
            chargeId = charge.getId();
        } catch (CardException | APIException | InvalidRequestException |
                APIConnectionException | AuthenticationException e) {
            e.printStackTrace();
        }
        return chargeId;
    }

}
