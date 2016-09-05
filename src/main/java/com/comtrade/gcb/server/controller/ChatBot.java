package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.GiftCard;
import com.comtrade.gcb.client.gyft.Detail_;
import com.comtrade.gcb.client.gyft.ShopCard;
import com.comtrade.messenger.send.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by muros on 29.8.2016.
 */
@Component
public class ChatBot extends MessengerBot{

    private static final int MAX_CARDS = 3;
    private static final int MAX_MERCHANTS = 10;

    public String sendPurchaseCard(String recipientId, String providerId) {

        Payload payload = new Payload();
        payload.setTemplateType("generic");
        List<Element> elements = new ArrayList<>();
        Element element = new Element();
        element.setTitle("StarbucksCard");
        element.setSubtitle("Gift card");
        element.setImageUrl("http://imagestest.gyft.com/merchants_cards/c-508-1346844985758-20_cover_hd.png");
        List<Button> buttons = new ArrayList<>();
        Button button = null;
        button = new Button();
        button.setType("postback");
        button.setTitle("$50");
        button.setPayload("gyft-4698");
        buttons.add(button);
        button = new Button();
        button.setType("postback");
        button.setTitle("$80");
        button.setPayload("gyft-4614");
        buttons.add(button);
        element.setButtons(buttons);
        elements.add(element);
        payload.setElements(elements);

        String mid = sendGenericTemplate(recipientId, payload);

        return mid;
    }

    public String sendPurchaseCards(String recipientId, List<Detail_> merchants) {

        Payload payload = new Payload();
        payload.setTemplateType("generic");
        List<Element> elements = new ArrayList<>();
        int merCnt = 0;
        for (Detail_ merchant: merchants) {
            Element element = new Element();
            element.setTitle(merchant.getName());
            element.setSubtitle("Gift card");
            element.setImageUrl(merchant.getCoverImageUrlHd());
            List<ShopCard> cards = merchant.getShopCards();
            if ((cards != null) && (cards.size() > 0)) {
                List<Button> buttons = new ArrayList<>();
                int cardCnt = 0;
                for (ShopCard card: cards) {
                    Button button = new Button();
                    button.setType("postback");
                    button.setTitle(card.getCurrencyCode() + " " + card.getPrice());
                    button.setPayload("gyft-" + card.getId());
                    buttons.add(button);
                    if (++cardCnt >= MAX_CARDS) {
                        break;
                    }
                }
                element.setButtons(buttons);
            }
            elements.add(element);
            if (++merCnt >= MAX_MERCHANTS) {
                break;
            }
        }
        payload.setElements(elements);

        String mid = sendGenericTemplate(recipientId, payload);

        return mid;
    }

    public String sendPurchaseReply(String recipientId, String text, String payload, String... options) {

        Message message = new Message();
        message.setText(text);
        List<QuickReply> replies = new ArrayList<>();
        for (String option: options) {
            QuickReply reply = new QuickReply();
            reply.setContentType("text");
            reply.setTitle(option);
            reply.setPayload(payload);
            replies.add(reply);
        }
        message.setQuickReplies(replies);

        String mid = sendQuickReply(recipientId, message);

        return mid;
    }

    public String sendStripePay(String recipientId, String cardId) {

        Payload payload = new Payload();
        payload.setTemplateType("generic");
        List<Element> elements = new ArrayList<>();
        Element element = new Element();
        element.setTitle("Stripe");
        element.setSubtitle("Gateway");
        element.setImageUrl("http://ec2.urkei.com:9091/v1/giftbot/stripe.png");
        List<Button> buttons = new ArrayList<>();
        Button button = null;
        button = new Button();
        button.setType("web_url");
        button.setTitle("Chose this one");
        button.setUrl("http://ec2.urkei.com:9091/v1/giftbot/stripe/card?cardId="+cardId+"&recipientId="+recipientId);
        buttons.add(button);
        element.setButtons(buttons);
        elements.add(element);
        payload.setElements(elements);

        String mid = sendGenericTemplate(recipientId, payload);

        return mid;
    }

    public String sendPayedCard(String recipientId, GiftCard giftCard) {

        Payload payload = new Payload();
        payload.setTemplateType("generic");
        List<Element> elements = new ArrayList<>();
        Element element = new Element();
        element.setTitle(giftCard.getName() + " " +  giftCard.getCurrency() + " " + giftCard.getPrice());
        element.setSubtitle(giftCard.getCardNumber() + " PIN " + giftCard.getPin());
        element.setImageUrl(giftCard.getImage());
        element.setButtons(null);
        elements.add(element);
        payload.setElements(elements);

        String mid = sendGenericTemplate(recipientId, payload);

        return mid;
    }
}
