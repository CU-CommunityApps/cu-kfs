package org.kuali.kfs.ksr.service.impl;

import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.document.validation.AddQualificationLineEvent;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.kfs.ksr.service.SecurityRequestDocumentService;
import org.kuali.kfs.ksr.web.form.SecurityRequestDocumentForm;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.service.impl.DocumentViewHelperServiceImpl;
import org.kuali.rice.krad.uif.view.ViewModel;

/**
 * ====
 * CU Customization:
 * Added this class to help process security request doc addLines via KRAD,
 * as well as to support dynamic setting of qualification detail form controls
 * and value finders.
 * ====
 */
public class SecurityRequestDocumentViewHelperServiceImpl extends DocumentViewHelperServiceImpl {

    private static final long serialVersionUID = 6074764181103472326L;

    protected static final String NEW_QUALIFICATION_ERROR_PATH_SUFFIX = ".newRequestRoleQualification";

    protected transient SecurityRequestDocumentService securityRequestDocumentService;

    /**
     * Overridden to perform proper validation of security request qualification addLines.
     * 
     * @see org.kuali.rice.krad.uif.service.impl.DocumentViewHelperServiceImpl#performAddLineValidation(
     * org.kuali.rice.krad.uif.view.ViewModel, java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    protected boolean performAddLineValidation(ViewModel viewModel, Object newLine, String collectionId, String collectionPath) {
        boolean isValid = super.performAddLineValidation(viewModel, newLine, collectionId, collectionPath);
        
        if (newLine instanceof SecurityRequestRoleQualification) {
            SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) viewModel;
            SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
            SecurityRequestRoleQualification newQualification = (SecurityRequestRoleQualification) newLine;
            
            String errorPath = collectionPath + NEW_QUALIFICATION_ERROR_PATH_SUFFIX;
            
            isValid &= getKualiRuleService().applyRules(
                    new AddQualificationLineEvent(errorPath, document, newQualification));
        }
        
        return isValid;
    }

    /**
     * Overridden to properly initialize newly-created qualification addLine objects if necessary.
     * 
     * @see org.kuali.rice.krad.uif.service.impl.ViewHelperServiceImpl#applyDefaultValuesForCollectionLine(
     * org.kuali.rice.krad.uif.container.CollectionGroup, java.lang.Object)
     */
    @Override
    public void applyDefaultValuesForCollectionLine(CollectionGroup collectionGroup, Object line) {
        if (line instanceof SecurityRequestRoleQualification) {
            SecurityRequestRoleQualification lineQualification = (SecurityRequestRoleQualification) line;
            SecurityRequestRole requestRole = (SecurityRequestRole) collectionGroup.getContext()
                    .get(UifConstants.ContextVariableNames.PARENT_LINE);
            
            if (CollectionUtils.isEmpty(lineQualification.getRoleQualificationDetails())
                    && requestRole != null && requestRole.isQualifiedRole()) {
                SecurityRequestRoleQualification newQualification = getSecurityRequestDocumentService().buildRoleQualificationLine(
                        requestRole, Collections.emptyMap());
                copyQualificationData(newQualification, lineQualification);
            }
        }
        
        super.applyDefaultValuesForCollectionLine(collectionGroup, line);
    }

    protected void copyQualificationData(SecurityRequestRoleQualification source, SecurityRequestRoleQualification target) {
        target.setDocumentNumber(source.getDocumentNumber());
        target.setQualificationId(source.getQualificationId());
        target.setRoleRequestId(source.getRoleRequestId());
        target.setRoleQualificationDetails(source.getRoleQualificationDetails());
    }

    public SecurityRequestDocumentService getSecurityRequestDocumentService() {
        if (securityRequestDocumentService == null) {
            securityRequestDocumentService = KSRServiceLocator.getSecurityRequestDocumentService();
        }
        return securityRequestDocumentService;
    }

    public void setSecurityRequestDocumentService(SecurityRequestDocumentService securityRequestDocumentService) {
        this.securityRequestDocumentService = securityRequestDocumentService;
    }

}
