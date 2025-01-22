package com.saisonomni.searchly_client.cdcConfigs;


import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
public class GlobalEntityUpdateListener implements MergeEventListener {

    @Override
    public void onMerge(MergeEvent event) {
        try {
            helper(event);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        try {
            helper(event);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void helper(MergeEvent event) throws IllegalAccessException, NoSuchFieldException {
        Object entity = event.getEntity();
        if(!entity.getClass().isAnnotationPresent(CDCEntity.class)){
            return;
        }
        Class<?> entityClass = entity.getClass();
        log.info("Entering post update listener");
        JSONObject jsonObject;
        //check what field needs to be checked
        PublishEventOnDelete publishEventOnDelete = entityClass.getAnnotation(PublishEventOnDelete.class);
        Field fieldToBeCheckedForDeletion = entityClass.getDeclaredField(publishEventOnDelete.keyName());
        /*
        Check if the entity is being soft deleted
        * */
        Boolean isSoftDeleted = fieldToBeCheckedForDeletion.get(entity).toString().compareToIgnoreCase(publishEventOnDelete.deletedValue())==0;
        if(isSoftDeleted){
            // publish the payload with type DELETE
            //and return
            jsonObject = HibernateOperationsUtility.deleteHelper(entity,publishEventOnDelete);
            }
        else{
            jsonObject = HibernateOperationsUtility.upsertHelper(entity);
        }
        new SendEventUtility().sendEventUtility(jsonObject);
    }
}
