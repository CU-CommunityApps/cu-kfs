package edu.cornell.kfs.module.purap.document.validation.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.List;

@SuppressWarnings("deprecation")
public class IWantDocumentPreRules extends PromptBeforeValidationBase {

    @Override
    public boolean doPrompts(Document document) {
        IWantDocument iWantDocument = (IWantDocument) document;

        if (!iWantDocument.hasAttachment()) {

            ConfigurationService configurationService = SpringContext.getBean(ConfigurationService.class);
            String questionText = configurationService.getPropertyValueAsString(CUPurapKeyConstants.IWNT_NO_ATTACHMENTS_QUESTION);
            boolean yesContinueSelected = askOrAnalyzeYesNoQuestion(CUPurapConstants.IWNT_NO_ATTACHMENTS_QUESTION_ID, questionText);

            if (!yesContinueSelected) {
                getEvent().setActionForwardName(KFSConstants.MAPPING_BASIC);
                return false;
            }

        }

        return true;
    }

}
