package com.secqme.domain.dao.jpa;

import java.util.HashMap;

/**
 * User: James Khoo
 * Date: 7/16/13
 * Time: 3:28 PM
 */
public class JPAParameter {
    private HashMap<String, Object> parameterMap  = null;

    public JPAParameter() {
        parameterMap = new HashMap<String, Object>();
    }

    public JPAParameter setParameter(String key, Object obj) {
        parameterMap.put(key, obj);
        return this;
    }

    public HashMap<String, Object> getParameterMap() {
        return parameterMap;
    }
}
