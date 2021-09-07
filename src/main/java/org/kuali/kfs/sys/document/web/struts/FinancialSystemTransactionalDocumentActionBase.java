/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.document.web.struts;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.web.struts.action.KualiTransactionalDocumentActionBase;
import org.kuali.kfs.kns.web.struts.form.KualiTransactionalDocumentFormBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

public class FinancialSystemTransactionalDocumentActionBase extends KualiTransactionalDocumentActionBase {

    public static final String MODULE_LOCKED_MESSAGE = "moduleLockedMessage";
    public static final String MODULE_LOCKED_URL_SUFFIX = "/moduleLocked.do";

    /**
     * This action method triggers a correct of the transactional document.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception KRAD Conversion: Customizing the extra buttons on the form
     */
    public ActionForward correct(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        KualiTransactionalDocumentFormBase tmpForm = (KualiTransactionalDocumentFormBase) form;

        Document document = tmpForm.getDocument();

        ((Correctable) tmpForm.getTransactionalDocument()).toErrorCorrection();
        tmpForm.setExtraButtons(new ArrayList<>());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ActionForward returnForward = super.execute(mapping, form, request, response);
        if (isDocumentLocked(mapping, form, request)) {
            String message = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                    KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.PARAMETER_ALL_DETAIL_TYPE,
                    KFSConstants.DOCUMENT_LOCKOUT_DEFAULT_MESSAGE);
            request.setAttribute(MODULE_LOCKED_MESSAGE, message);
            returnForward = mapping.findForward(KFSConstants.MODULE_LOCKED_MAPPING);
        }

        return returnForward;
    }

    protected boolean isDocumentLocked(ActionMapping mapping, ActionForm form, HttpServletRequest request) {
        KualiTransactionalDocumentFormBase tmpForm = (KualiTransactionalDocumentFormBase) form;
        Document document = tmpForm.getDocument();

        if (document != null) {
            ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            boolean exists = parameterService.parameterExists(document.getClass(),
                    KFSConstants.DOCUMENT_LOCKOUT_PARAM_NM);
            if (exists) {
                boolean documentLockedOut = parameterService.getParameterValueAsBoolean(document.getClass(),
                        KFSConstants.DOCUMENT_LOCKOUT_PARAM_NM);
                if (documentLockedOut) {
                    return tmpForm.getDocumentActions().containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT);
                }
            }
        }

        return false;
    }
}
