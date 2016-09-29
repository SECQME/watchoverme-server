package com.secqme.util.spring;

import java.util.Locale;

/**
 * Interface of SpringContextHelper, a utilities on locating Configure SpringBean
 *
 * @author jameskhoo
 */
public interface SpringUtil {

    public Object getBean(BeanType springBean);
    public String getMessage(String key);

    public String getMessage(String key, String langCode);
    public String getMessage(String key, Locale locale);
    public String getMessage(String key, Object [] params);
    public String getMessage(String key, String langCode, Object [] params);
    public String getMessage(String key, Object [] params, Locale locale);

}
