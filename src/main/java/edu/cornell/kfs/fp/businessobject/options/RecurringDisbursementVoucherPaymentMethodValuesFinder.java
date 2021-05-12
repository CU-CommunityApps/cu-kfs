package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.fp.CuFPConstants;

public class RecurringDisbursementVoucherPaymentMethodValuesFinder extends PaymentMethodsForDVValuesFinder {
	private static final long serialVersionUID = 3085641379675595140L;
	protected ParameterService parameterService;

	@Override
	public List<KeyValue> getKeyValues() {
		return filterValues(super.getKeyValues());
	}
	
	protected List<KeyValue> filterValues(List<KeyValue> keyValues) {
		List<KeyValue> filteredValues = new ArrayList<KeyValue>();
		for (KeyValue kv : keyValues) {
			if (!isFilteredKey(kv.getKey())) {
				filteredValues.add(kv);
			}
		}
		return filteredValues;
	}
	
	protected boolean isFilteredKey(String key) {
		String filters = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
				CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_COMPONENT_NAME, 
				CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_PAYMENT_METHOD_FILTER_PARAMETER_NAME);
		if (StringUtils.isEmpty(key)) {
			return false;
		} else {
			return StringUtils.containsIgnoreCase(filters, key);
		}
	}
	
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

}
