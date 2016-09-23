package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.client.gyft.Detail_;
import com.comtrade.gcb.client.gyft.GyftClient;
import com.comtrade.gcb.data.jpa.Message;
import com.comtrade.gcb.data.jpa.MessageType;
import com.comtrade.gcb.data.jpa.Transaction;
import com.comtrade.gcb.data.jpa.TransactionType;
import com.comtrade.messenger.webhook.Entry;
import com.comtrade.messenger.webhook.Messaging;
import com.comtrade.messenger.webhook.QuickReply;
import com.comtrade.messenger.webhook.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;

@Controller
@EnableAutoConfiguration
@ComponentScan("com.comtrade.*")
public class ChatBotController {

    private static final Logger log = LoggerFactory.getLogger(ChatBotController.class);

    private static final long MAX_DELAY_SEC = 10;

    @Autowired
    private ChatBot chatBot;

    @Autowired
    GyftClient gyftClient;

    @Autowired
    private MessengerLogger msgLogger;

    @RequestMapping(path="/messenger/webhook", method=RequestMethod.GET)
    public @ResponseBody String webhookGet(@RequestParam("hub.mode") String mode,
                                           @RequestParam("hub.verify_token") String verifyToken,
                                           @RequestParam("hub.challenge") String challenge) {
        // Verify token and return challenge
        log.info("Webhook GET with {}, {}, {}", mode, verifyToken, challenge);

        return challenge;
    }

    @RequestMapping(path="/messenger/webhook", method= RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void webhookPost(@RequestBody Webhook data) {

        if ("page".equals(data.getObject())) {
            log.info("Message that is 'page'");
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
        } else {
            log.info("Message that is not 'page'");
            log.info("Webhook data:\n" + data.getObject());
        }
    }

    // TODO remove Local tests only
    @RequestMapping(path="/messenger/test", method= RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void test(@RequestParam("recipientId") String recipientId,
                     @RequestParam("message") String message) {

        log.info("Messenger test method called.");

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
        Message lastMsg = msgLogger.findLastLogMessageForRecipient(recipientId, MAX_DELAY_SEC);
        if ((lastMsg == null)
                || ((lastMsg != null) && (MessageType.INITIAL.equals(lastMsg.getMessageType())))
                || ((lastMsg != null) && (MessageType.SHOW_CARDS.equals(lastMsg.getMessageType())))
                ) {
            if ((messageText != null) && (messageText.toUpperCase().startsWith("BUY "))) {
                messageText = messageText.substring(3).trim();
                List<Detail_> merchants = gyftClient.getCardsContainingText(messageText);
                if ((merchants != null) && (merchants.size() > 0)) {
                    chatBot.sendPurchaseCards(senderId, merchants);
                } else {
                    chatBot.sendTextMessage(MessageType.INITIAL, senderId, "Can not match a gift card with " + messageText + ".");
                }
            } else {
                chatBot.sendTextMessage(MessageType.INITIAL, senderId, "Say buy and card name.");
            }
        } else if (qreply != null) {
            payload = qreply.getPayload();
            if (payload.startsWith("merchant")) {
                //cardsWithValues = gyftClient.getCardValuesForMerchant(payload);
                chatBot.sendCardValues(senderId, payload);
            } else if (payload.startsWith("card")) {
                chatBot.sendStripePay(senderId, payload);
            }
        } else {
        chatBot.sendTextMessage(MessageType.INITIAL, senderId, "Say buy and card name.");
    }


    // TODO refactor this
    }

    private void receivedAuthenitcation(Messaging event) {
        String senderId = event.getSender().getId();
        String recipientId = event.getRecipient().getId();
        Double timeOfAuth = event.getTimestamp();

        System.out.println("Received authentication for user " + senderId);
        chatBot.sendTextMessage(MessageType.INITIAL, senderId, "Authentication successful");
    }
}
