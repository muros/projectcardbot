package com.comtrade.gcb.server.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@EnableAutoConfiguration
@SpringBootApplication
public class GcbConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(GcbConfiguration.class, args);
    }
}
