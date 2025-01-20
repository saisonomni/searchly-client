package com.saisonomni.searchly_client.cdcConfigs;

import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;

@Slf4j
public class GlobalEntityDeleteListener implements PostDeleteEventListener {
    @Override
    public void onPostDelete(PostDeleteEvent event) {
        log.info("Entering post delete listener");
        /*
        handling hard deletes
        * */
        Object entity = event.getEntity();
        if(!entity.getClass().isAnnotationPresent(CDCEntity.class)){
            return;
        }
        Class<?> entityClass = entity.getClass();
        if(entityClass.getAnnotation(PublishEventOnDelete.class) == null){
            return;
        }
        JSONObject jsonObject;
        try {
            jsonObject = HibernateOperationsUtility.deleteHelper(entity,entityClass.getAnnotation(PublishEventOnDelete.class));
            new SendEventUtility().sendEventUtility(jsonObject);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
