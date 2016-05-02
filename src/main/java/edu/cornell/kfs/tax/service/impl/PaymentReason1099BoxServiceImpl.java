package edu.cornell.kfs.tax.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1099ParameterNames;
import edu.cornell.kfs.tax.service.PaymentReason1099BoxService;

public class PaymentReason1099BoxServiceImpl implements PaymentReason1099BoxService, Serializable {

	private ParameterService parameterService;
	private static final long serialVersionUID = 3831002692873100613L;

	@Override
	public boolean isPaymentReasonMappedTo1099Box(String paymentReasonCode) {
		return convertCollectionToMap(getPaymentReasonTo1099BoxMappings()).containsKey(paymentReasonCode);
	}

	@Override
	public String getPaymentReason1099Box(String paymentReasonCode) {
		return convertCollectionToMap(getPaymentReasonTo1099BoxMappings()).get(paymentReasonCode);
	}

	@Override
	public boolean isPaymentReasonMappedToNo1099Box(String paymentReasonCode) {	
		return getPaymentReasonToNo1099Boxes().contains(paymentReasonCode);
	}
	
	public Collection<String> getPaymentReasonTo1099BoxMappings() {
		return getParameterService().getParameterValuesAsString(CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, 
				Tax1099ParameterNames.PAYMENT_REASON_TO_TAX_BOX);
	}
	
	public Collection<String> getPaymentReasonToNo1099Boxes() {
		return getParameterService().getParameterValuesAsString(CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, 
				Tax1099ParameterNames.PAYMENT_REASON_TO_NO_TAX_BOX);
	}

	public ParameterService getParameterService() {
		if (parameterService == null) {
			parameterService = SpringContext.getBean(ParameterService.class);
		}
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
	
	/**
	 * The format of each collection entry must be "R=2" where R is payment reason code, and 2 is a 1099 box
	 * @param collectionOfStrings
	 * @return
	 */
	public Map<String,String> convertCollectionToMap(Collection<String> collectionOfStrings) {
		HashMap<String, String> returnMap = new HashMap<String,String>();
		for (String stringItem : collectionOfStrings) {
			String[] stringArray = StringUtils.split(stringItem, "=");
			returnMap.put(stringArray[0], stringArray[1]);
		}
		return returnMap;
	}
}
