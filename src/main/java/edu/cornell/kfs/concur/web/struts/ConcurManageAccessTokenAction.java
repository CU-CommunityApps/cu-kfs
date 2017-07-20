package edu.cornell.kfs.concur.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;

@SuppressWarnings("deprecation")
public class ConcurManageAccessTokenAction extends KualiAction {

    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward replaceToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        boolean revokeOldToken;
        
        if (ObjectUtils.isNull(question)) {
            if (getConcurAccessTokenService().currentAccessTokenExists()) {
                String questionText = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REPLACE_QUESTION);
                return performQuestionWithoutInput(mapping, form, request, response,
                        ConcurConstants.REPLACE_ACCESS_TOKEN_QUESTION, questionText,
                        KFSConstants.CONFIRMATION_QUESTION, ConcurConstants.REPLACE_TOKEN_METHOD_TO_CALL, StringUtils.EMPTY);
            } else {
                revokeOldToken = false;
            }
        } else {
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            revokeOldToken = userClickedYes(buttonClicked);
        }
        
        if (revokeOldToken) {
            getConcurAccessTokenService().revokeAndReplaceAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS);
        } else {
            getConcurAccessTokenService().requestNewAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REPLACE_SUCCESS);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().currentAccessTokenExists()) {
            getConcurAccessTokenService().refreshAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REFRESH_SUCCESS);
        } else {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, ConcurKeyConstants.ERROR_CONCUR_REFRESH_NONEXISTENT_TOKEN);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected boolean userClickedYes(Object buttonClicked) {
        return ConfirmationQuestion.YES.equals(buttonClicked);
    }

    protected ConcurAccessTokenService getConcurAccessTokenService() {
        return SpringContext.getBean(ConcurAccessTokenService.class);
    }

    protected ConfigurationService getConfigurationService() {
        return SpringContext.getBean(ConfigurationService.class);
    }

}
