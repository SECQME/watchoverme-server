package com.secqme.util.rest;

import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author jameskhoo
 */
public interface RestUtil {
    public String executeGet(String URL, Properties header) throws RestExecException;
    public String executePost(String URL, String requestBody, Properties header) throws RestExecException;
    public String executePost(String URL, HashMap<String, String> parameters, Properties header) throws RestExecException;
    public String executePut(String URL, String requestBody, Properties header) throws RestExecException;
    public String executeDelete(String URL, Properties header)throws RestExecException ;
}
