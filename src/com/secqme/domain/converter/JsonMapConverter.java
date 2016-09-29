package com.secqme.domain.converter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by edward on 04/06/2015.
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final Logger myLog = Logger.getLogger(JsonMapConverter.class);

    @Override
    public String convertToDatabaseColumn(Map<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (IOException ex) {
            myLog.error("Failed to serialize map to string", ex);
        }
        return null;
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String value) {
        if (value != null && !StringUtils.equalsIgnoreCase(value, "null")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(value, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException ex) {
                myLog.error("Failed to deserialize string to map", ex);
            }
        }
        return null;
    }

}
