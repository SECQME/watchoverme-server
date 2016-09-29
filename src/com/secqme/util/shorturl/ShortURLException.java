package com.secqme.util.shorturl;

/**
 *
 * @author jameskhoo
 */
public class ShortURLException extends Exception {

    public ShortURLException(String msg, Exception e) {
        super(msg, e);
    }

    public ShortURLException(String msg) {
        super(msg);
    }

}
