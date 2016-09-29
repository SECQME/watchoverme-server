package com.secqme.util.ar;

import com.secqme.domain.model.ar.ARMarketMessageTemplateVO;
import com.secqme.domain.model.ar.ARMessageTemplateVO;
import com.secqme.domain.model.ar.ARMessageTemplateField;

import java.util.List;
import java.util.Map;

/**
 *
 * @author coolboykl
 */
public interface ARTemplateEngine {
    
    public void refresh();
    public String getEngineName();
    public List<ARMarketMessageTemplateVO> getMarketTemplates(String marketCode);
    public ARMessageTemplateVO getARMessageTemplateVO(String marketCode, String templateCode, String langCode);
    public String getTemplateMessageText(String marketCode, String templateCode, String langCode, ARMessageTemplateField arMessageTemplateField);
    public String getProcessedMessageText(String marketCode, String templateCode, String langCode, ARMessageTemplateField arMessageTemplateField, Map<String, Object> attributes);
}