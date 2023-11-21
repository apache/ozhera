package com.xiaomi.mone.test;

import com.xiaomi.mone.app.api.model.HeraMetaDataPortModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

@Slf4j
public class HeraMetaDataTest {

    @Test
    public void testField(){
        HeraMetaDataPortModel portModel = new HeraMetaDataPortModel();
        portModel.setThriftPort(10030);

        Class<? extends HeraMetaDataPortModel> aClass = portModel.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for(Field field : declaredFields){
            field.setAccessible(true);
            try {
                int o = (int) field.get(portModel);
                if(o > 0){
                    System.out.println("get port : "+o);
                    return;
                }
            } catch (Exception e) {
                log.error("Hera meta data Consumer getAvailablePort error : ",e);
            }
        }
        System.out.println("no get port");
    }
}
