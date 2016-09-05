package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.GiftCard;
import com.comtrade.gcb.client.gyft.GiftCardDetail;
import com.comtrade.gcb.client.gyft.GyftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by muros on 22.8.2016.
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

    @RequestMapping(path="/stripe/pay", method= RequestMethod.POST)
    public String stripePay(@RequestParam("stripeToken") String stripeToken,
                            @RequestParam("recipientId") String recipientId,
                            @RequestParam("cardId") String cardId,
                            Model model) {
        model.addAttribute("stripeToken", stripeToken);
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("cardId", cardId);

        // TODO Provide FB messenger message id of payment message. Is that useful and necessary?
        String messageId = "NA";
        // TODO Get locale form user in messenger
        String locale = "en_US";
        // TODO are these necessary if yes collect them in messenger
        String recipientEmail = "some@one.com";
        String notes = "NA";
        String firstName = "John";
        String lastName = "Doe";
        GiftCard giftCard = gyftClient.purchaseGyftCard(recipientId, messageId, locale, cardId, stripeToken,
                recipientEmail, notes, firstName, lastName);

        chatBot.sendPayedCard(recipientId, giftCard);

        return "redirect:" + "https://www.messenger.com/closeWindow/?image_url=http://ec2.urkei.com:9091/v1/giftbot/logo.png&display_text=Thank you for your purchase.";
    }

    @RequestMapping(path = "/stripe/card", method = RequestMethod.GET)
    public String stripeCard(@RequestParam("cardId") String cardId,
                             @RequestParam("recipientId") String recipientId,
                             Model model) {

        GiftCardDetail cardDetail = gyftClient.getCardDetails(cardId);

        model.addAttribute("cardImage", cardDetail.getMerchantCardImageUrl());
        model.addAttribute("cardDescription", cardDetail.getMerchantName());
        model.addAttribute("cardValue", cardDetail.getCurrencyCode() + " " + (cardDetail.getPrice() / 100));
        model.addAttribute("merchantLogo", cardDetail.getMerchantIconUrl());
        model.addAttribute("cardAmount", String.valueOf(cardDetail.getPrice()));
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("cardId", cardId);

        return "stripecard";
    }

}
