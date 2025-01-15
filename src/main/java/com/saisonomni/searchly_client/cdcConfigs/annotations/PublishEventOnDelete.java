package com.saisonomni.searchly_client.cdcConfigs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishEventOnDelete {
    String eventName();
    String keyName();
    String path();
    String primaryKeyName() ;
    String deletedValue();
    String[] ref() default {};
}
