package com.comtrade.gcb.data.jpa;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Message {

    private String recipientId;
    private String senderId;
    private String timestamp;
    private DBObject request;
    private String response;
    private MessageType messageType;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequest() {
        return request.toString();
    }

    public void setRequest(String request) {
        this.request = (DBObject) JSON.parse(request);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
