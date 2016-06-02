package edu.cornell.kfs.tax.service.impl;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.service.DocumentType1099BoxService;

public class DocumentType1099BoxServiceImpl implements DocumentType1099BoxService {

    protected ParameterService parameterService;

    @Override
    public boolean isDocumentTypeMappedTo1099Box(String documentTypeName) {
        return StringUtils.isNotBlank(getDocumentType1099Box(documentTypeName));
    }

    @Override
    public String getDocumentType1099Box(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            return null;
        }
        Collection<String> mappings = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, CUTaxConstants.Tax1099ParameterNames.DOCUMENT_TYPE_TO_TAX_BOX);
        return find1099Box(documentTypeName, mappings);
    }

    /**
     * Helper method for finding the tax box for the given document type (if any),
     * by searching through mappings of this form: "key1=value1", "key2=value2", ... , "keyN=valueN"
     */
    protected String find1099Box(String documentTypeName, Collection<String> mappings) {
        for (String mapping : mappings) {
            int equalsSignIndex = mapping.indexOf('=');
            if (documentTypeName.equals(mapping.substring(0, equalsSignIndex))) {
                return mapping.substring(equalsSignIndex + 1);
            }
        }
        return null;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
