package edu.cornell.kfs.module.purap.document.validation.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.context.SpringContext;

@SuppressWarnings("deprecation")
public class IWantDocumentPreRules extends PromptBeforeValidationBase {

    @Override
    public boolean doPrompts(Document document) {

        if(ObjectUtils.isEmpty(document.getNotes())) {

            ConfigurationService configurationService = SpringContext.getBean(ConfigurationService.class);
            String questionText = configurationService.getPropertyValueAsString("message.iwant.document.no.attachments.confirm");
            boolean yesContinueSelected = askOrAnalyzeYesNoQuestion(CUPurapConstants.IWNT_NO_ATTACHMENTS_QUESTION_ID, questionText);

            if (!yesContinueSelected) {
                getEvent().setActionForwardName("basic");
                return false;
            }

        }

        return true;
    }

}
