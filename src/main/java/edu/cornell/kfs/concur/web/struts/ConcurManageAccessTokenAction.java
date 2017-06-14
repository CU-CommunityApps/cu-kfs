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

    public ActionForward requestNewToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        boolean requestNewToken;
        
        if (ObjectUtils.isNull(question)) {
            if (getConcurAccessTokenService().isCurrentAccessTokenRevoked()) {
                requestNewToken = true;
            } else {
                String questionText = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_TOKEN_REQUEST_QUESTION);
                return performQuestionWithoutInput(mapping, form, request, response,
                        ConcurConstants.OVERWRITE_ACCESS_TOKEN_QUESTION, questionText,
                        KFSConstants.CONFIRMATION_QUESTION, ConcurConstants.REQUEST_NEW_TOKEN_METHOD_TO_CALL, StringUtils.EMPTY);
            }
        } else {
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            requestNewToken = userClickedYes(buttonClicked);
        }
        
        if (requestNewToken) {
            getConcurAccessTokenService().requestNewAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REQUEST_SUCCESS);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().isCurrentAccessTokenRevoked()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, ConcurKeyConstants.CONCUR_TOKEN_REFRESH_REVOKED);
        } else {
            getConcurAccessTokenService().refreshAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REFRESH_SUCCESS);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward revokeToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().isCurrentAccessTokenRevoked()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, ConcurKeyConstants.CONCUR_TOKEN_ALREADY_REVOKED);
        } else {
            Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (ObjectUtils.isNull(question)) {
                String questionText = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_TOKEN_REVOKE_QUESTION);
                return performQuestionWithoutInput(mapping, form, request, response,
                        ConcurConstants.REVOKE_ACCESS_TOKEN_QUESTION, questionText,
                        KFSConstants.CONFIRMATION_QUESTION, ConcurConstants.REVOKE_TOKEN_METHOD_TO_CALL, StringUtils.EMPTY);
            }
            
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (userClickedYes(buttonClicked)) {
                getConcurAccessTokenService().revokeAccessToken();
                GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REVOKE_SUCCESS);
            }
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
