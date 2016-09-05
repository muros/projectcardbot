package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.client.gyft.CardDetails;
import com.comtrade.gcb.client.gyft.Detail_;
import com.comtrade.gcb.client.gyft.GyftClient;
import com.comtrade.messenger.send.Message;
import com.comtrade.messenger.webhook.Entry;
import com.comtrade.messenger.webhook.Messaging;
import com.comtrade.messenger.webhook.QuickReply;
import com.comtrade.messenger.webhook.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
@ComponentScan("com.comtrade.*")
public class ChatBotController {

    @Autowired
    private ChatBot chatBot;

    @Autowired
    GyftClient gyftClient;

    @RequestMapping(path="/messenger/webhook", method=RequestMethod.GET)
    public @ResponseBody String webhookGet(@RequestParam("hub.mode") String mode,
                                           @RequestParam("hub.verify_token") String verifyToken,
                                           @RequestParam("hub.challenge") String challenge) {
        // Verify token and return challenge

        return challenge;
    }

    @RequestMapping(path="/messenger/webhook", method= RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void webhookPost(@RequestBody Webhook data) {

        if ("page".equals(data.getObject())) {
            List<Entry> entries = data.getEntry();
            for (Entry pageEntry: entries) {
                String pageId = pageEntry.getId();
                Double time = pageEntry.getTime();
                List<Messaging> messaging = pageEntry.getMessaging();
                for (Messaging messagingEvent: messaging) {
                    if (messagingEvent.getOptin() != null) {
                        receivedAuthenitcation(messagingEvent);
                    } else if (messagingEvent.getMessage() != null) {
                        receivedMessage(messagingEvent);
                    } else if (messagingEvent.getPostback() != null) {
                        receivedPostback(messagingEvent);
                    } else if (messagingEvent.getDelivery() != null) {
                        receivedDeliveryConfirmation(messagingEvent);
                    } else {
                        System.out.println("Webhook received unknown messagingEvent " + messagingEvent);
                    }
                }
            }
        }
    }

    @RequestMapping(path="/messenger/test", method= RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void test(@RequestParam("recipientId") String recipientId,
                     @RequestParam("message") String message) {
        List<Detail_> merchants = gyftClient.getCardsContainingText(message);
        if ((merchants != null) && (merchants.size() > 0)) {
            chatBot.sendPurchaseCards(recipientId, merchants);
        }
    }

    private void receivedDeliveryConfirmation(Messaging messagingEvent) {
        List<String> mids = messagingEvent.getDelivery().getMids();
        if (mids != null) {
            for (String mid : mids) {
                System.out.println("Received delivery confirmation for " + mid);
            }
        }
    }

    private void receivedPostback(Messaging event) {
        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        Double timeOfMessage = event.getTimestamp();
        String payload = event.getPostback().getPayload();

        System.out.println("Received postback with payload " + payload);
        chatBot.sendPurchaseReply(senderId, "Proceed to checkout?", payload,  "yes", "no");
    }

    private void receivedMessage(Messaging event) {
        System.out.println("Received Message");

        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        Double timeOfMessage = event.getTimestamp();
        com.comtrade.messenger.webhook.Message message = event.getMessage();
        QuickReply qreply = message.getQuickReply();
        String payload = "";

        String messageText = message.getText();
        if ((messageText != null) && (messageText.toUpperCase().startsWith("BUY "))) {
            messageText = messageText.substring(3).trim();
            // TODO Ok, pojdi skozi listo in dodajaj v carussel kartice, ki imajo v imenu
            // string ki je v message Text, pazi na omejitev carussela - 20 ali nekaj.
            List<Detail_> merchants = gyftClient.getCardsContainingText(messageText);
            if ((merchants != null) && (merchants.size() > 0)) {
                chatBot.sendPurchaseCards(senderId, merchants);
            } else {
                chatBot.sendTextMessage(senderId, "Can not match a gift card with " + messageText + ".");
            }
        } else if (qreply != null) {
            payload = qreply.getPayload();
            chatBot.sendStripePay(senderId, payload);
        } else {
            chatBot.sendTextMessage(senderId, "Say buy and card name.");
        }
    }

    private void receivedAuthenitcation(Messaging event) {
        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        Double timeOfAuth = event.getTimestamp();

        System.out.println("Received authentication for user " + senderId);
        chatBot.sendTextMessage(senderId, "Authentication successful");
    }

}
