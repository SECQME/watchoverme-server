package com.secqme.util.ar;

import com.secqme.domain.dao.ARMarketMessageTemplateDAO;
import com.secqme.domain.dao.LanguageDAO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.ar.ARMessageTemplateVO;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author coolboykl
 */
public class FreeMarkerARTemplateEngine extends BaseARTemplateEngine {

    private static Logger myLog = Logger.getLogger(FreeMarkerARTemplateEngine.class);

    private static final ARMessageTemplateField[] FREEMARKER_TEMPLATE_FIELDS = new ARMessageTemplateField[] {
            ARMessageTemplateField.EMAIL_BODY,
            ARMessageTemplateField.EMAIL_SUBJECT,
            ARMessageTemplateField.FACEBOOK_POST,
            ARMessageTemplateField.PUSH_NOTIFICATION_MESSAGE,
            ARMessageTemplateField.SMS_BODY
    };

    private Configuration fmConfiguration = null;
    private StringTemplateLoader fmTemplateLoader = null;

    public FreeMarkerARTemplateEngine(ARMarketMessageTemplateDAO arMarketMessageTemplateDAO, LanguageDAO languageDAO) {
        super(arMarketMessageTemplateDAO, languageDAO);
        fmConfiguration = new Configuration();
        fmTemplateLoader = new StringTemplateLoader();
        fmConfiguration.setTemplateLoader(fmTemplateLoader);
        refresh();
    }

    @Override
    public String getEngineName() {
        return ARMessageTemplateVO.ENGINE_FREE_MARKER;
    }

    @Override
    public void refresh() {
        super.refresh();

        myLog.debug("Refreshing FreeMarker templates...");

        fmConfiguration.clearTemplateCache();

        int count = 0;
        for (Map.Entry<String, ARMessageTemplateVO> entry : messageTemplates.entrySet()) {
            String templateKey = entry.getKey();
            ARMessageTemplateVO templateVO = entry.getValue();

            for (ARMessageTemplateField field : FREEMARKER_TEMPLATE_FIELDS) {
                String fieldValue = templateVO.getTemplateValue(getEngineName(), field);
                if (StringUtils.isNotEmpty(fieldValue)) {
                    fmTemplateLoader.putTemplate(generateTemplateItemKey(templateKey, field), fieldValue);
                    count++;
                }
            }
        }

        myLog.debug(String.format("Found %d FreeMarker entries.", count));
    }

    @Override
    public String getProcessedMessageText(String marketCode, String templateCode, String langCode, ARMessageTemplateField arMessageTemplateField, Map<String, Object> attributes) {
        String processedMessage = null;
        String arTemplateCode = null;

        String templateValue = getTemplateMessageText(marketCode, templateCode, langCode, arMessageTemplateField);
        if (StringUtils.isNotEmpty(templateValue)) {
            arTemplateCode = generateTemplateItemKey(generateTemplateKey(marketCode, templateCode, langCode), arMessageTemplateField);
        } else {
            arTemplateCode = generateTemplateItemKey(generateTemplateKey(marketCode, templateCode, defaultLangCode), arMessageTemplateField);
        }

        myLog.debug("Processing Template for: " + arTemplateCode);

        StringWriter stringWriter = new StringWriter();
        try {
            Template fmTemplate = fmConfiguration.getTemplate(arTemplateCode, "UTF-8");
            fmTemplate.process(attributes, stringWriter);
            stringWriter.flush();
            processedMessage = stringWriter.toString();
            stringWriter.close();
        } catch (IOException ex) {
            myLog.error("Problem of processing template for: " + arTemplateCode, ex);
        } catch (TemplateException ex) {
            myLog.error("Problem of processing template for: " + arTemplateCode, ex);
        }
        return processedMessage;
    }

    private String generateTemplateItemKey(String templateKey, ARMessageTemplateField field) {
        return templateKey + "#" + field.name();
    }
}
