package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.businessobject.CuObjectCodeActivationGlobal;
import edu.cornell.kfs.coa.businessobject.CuObjectCodeGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

@SuppressWarnings("deprecation")
public class CuObjectCodeActivationGlobalMaintenanceDocumentRule extends GlobalDocumentRuleBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuObjectCodeActivationGlobalMaintenanceDocumentRule.class);
    private static final String NEW_OBJECT_CODE_PROPERTY_NAME = "add.objectCodeGlobalDetails.financialObjectCode";
    
    private transient ObjectCodeService objectCodeService;
    
    @Override
    public boolean processRouteDocument(Document document) {
        boolean canRoute = super.processRouteDocument(document) && validObjectCodeList((MaintenanceDocument)document);
        return canRoute;
    }
    
    protected boolean validObjectCodeList(MaintenanceDocument maintenanceDocument) {
        CuObjectCodeActivationGlobal objectCodeGlobal = (CuObjectCodeActivationGlobal) maintenanceDocument.getDocumentBusinessObject();
        boolean valid = true;
        if (CollectionUtils.isEmpty(objectCodeGlobal.getObjectCodeGlobalDetails())) {
            LOG.debug("validObjectCodeList, no object codes selected to be edited");
            valid = false;
            putFieldError(NEW_OBJECT_CODE_PROPERTY_NAME, CUKFSKeyConstants.OBJECT_CODE_ACTIVATION_GLOBAL_OBJECT_CODE_LIST_ERROR);
        } else {
            for (CuObjectCodeGlobalDetail detail : objectCodeGlobal.getObjectCodeGlobalDetails()) {
                boolean thisDetailValid = validateCuObjectCodeGlobalDetail(detail);
                valid &= thisDetailValid;
                if (!thisDetailValid) {
                    putFieldError(NEW_OBJECT_CODE_PROPERTY_NAME, CUKFSKeyConstants.OBJECT_CODE_ACTIVATION_GLOBAL_OBJECT_CODE_NEW_CODE_ERROR, detail.getFinancialObjectCode());
                }
            }
        }
        return valid;
    }
    
    @Override
    public boolean processAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName,
            PersistableBusinessObject bo) {
        LOG.debug("processAddCollectionLineBusinessRules, entering");
        boolean valid = super.processAddCollectionLineBusinessRules(document, collectionName, bo);
        
        CuObjectCodeGlobalDetail newDetail = (CuObjectCodeGlobalDetail) bo;
        
        if (valid && !validateCuObjectCodeGlobalDetail(newDetail)) {
            valid = false;
            putFieldError(NEW_OBJECT_CODE_PROPERTY_NAME, CUKFSKeyConstants.OBJECT_CODE_ACTIVATION_GLOBAL_OBJECT_CODE_NEW_CODE_ERROR, newDetail.getFinancialObjectCode());
        }
        
        return valid;
    }
    
    protected boolean validateCuObjectCodeGlobalDetail(CuObjectCodeGlobalDetail detail) {
        ObjectCode objectCode = getObjectCodeService().getByPrimaryId(detail.getUniversityFiscalYear(), detail.getChartOfAccountsCode(), detail.getFinancialObjectCode());
        return ObjectUtils.isNotNull(objectCode);
    }

    public ObjectCodeService getObjectCodeService() {
        if (objectCodeService == null) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }
    
}
