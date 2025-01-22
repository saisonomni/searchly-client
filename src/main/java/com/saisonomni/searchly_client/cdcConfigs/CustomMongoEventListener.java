package com.saisonomni.searchly_client.cdcConfigs;

import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


@Component
@ConditionalOnProperty(prefix = "hibernate.event.listener", name = "enabled", havingValue = "true")
@Slf4j
public class CustomMongoEventListener extends AbstractMongoEventListener<Object> {

    @Override
    public void onAfterSave(AfterSaveEvent<Object> event) {
        Object entity = event.getSource();
        if(!entity.getClass().isAnnotationPresent(CDCEntity.class)){
            return;
        }
        Class<?> entityClass = entity.getClass();
        log.info("Entering post save listener");
        JSONObject jsonObject;
        //check what field needs to be checked
        PublishEventOnDelete publishEventOnDelete = entityClass.getAnnotation(PublishEventOnDelete.class);
        Field fieldToBeCheckedForDeletion = null;
        try {
            fieldToBeCheckedForDeletion = entityClass.getDeclaredField(publishEventOnDelete.keyName());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        /*
        Check if the entity is being soft deleted
        * */
        Boolean isSoftDeleted = null;
        try {
            fieldToBeCheckedForDeletion.setAccessible(true);
            isSoftDeleted = fieldToBeCheckedForDeletion.get(entity).toString().compareToIgnoreCase(publishEventOnDelete.deletedValue())==0;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if(isSoftDeleted){
            // publish the payload with type DELETE
            //and return
            try {
                jsonObject = HibernateOperationsUtility.deleteHelper(entity,publishEventOnDelete);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            jsonObject = HibernateOperationsUtility.upsertHelper(entity);
        }
        new SendEventUtility().sendEventUtility(jsonObject);
    }
    @Override
    public void onAfterDelete(AfterDeleteEvent<Object> event) {
        Object entity = event.getSource();
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

}
