package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.IntraAccountAdjustmentDocument;
import org.kuali.kfs.fp.document.validation.impl.IntraAccountAdjustmentSubAccountValidation;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuIntraAccountAdjustmentSubAccountValidation extends IntraAccountAdjustmentSubAccountValidation {

	@Override
	public boolean validate(final AttributedDocumentEvent event) {
		// check system parameter to see if validation is needed
		boolean validate = false;
		validate = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(IntraAccountAdjustmentDocument.class, CUKFSParameterKeyConstants.FpParameterConstants.FP_VALIDATE_CS_SUB_ACCOUNT_OR_ICR_ATTRIBUTES);

		if (!validate) {
			return true;
		} else {
			return super.validate(event);
		}
	}

}
