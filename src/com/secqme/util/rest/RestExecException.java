/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.secqme.util.rest;

/**
 *
 * @author jameskhoo
 */
public class RestExecException extends RuntimeException {

    private int httpCode;
    private String url;

    public RestExecException() {
        super();
    }

    public RestExecException(int httpCode, String msg) {
        super(msg);
        this.httpCode = httpCode;
    }


    public RestExecException(int httpCode, String msg, Exception e) {
        super(msg, e);
        this.httpCode = httpCode;
    }

    public RestExecException(int httpCode, String url, String msg, Throwable t) {
        super(msg, t);
        this.httpCode = httpCode;
        this.url = url;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
