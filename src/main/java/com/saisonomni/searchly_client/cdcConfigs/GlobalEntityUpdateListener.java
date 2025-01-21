package com.saisonomni.searchly_client.cdcConfigs;


import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.MergeContext;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class GlobalEntityUpdateListener implements MergeEventListener {

    SendEventUtility sendEventUtility;

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

    }

//    @Override
//    public void onMerge(MergeEvent event, MergeContext copiedAlready) {
//        try {
//            helper(event);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void helper(MergeEvent event) throws IllegalAccessException, NoSuchFieldException {
        Object entity = event.getEntity();
        if(!entity.getClass().isAnnotationPresent(CDCEntity.class)){
            return;
        }
        Class<?> entityClass = entity.getClass();
        log.info("Entering post delete listener");
        JSONObject jsonObject;
        /*
        Check if the entity is being soft deleted
        * */
        List<Field> fieldList = Arrays.stream(entityClass.getDeclaredFields()).filter(field ->
                        field.isAnnotationPresent(PublishEventOnDelete.class))
                .collect(Collectors.toList());
        fieldList = fieldList.stream().filter(field -> {
            field.setAccessible(true);
            try {
                return field.get(entity).toString().compareToIgnoreCase(field.getAnnotation(PublishEventOnDelete.class)
                        .deletedValue())==0;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        if(fieldList.size()==1){
            // publish the payload with type DELETE
            //and return
            jsonObject = HibernateOperationsUtility.deleteHelper(entity,fieldList);
            }
        else{
            jsonObject = HibernateOperationsUtility.upsertHelper(entity);
        }
        sendEventUtility.sendEventUtility(jsonObject);
    }
}
