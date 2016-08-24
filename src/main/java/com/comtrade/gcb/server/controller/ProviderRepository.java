package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.data.jpa.GiftProvider;
import com.comtrade.gcb.data.jpa.User;
import org.springframework.data.repository.CrudRepository;

public interface ProviderRepository extends CrudRepository<GiftProvider, String> {
}
