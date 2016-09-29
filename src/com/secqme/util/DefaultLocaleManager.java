package com.secqme.util;

import com.secqme.domain.dao.LanguageDAO;
import com.secqme.domain.model.LanguageVO;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author coolboykl
 */
public final class DefaultLocaleManager implements LocaleManager {

    private Logger myLog = Logger.getLogger(DefaultLocaleManager.class);
    private LanguageDAO langDAO = null;
    private Map<String, LanguageVO> languageMap = null;
    private LanguageVO defaultLanguage = null;
    private List<LanguageVO> languageList = null;

    public DefaultLocaleManager(LanguageDAO languageDAO) {
        this.langDAO = languageDAO;
        refreshLocaleSetting();
    }

    public LanguageVO getDefaultLanguage() {
        return defaultLanguage;
    }

    @Override
    public LanguageVO getLanguageVO(String langCode) {
        return languageMap.get(langCode);
    }

    public Locale getLocale(String langCode) {
        Locale locale;
        String finalLangCode = verifyLanguageCode(langCode);
        if (finalLangCode.contains("_")) {
            String[] languageCodeArray = finalLangCode.split("_");
            locale = new Locale(languageCodeArray[0], languageCodeArray[1]);
        } else {
            locale = new Locale(langCode);
        }

        return locale;
    }

    public LanguageVO getSupportedLanguage(Locale locale) {
        return getSupportedLanguage(locale.toString());
    }

    public LanguageVO getSupportedLanguage(String langCode) {
        if (langCode == null) {
            return getDefaultLanguage();
        }

        // First try to see if we can find the langCode from the Map
        LanguageVO languageVO = languageMap.get(langCode);
        if (languageVO != null) {
            return languageVO;
        } else {
            // Find all the language Abbreviation
            Set<Entry<String, LanguageVO>> languageSet = languageMap.entrySet();
            for (Entry<String, LanguageVO> languageEntry : languageSet) {
                languageVO = languageEntry.getValue();
                if (languageVO.getAbbreviation() != null
                     && languageVO.getAbbreviation().indexOf(langCode) > 0) {
                    return languageVO;
                }
            }
        }

        return this.getDefaultLanguage();
    }

    @Override
    public String verifyLanguageCode(String langCode) {
        return getSupportedLanguage(langCode).getCode();
    }

    public void refreshLocaleSetting() {
        languageList = langDAO.findAll();
        languageMap = new HashMap<String, LanguageVO>();
        for (LanguageVO langVO : languageList) {
            if (langVO.isDefaultLanguage()) {
                defaultLanguage = langVO;
            }
            languageMap.put(langVO.getCode(), langVO);
        }
    }

    public List<LanguageVO> getAllSupportedLanguageVO() {
        return languageList;
    }
}
