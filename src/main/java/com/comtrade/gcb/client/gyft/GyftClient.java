package com.comtrade.gcb.client.gyft;

import com.comtrade.gcb.Gender;
import com.comtrade.gcb.GiftCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by muros on 1.8.2016.
 */
@Component
public class GyftClient {

    private static final Logger log = LoggerFactory.getLogger(GyftClient.class);

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

    public GiftCard purchaseCard(String cardId, String recipientEmail, String reselerRef, String notes, String firstName,
                             String lastName, Gender gender, Date birthDate) {
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
            if (gender != null) {
                urlWithParams.append("&gender=").append(gender.toString());
            } else {
                urlWithParams.append("&gender=").append("male");
            }
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
        GiftCard giftCard = getCardDetails(cardKey);

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

    public GiftCard getCardDetails(String cardKey) {
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
}
