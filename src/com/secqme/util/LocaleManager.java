package com.secqme.util;

import com.secqme.domain.model.LanguageVO;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author coolboykl
 */
public interface LocaleManager {
    public Locale getLocale(String langCode);
    public String verifyLanguageCode(String langCode);
    public void refreshLocaleSetting();
    public LanguageVO getDefaultLanguage();
    public LanguageVO getLanguageVO(String langCode);
    public LanguageVO getSupportedLanguage(String langCode);
    public LanguageVO getSupportedLanguage(Locale locale);
    public List<LanguageVO> getAllSupportedLanguageVO();
    
}
