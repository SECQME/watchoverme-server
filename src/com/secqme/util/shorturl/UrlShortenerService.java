package com.secqme.util.shorturl;

/**
 *
 * @author jameskhoo
 */
public interface UrlShortenerService {

    public String getShortURL(String longURL, String feature) throws ShortURLException;

}
