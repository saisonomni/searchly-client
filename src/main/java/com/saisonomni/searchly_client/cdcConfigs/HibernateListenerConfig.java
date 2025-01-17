package com.saisonomni.searchly_client.cdcConfigs;


import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;


//@Configuration
@Component
@ConditionalOnProperty(prefix = "hibernate.event.listener", name = "enabled", havingValue = "true")
@DependsOn("SendEventUtility")
public class HibernateListenerConfig {
    public HibernateListenerConfig(EntityManagerFactory entityManagerFactory) {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                .getService(EventListenerRegistry.class);

        // Register the custom listener for PreInsert and PreUpdate events
        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(new GlobalEntityInsertListener());
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(new GlobalEntityDeleteListener());
        registry.getEventListenerGroup(EventType.MERGE).appendListener(new GlobalEntityUpdateListener());
    }
}
