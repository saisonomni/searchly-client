package com.saisonomni.searchly_client.cdcConfigs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.saison.omni.ehs.EhsHelper;
import com.saison.omni.ehs.EventConstants;
import com.saison.omni.ehs.MessageCategory;
import com.saisonomni.searchly_client.cdcConfigs.annotations.CDCEntity;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnDelete;
import com.saisonomni.searchly_client.cdcConfigs.annotations.PublishEventOnUpsert;
import com.saisonomni.searchly_client.cdcConfigs.dto.UpsertValueDTO;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class HibernateOperationsUtility {
    @Value("${service.ehs.url}")
    String eventUrl;
    @Value("${spring.application.name}")
    String applicationName;
    @Autowired
    WebUtils webUtils;
    @Autowired
    ObjectMapper objectMapper;
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
//            sendEventUtility(jsonObject, "kuch bhi", "searchService.send", "internal");
        }
        return jsonObject;
    }
    public static JSONObject deleteHelper(Object entity, List<Field> fieldList) throws NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = new JSONObject();
        Class<?> entityClass = entity.getClass();
        PublishEventOnDelete annotation = fieldList.get(0).getAnnotation(PublishEventOnDelete.class);
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
//        sendEventUtility(jsonObject,MessageCategory.DIRECT,applicationName,"searchService.send","internal");
    }
//    public void sendEventUtility(Object object, MessageCategory category, String serviceName, String eventType, String destination) {
//        try {
//            Gson gson = new Gson();
//            EhsHelper ehsHelper = new EhsHelper(eventUrl, applicationName, webUtils, objectMapper);
//            Map<String, Object> attributes = new HashMap<>(4);
//            attributes.put(EventConstants.EVENT_METADATA_EVENT_TYPE, eventType);
//            attributes.put(EventConstants.EVENT_METADATA_SOURCE,serviceName);
//            attributes.put(EventConstants.REG_METADATA_MESSAGE_TYPE,category);
//            attributes.put(EventConstants.PAYLOAD_METADATA_DESTINATION,destination);
//            log.info("sending event: {}, eventType: {}, destination: {}, eventUrl: {}", object, eventType, destination, eventUrl);
//            ehsHelper.sendEvent(gson.toJson(object),attributes);
//        } catch (RuntimeException e) {
//            log.error(e.getMessage());
//            log.error(Arrays.toString(e.getStackTrace()));
//        }
//
//    }
//    public static void sendEventUtility(Object object, String serviceName,
//                                        String eventType, String destination) {
//        try {
//            Gson gson = new Gson();
//            String eventUrl = "http://ehs-service.l2.svc.cluster.local:8080";
//            Map<String, Object> attributes = new HashMap<>(4);
//            attributes.put("eventMetadata.eventType", eventType);
//            attributes.put("eventMetadata.source",serviceName);
//            attributes.put("listenerRegistrationUnit.messageType","DIRECT");
//            attributes.put("payloadMetadata.destination",destination);
//            log.info("sending event: {}, eventType: {}, destination: {}, eventUrl: {}", object, eventType, destination, eventUrl);
//            sendEvent(gson.toJson(object),attributes,eventUrl);
//        } catch (RuntimeException e) {
//            log.error(e.getMessage());
//            log.error(Arrays.toString(e.getStackTrace()));
//        }
//    }
//    public static void sendEvent(String payload, Map<String, Object> attributes,String eventUrl) {
//        EventPayload eventPayload = EventPayload.builder().payload(payload).eventContext(new EventContext()).payloadMetadata(new PayloadMetadata()).build();
//        eventPayload.getEventContext().setEventId(UUID.randomUUID().toString());
//        eventPayload.getEventContext().setCreatedAt(System.currentTimeMillis());
//        attributes.forEach((k, v) -> {
//            switch (k) {
//                case "eventMetadata.source":
//                    eventPayload.getEventContext().setSource(String.valueOf(v));
//                    break;
//                case "eventMetadata.eventType":
//                    eventPayload.getEventContext().setEventType(String.valueOf(v));
//                    break;
//                case "payloadMetadata.destination":
//                    eventPayload.getPayloadMetadata().setDestination(String.valueOf(v));
//                    break;
//                case "payloadMetadata.destinationType":
//                    eventPayload.getPayloadMetadata().setDestinationType(String.valueOf(v));
//                    break;
//                case "eventMetadata.delay":
//                    eventPayload.getEventContext().setDelay((Long)v);
//            }
//
//        });
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.postForEntity(eventUrl + "/event-handling/event", eventPayload, Map.class, new Object[0]);
//    }
}
