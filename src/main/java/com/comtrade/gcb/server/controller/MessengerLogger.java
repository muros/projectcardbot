package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.Message;
import com.comtrade.gcb.data.jpa.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Logging of messages sent to Messenger and replies from client.
 * Messages can also be queried for flow of conversation purposes.
 */
@Component
public class MessengerLogger {

    private ObjectMapper mapper;

    @Autowired
    private MessageRepository messageRepository;

    public MessengerLogger() {
        mapper = new ObjectMapper();
    }

    public void logMessengerSendMessage(MessageType messageType, String recipientId, Object request, Object response) {
        String requestAsStr = null;
        try {
            requestAsStr = mapper.writeValueAsString(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message message = new Message();
        message.setMessageType(messageType);
        message.setRecipientId(recipientId);
        message.setRequest(requestAsStr);
        message.setResponse(response.toString());
        message.setTimestamp(LocalDateTime.now().toString());

        messageRepository.save(message);
    }

    public Message findLastLogMessageForRecipient(String recipientId, Long messageAgeInSeconds) {
        Message lastMsg = messageRepository.findLastMessageForRecipient(recipientId, new Sort(Sort.Direction.DESC, "timestamp"));
        if (lastMsg == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        String msgTime = lastMsg.getTimestamp();
        LocalDateTime then = LocalDateTime.parse(msgTime);
        Duration duration = Duration.between(then, now);
        long seconds = duration.getSeconds();
        if (seconds < messageAgeInSeconds) {
            return lastMsg;
        } else {
            return null;
        }
    }

}
