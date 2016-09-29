package com.secqme.domain.converter;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by edward on 07/28/2015.
 */
@Converter
public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {

    private static final Logger myLog = Logger.getLogger(JSONObjectConverter.class);

    @Override
    public String convertToDatabaseColumn(JSONObject jsonObject) {
        if (jsonObject != null) {
            return jsonObject.toString();
        } else {
            return null;
        }
    }

    @Override
    public JSONObject convertToEntityAttribute(String value) {
        if (value != null) {
            try {
                return new JSONObject(value);
            } catch (JSONException e) {
                myLog.error("Failed to deserialize JSONObject");
            }
        }
        return null;
    }

}
