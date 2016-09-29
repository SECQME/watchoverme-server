package com.secqme.domain.model.notification.email;

import com.secqme.domain.model.ar.Parameterizable;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 28/04/2015.
 */
public class EmailRecipientVO implements Serializable, Parameterizable<String, Object> {

    private static Logger myLog = Logger.getLogger(EmailRecipientVO.class);

    private String name;
    private String email;
    private Map<String, Object> params;

    public EmailRecipientVO(String name, String email) {
        this(name, email, new HashMap<String, Object>());
    }

    public EmailRecipientVO(String name, String email, @NotNull Map<String, Object> params) {
        this.name = name;
        this.email = email;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Map<String, Object> getParams() {
        return this.params;
    }

    @Override
    public void clearParam() {
        params.clear();
    }

    @Override
    public void putParam(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public Object removeParam(String key) {
        return params.remove(key);
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer, this);
        } catch (IOException e) {
            myLog.error("Failed to serialize " + this.getClass().getName());
        }
        return writer.toString();
    }
}
