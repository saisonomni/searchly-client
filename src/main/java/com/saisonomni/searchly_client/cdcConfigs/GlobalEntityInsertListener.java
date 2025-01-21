package com.saisonomni.searchly_client.cdcConfigs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
public class GlobalEntityInsertListener implements PostInsertEventListener {
    SendEventUtility sendEventUtility;

    @Override
    public void onPostInsert(PostInsertEvent event) {
        Object entity = event.getEntity();
        JSONObject jsonObject = HibernateOperationsUtility.upsertHelper(entity);
        sendEventUtility.sendEventUtility(jsonObject);
    }


    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}