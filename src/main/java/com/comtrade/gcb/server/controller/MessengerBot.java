package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.MessageType;
import com.comtrade.messenger.send.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by muros on 29.8.2016.
 */
@Component
public class MessengerBot {

    @Value("${messenger.bot.appSecret}")
    private String appSecret;

    @Value("${messenger.bot.validationToken}")
    private String validationToken;

    @Value("${messenger.bot.pageAccessToken}")
    private String pageAccessToken;

    @Value("${messenger.bot.serverUrl}")
    private String serverUrl;

    @Value("${messenger.bot.graphApi}")
    private String graphApi;

    @Autowired
    private MessengerLogger msgLogger;

    private RestTemplate restTemplate;

    public MessengerBot() {
        restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor ri = new LoggingRequestInterceptor();
        List<ClientHttpRequestInterceptor> ris = new ArrayList<ClientHttpRequestInterceptor>();
        ris.add(ri);
        restTemplate.setInterceptors(ris);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }

    public String sendTextMessage(MessageType messageType, String recipientId, String text) {
        Message message = new Message();
        message.setText(text);
        message.setQuickReplies(null);

        String mid = callSendApi(messageType, recipientId, message);

        return mid;
    }

    public String sendQuickReply(MessageType messageType, String recipientId, Message message) {

        String mid = callSendApi(messageType, recipientId, message);

        return mid;
    }

    public String sendGenericTemplate(MessageType messageType, String recipientId, Payload payload) {
        Message message = new Message();
        Attachment attachment = new Attachment();
        attachment.setType("template");
        attachment.setPayload(payload);
        message.setAttachment(attachment);
        message.setQuickReplies(null);

        String mid = callSendApi(messageType, recipientId, message);

        return mid;
    }

    private String callSendApi(MessageType messageType, String recipientId, Message message) {
        String mid = "NA";
        String methodURL = graphApi+"?access_token="+pageAccessToken;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        SendAPI request = new SendAPI();
        Recipient recipient = new Recipient();
        recipient.setId(recipientId);
        request.setRecipient(recipient);
        request.setMessage(message);

        HttpEntity<SendAPI> requestEntity = new HttpEntity(request, requestHeaders);

        ResponseEntity<SendAPIResp> response = restTemplate.exchange(methodURL, HttpMethod.POST, requestEntity, SendAPIResp.class);
        HttpStatus status = response.getStatusCode();
        if (HttpStatus.OK.equals(status)) {
            System.out.println("Response form Send API OK");
            mid = response.getBody().getMessageId();
        } else {
            System.out.println("Response form Send API Fault");
        }

        msgLogger.logMessengerSendMessage(messageType, recipientId, request, response);

        return mid;
    }

}
