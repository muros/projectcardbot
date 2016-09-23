package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by muros on 9.9.2016.
 */
public interface MessageRepository extends MongoRepository<Message, String> {

    @Query(value = "{recipientId : ?0}")
    Message findLastMessageForRecipient(String recipientId, Sort sort);
    List<Message> findByRecipientId(String recipientId);
    List<Message> findBySenderId(String recipientId);
}