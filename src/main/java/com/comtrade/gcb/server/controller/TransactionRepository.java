package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByRecipientId(String recipientId);
    Transaction findByUuid(String uuid);
    List<Transaction> findByUserId(String userId);
}
