package com.comtrade.gcb.client.gyft;

import com.comtrade.gcb.GiftCard;
import com.comtrade.gcb.data.jpa.Transaction;
import com.comtrade.gcb.data.jpa.TransactionType;
import com.comtrade.gcb.server.controller.AuthorizationClient;
import com.comtrade.gcb.server.controller.StripePaymentClient;
import com.comtrade.gcb.server.controller.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by muros on 1.8.2016.
 */
@Component
public class GyftClient {

    private static final Logger log = LoggerFactory.getLogger(GyftClient.class);

    @Autowired
    AuthorizationClient authClient;

    @Autowired
    StripePaymentClient stripe;

    @Autowired
    TransactionRepository transactionRepo;

    @Value("${gyft.host}")
    private String host;

    @Value("${gyft.root}")
    private String root;

    @Value("${gyft.apiKey}")
    private String apiKey;

    @Value("${gyft.apiSecret}")
    private String apiSecret;

    @Value("${gyft.services.host}")
    private String serviceHost;

    @Value("${gyft.services.redemption}")
    private String redemptionRoot;

    private RestTemplate restTemplate;

    public GyftClient() {
        restTemplate = new RestTemplate();
    }

    public boolean healthCheck() {
        final String method = "/health/check";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseEntity<String> response = restTemplate.getForEntity(methodURL, String.class);
        HttpStatus status = response.getStatusCode();
        if (HttpStatus.OK.equals(status)) {
            return true;
        } else {
            return false;
        }
    }

    public List<ShopCard> shopCards() {
        final String method = "/reseller/shop_cards";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<List<ShopCard>> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<ShopCard>>(){});

        return response.getBody();
    }

    public List<Detail_> merchants() {
        final String method = "/reseller/merchants";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<MerchantResp> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, MerchantResp.class);

        return response.getBody().getDetails();
    }

    public GiftCardDetail getCardDetails(String cardId) {
        final String method = "/reseller/merchants";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<MerchantResp> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, MerchantResp.class);
        GiftCardDetail gyftCardDetail = extractCardDetail(response.getBody().getDetails(), cardId);

        return gyftCardDetail;
    }

    public List<Detail> categories() {
        final String method = "/reseller/categories";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<CategoryResp> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, CategoryResp.class);

        return response.getBody().getDetails();
    }

    public GiftCard purchaseGyftCard(String recipientId,
                          String messageId,
                          String locale,
                          String cardId,
                          String paymentToken,
                          String recipientEmail,
                          String notes,
                          String firstName,
                          String lastName) {
        if (authClient.isUserAuthorized(locale)) {
            String cardIdOnly = "";
            String cardIdParts[] = cardId.split("-");
            if ((cardIdParts != null) && (cardIdParts.length == 2)) {
                cardIdOnly = cardIdParts[1];
            }
            GiftCard giftCard = purchaseCard(cardIdOnly, recipientEmail, "no-ref",
                    notes, firstName, lastName, null);
            List<ShopCard> cards = new ArrayList<>();
            cards = shopCards();

            ShopCard theCard = null;
            for (ShopCard card: cards) {
                if (card.getId().equals(cardIdOnly)) {
                    theCard = card;
                }
            }
            String chargeToken = "";
            if (giftCard != null) {
                String currency = "usd";
                Double price = theCard.getOpeningBalance();
                chargeToken = stripe.checkout(paymentToken, price.intValue() * 100, currency, "$" + price.intValue(),
                        messageId, recipientId);
            }

            GiftCardDetail cardDetails = getCardDetails(cardId);
            giftCard.setName(cardDetails.getMerchantName());
            giftCard.setCurrency(cardDetails.getCurrencyCode());
            giftCard.setPrice(new Double(cardDetails.getPriceAsString()));

            Transaction transaction = new Transaction();
            transaction.setRecipientId(recipientId);
            transaction.setMessageId(messageId);
            if (theCard != null) {
                transaction.setAmount( (int)(theCard.getOpeningBalance() * 100) );
            }
            transaction.setCardId(String.valueOf(cardId));
            transaction.setReferenceId(giftCard.getCardNumber());
            transaction.setType(TransactionType.PURCHASE);
            transaction.setCardKey(giftCard.getCardKey());
            transaction.setPaymentReference(chargeToken);
            transaction.setLocale(locale);
            transaction.setTimestamp(Calendar.getInstance().getTime());
            transactionRepo.save(transaction);

            return giftCard;
        } else {
            throw new RestClientException("User not authorized for gift card purchase.");
        }
    }

    public GiftCard purchaseCard(String cardId, String recipientEmail, String reselerRef, String notes, String firstName,
                             String lastName, Date birthDate) {
        final String method = "/partner/purchase/gift_card_direct";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createRootUrl(host, root, method);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder urlWithParams = new StringBuilder(methodURL);
        try {
            urlWithParams.append("shop_card_id=").append(cardId);
            urlWithParams.append("&to_email=").append(recipientEmail);
            urlWithParams.append("&reseller_reference=").append(reselerRef);
            urlWithParams.append("&notes=").append(notes);
            urlWithParams.append("&first_name=").append(firstName);
            urlWithParams.append("&last_name=").append(lastName);
            urlWithParams.append("&gender=").append("male");
            if (birthDate != null) {
                urlWithParams.append("&birthday=").append(
                        (new SimpleDateFormat("dd/MM/yyyy")).format(birthDate).toString());
            } else {
                urlWithParams.append("&birthday=").append("21/02/1972");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO fail!
        }
        String fullURL = null;
        try {
            fullURL = GyftUtil.appendCredentials(urlWithParams.toString(), apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO fail!
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<PurchasedCard> response = null;
        try {
            response = restTemplate.exchange(fullURL, HttpMethod.POST,
                    requestEntity, PurchasedCard.class);
        } catch (HttpClientErrorException hcee) {
            System.out.println(hcee.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            System.out.println(e.getCause());
            return null;
        }
        PurchasedCard purchasedCard = response.getBody();
        String cardUrl = purchasedCard.getUrl();
        String[] tokens = cardUrl.split("\\?c=");
        String cardKey = tokens[1];
        GiftCard giftCard = getPurchasedCardDetails(cardKey);

        return giftCard;
    }

    public Account getAccount() {
        final String method = "/reseller/account";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<Account> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, Account.class);

        return response.getBody();
    }

    public GiftCard getPurchasedCardDetails(String cardKey) {
        String cardURL = GyftUtil.createCardUrl(serviceHost, redemptionRoot, cardKey);
        ResponseEntity<Card> response = restTemplate.exchange(cardURL, HttpMethod.GET,
                null, Card.class);
        Card card = response.getBody();
        GiftCard giftCard = new GiftCard();
        giftCard.setCardNumber(card.getCardDetails().getCredentials().getCardNumber().getValue());
        giftCard.setPin(card.getCardDetails().getCredentials().getPin().getValue());
        giftCard.setBarcode(card.getCardDetails().getCredentials().getBarcode().getImageUrl());
        giftCard.setIcon(card.getCardDetails().getTheme().getIconImageUrl());
        giftCard.setImage(card.getCardDetails().getTheme().getCoverImageUrl());
        giftCard.setCardKey(cardKey);

        return giftCard;
    }

    private GiftCardDetail extractCardDetail(List<Detail_> details, String cardId) {
        GiftCardDetail giftCardDetail = new GiftCardDetail();

        if (details != null) {
            for (Detail_ detail: details) {
                List<ShopCard> shopCards = detail.getShopCards();
                ShopCard card = extractCardWithId(shopCards, cardId);
                if (card != null) {
                    giftCardDetail.setCardId(cardId);
                    giftCardDetail.setCurrencyCode(card.getCurrencyCode());
                    giftCardDetail.setPrice((int)(card.getPrice() * 100));
                    giftCardDetail.setMerchantName(detail.getName());
                    giftCardDetail.setMercnantDescription(detail.getDescription());
                    giftCardDetail.setMerchantIconUrl(detail.getIconUrl());
                    giftCardDetail.setMerchantCardImageUrl(detail.getCoverImageUrlHd());
                    break;
                }
            }
        }

        return giftCardDetail;
    }

    private ShopCard extractCardWithId(List<ShopCard> shopCards, String cardId) {
        ShopCard shopCard = null;
        String[] cardParts = cardId.split("-");

        if (shopCards != null) {
            for (ShopCard card: shopCards) {
                if (card.getId().equals(cardParts[1])) {
                    shopCard = card;
                    break;
                }
            }
        }

        return shopCard;
    }

    public List<Detail_> getCardsContainingText(String messageText) {
        final String method = "/reseller/merchants";
        String methodURL = null;
        String timestamp = GyftUtil.createTimestamp();
        try {
            methodURL = GyftUtil.createFullUrl(host, root, method, apiKey, apiSecret, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-sig-timestamp", timestamp);
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<MerchantResp> response = restTemplate.exchange(methodURL, HttpMethod.GET,
                requestEntity, MerchantResp.class);

        List<Detail_> merchants = new ArrayList<>();
        List<Detail_> details = response.getBody().getDetails();
        for (Detail_ merchant: details ) {
            String name = merchant.getName();
            if(name.toUpperCase().contains(messageText.toUpperCase())) {
                merchants.add(merchant);
            }
        }

        return merchants;
    }
}
