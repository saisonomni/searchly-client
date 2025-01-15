package com.saisonomni.searchly_client.cdcConfigs;

import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GlobalEntityDeleteListener implements PostDeleteEventListener {
    @Autowired
    SendEventUtility sendEventUtility;
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
        JSONObject jsonObject;
        try {
            jsonObject = HibernateOperationsUtility.deleteHelper(entity,fieldList);
            sendEventUtility.sendEventUtility(jsonObject);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}
