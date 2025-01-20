package com.saisonomni.searchly_client.cdcConfigs;

import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnUpsert;
import com.saisonomni.searchly_client.cdcConfigs.dto.UpsertValueDTO;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class HibernateOperationsUtility {
    public static JSONObject upsertHelper(Object entity){
        if(!entity.getClass().isAnnotationPresent(CDCEntity.class)){
            return null;
        }
        Class<?> entityClass = entity.getClass();
        JSONObject jsonObject = new JSONObject();
        boolean annotationPresent = false;
        List<UpsertValueDTO> upsertValueDTOList = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PublishEventOnUpsert.class)) {
                annotationPresent = true;
                field.setAccessible(true);
                try {
                    UpsertValueDTO upsertValueDTO = UpsertValueDTO.builder()
                            .build();
                    PublishEventOnUpsert annotation = field.getAnnotation(PublishEventOnUpsert.class);
                    try {
                        field.setAccessible(true);
                        Map<String, Object> dataPairMap =  new HashMap<>();
                        dataPairMap.put(annotation.keyName(), field.get(entity).toString());
                        upsertValueDTO.setDataPairs(dataPairMap);
                        jsonObject.put("searchIndex", annotation.eventName());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    if (annotation.ref().length > 0) {
                        List<String> refIdList = new ArrayList<>();
                        for (int i = 0; i < annotation.ref().length; i++) {
                            String path = annotation.ref()[i];
                            Object tempEntity = entity;
                            StringTokenizer stringTokenizer = new StringTokenizer(path, ".");
                            Class returnTypeClass = entityClass;
                            while (stringTokenizer.hasMoreTokens()) {
                                String token = stringTokenizer.nextToken();
                                String methodName = "get" + token.substring(0, 1).toUpperCase() + token.substring(1);
                                Method method = null;
                                try {
                                    method = returnTypeClass.getMethod(methodName);
                                    Object result = method.invoke(tempEntity);
                                    tempEntity = result;
                                } catch (NoSuchMethodException e) {
                                    System.out.println("no such method");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                returnTypeClass = method.getReturnType();
                            }
                            refIdList.add(tempEntity.toString());
                        }
                        Collections.reverse(refIdList);
                        upsertValueDTO.setRef(refIdList);
                    }
                    upsertValueDTO.setPath(annotation.path());
                    upsertValueDTOList.add(upsertValueDTO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (annotationPresent) {
            jsonObject.put("operation", "UPSERT");
            jsonObject.put("value",upsertValueDTOList);
        }
        return jsonObject;
    }
    public static JSONObject deleteHelper(Object entity, PublishEventOnDelete annotation) throws NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = new JSONObject();
        Class<?> entityClass = entity.getClass();
        jsonObject.put("searchIndex", annotation.eventName());
        List<UpsertValueDTO> upsertValueDTOList = new ArrayList<>();
        UpsertValueDTO upsertValueDTO = UpsertValueDTO.builder()
                .build();
        if (annotation.ref().length > 0){
            List<String> refIdList = new ArrayList<>();
            for(int i=0;i<annotation.ref().length;i++){
                String path = annotation.ref()[i];
                if(path.compareToIgnoreCase("#")==0){
                    Field idKey = entityClass.getDeclaredField(annotation.primaryKeyName());
                    idKey.setAccessible(true);
                    refIdList.add(idKey.get(entity).toString());
                    upsertValueDTO.setRef(refIdList);
                    upsertValueDTO.setPath(annotation.path());
                    continue;
                }
                Object tempEntity = entity;
                StringTokenizer stringTokenizer = new StringTokenizer(path,".");
                Class returnTypeClass = entityClass;
                while(stringTokenizer.hasMoreTokens()) {
                    String token = stringTokenizer.nextToken();
                    String methodName = "get" + token.substring(0, 1).toUpperCase() + token.substring(1);
                    Method method = null;
                    try {
                        method = returnTypeClass.getMethod(methodName);
                        Object result = method.invoke(tempEntity);
                        tempEntity = result;
                    } catch (NoSuchMethodException e) {
                        System.out.println("no such method");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    returnTypeClass = method.getReturnType();
                }
                refIdList.add(tempEntity.toString());
            }
            Collections.reverse(refIdList);
            upsertValueDTO.setRef(refIdList);
            upsertValueDTO.setPath(annotation.path());
        }
        upsertValueDTOList.add(upsertValueDTO);
        jsonObject.put("operation","DELETE");
        jsonObject.put("value",upsertValueDTOList);
        return jsonObject;
    }
}
