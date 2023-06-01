package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.service.CUBankService;

public class CUBankServiceImpl implements CUBankService {
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;


    public Bank getDefaultBankByDocType(final String documentTypeCode) {
        if (StringUtils.isBlank(documentTypeCode)) {
            throw new RuntimeException("Document type not found for document type: " + documentTypeCode);
        }

        if (parameterService.parameterExists(Bank.class, KFSParameterKeyConstants.DEFAULT_BANK_BY_DOCUMENT_TYPE)) {
            final List<String> parmValues = new ArrayList<>(parameterService
                    .getSubParameterValuesAsString(Bank.class, KFSParameterKeyConstants.DEFAULT_BANK_BY_DOCUMENT_TYPE,
                            documentTypeCode));
            if (!parmValues.isEmpty()) {
                final String defaultBankCode = parmValues.get(0);
                final Bank defaultBank = this.getByPrimaryId(defaultBankCode);
                
                // check active status, if not return continuation bank if active
                if (!defaultBank.isActive() && defaultBank.getContinuationBank() != null && defaultBank.getContinuationBank().isActive()) {
                    return defaultBank.getContinuationBank();
                }
                
                return defaultBank;
            }
        }

        return null;
    }

    private Bank getByPrimaryId(final String bankCode) {
        Map primaryKeys = new HashMap();
        primaryKeys.put(KFSPropertyConstants.BANK_CODE, bankCode);

        return (Bank) businessObjectService.findByPrimaryKey(Bank.class, primaryKeys);
    }

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

}