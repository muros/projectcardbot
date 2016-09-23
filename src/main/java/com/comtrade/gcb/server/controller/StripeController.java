package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.GiftCard;
import com.comtrade.gcb.client.gyft.GiftCardDetail;
import com.comtrade.gcb.client.gyft.GyftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Stripe payment functionality.
 * Provide data for payment form and execute payment checkout.
 */
@Controller
@EnableAutoConfiguration
public class StripeController {

    @Autowired
    StripePaymentClient stripe;

    @Autowired
    GyftClient gyftClient;

    @Autowired
    private ChatBot chatBot;

    @Value("${universal.secret}")
    private String universalSecret;

    @RequestMapping(path="/stripe/pay", method= RequestMethod.POST)
    public String stripePay(@RequestParam("stripeToken") String stripeToken,
                            @RequestParam("uuid") String uuid,
                            @RequestParam("cardId") String cardId,
                            @RequestParam("userId") String userId,
                            Model model) {

        model.addAttribute("stripeToken", stripeToken);
        model.addAttribute("uuid", uuid);
        model.addAttribute("cardId", cardId);
        model.addAttribute("userId", userId);

        // TODO Provide FB messenger message id of payment message. Is that useful and necessary?
        String messageId = "NA";
        // TODO Get locale form user in messenger
        String locale = "en_US";
        // TODO are these necessary if yes collect them in messenger
        String recipientEmail = "some@one.com";
        String notes = "NA";
        String firstName = "John";
        String lastName = "Doe";
        GiftCard giftCard = gyftClient.purchaseGyftCard(userId, uuid, messageId, locale, cardId, stripeToken,
                recipientEmail, notes, firstName, lastName);

        // I am not sending the card, user will pull for the card with uuid that she generated
        //chatBot.sendPayedCard(recipientId, giftCard);

        return "redirect:" + "https://www.messenger.com/closeWindow/?image_url=http://ec2.urkei.com:9091/v1/giftbot/logo.png&display_text=Thank you for your purchase.";
    }

    @RequestMapping(path = "/stripe/card", method = RequestMethod.GET)
    public String stripeCard(@RequestParam ("secret") String secret,
                             @RequestParam("cardId") String cardId,
                             @RequestParam("uuid") String uuid,
                             @RequestParam("userId") String userId,
                             Model model) {

        if ((secret == null) || (!secret.equals(universalSecret))) {
            throw new ForbiddenException();
        }

        GiftCardDetail cardDetail = gyftClient.getCardDetails(cardId);

        model.addAttribute("cardImage", cardDetail.getMerchantCardImageUrl());
        model.addAttribute("cardDescription", cardDetail.getMerchantName());
        model.addAttribute("cardValue", cardDetail.getCurrencyCode() + " " + (cardDetail.getPrice() / 100));
        model.addAttribute("merchantLogo", cardDetail.getMerchantIconUrl());
        model.addAttribute("cardAmount", String.valueOf(cardDetail.getPrice()));
        model.addAttribute("uuid", uuid);
        model.addAttribute("cardId", cardId);
        model.addAttribute("userId", userId);

        return "stripecard";
    }

}
