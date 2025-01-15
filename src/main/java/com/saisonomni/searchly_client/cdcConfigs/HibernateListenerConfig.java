package com.saisonomni.searchly_client.cdcConfigs;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;



@Configuration
@ConditionalOnProperty(prefix = "hibernate.event.listener", name = "enabled", havingValue = "true")
public class HibernateListenerConfig {
    @Autowired
    GlobalEntityDeleteListener globalEntityDeleteListener;
    @Autowired
    GlobalEntityUpdateListener globalEntityUpdateListener;
    @Autowired
    GlobalEntityInsertListener globalEntityInsertListener;
    public HibernateListenerConfig(EntityManagerFactory entityManagerFactory) {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                .getService(EventListenerRegistry.class);

        // Register the custom listener for PreInsert and PreUpdate events
//        GlobalEntityInsertListener globalListener = new GlobalEntityInsertListener();
        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(globalEntityInsertListener);
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(globalEntityDeleteListener);
        registry.getEventListenerGroup(EventType.MERGE).appendListener(globalEntityUpdateListener);
    }
}
