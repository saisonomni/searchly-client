package com.saisonomni.searchly_client.cdcConfigs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityListenerConfig {
    @Autowired
    SendEventUtility sendEventUtility;
    @Bean
    public GlobalEntityInsertListener getGlobalEntityInsertListener() {
        return new GlobalEntityInsertListener(sendEventUtility);
    }
    @Bean
    public GlobalEntityDeleteListener getGlobalEntityDeleteListener() {
        return new GlobalEntityDeleteListener(sendEventUtility);
    }
    @Bean
    public GlobalEntityUpdateListener getGlobalEntityUpdateListener() {
        return new GlobalEntityUpdateListener(sendEventUtility);
    }
}
