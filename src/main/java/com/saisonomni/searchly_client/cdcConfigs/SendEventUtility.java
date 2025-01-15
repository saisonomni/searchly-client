package com.saisonomni.searchly_client.cdcConfigs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.saison.omni.common.utility.WebUtils;
import com.saison.omni.ehs.EhsHelper;
import com.saison.omni.ehs.EventConstants;
import com.saison.omni.ehs.MessageCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SendEventUtility {
    @Value("${service.ehs.url}")
    String eventUrl;
    @Value("${spring.application.name}")
    String applicationName;
    @Autowired
    Gson gson;
    @Autowired
    WebUtils webUtils;
    @Autowired
    ObjectMapper objectMapper;
    public void sendEventUtility(Object object) {
        try {
            Gson gson = new Gson();
            EhsHelper ehsHelper = new EhsHelper(eventUrl, applicationName, webUtils, objectMapper);
            Map<String, Object> attributes = new HashMap<>(4);
            attributes.put(EventConstants.EVENT_METADATA_EVENT_TYPE, "searchService.send");
            attributes.put(EventConstants.EVENT_METADATA_SOURCE,applicationName);
            attributes.put(EventConstants.REG_METADATA_MESSAGE_TYPE,MessageCategory.DIRECT);
            attributes.put(EventConstants.PAYLOAD_METADATA_DESTINATION,"internal");
            log.info("sending event: {}, eventType: {}, destination: {}, eventUrl: {}", object, "searchService.send", "internal", eventUrl);
            ehsHelper.sendEvent(gson.toJson(object),attributes);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

}
