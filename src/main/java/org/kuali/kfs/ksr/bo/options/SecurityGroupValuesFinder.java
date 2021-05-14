package org.kuali.kfs.ksr.bo.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;
import org.kuali.rice.krad.uif.view.ViewModel;

/**
 * ====
 * CU Customization:
 * Added this values finder for obtaining security groups.
 * ====
 */
public class SecurityGroupValuesFinder extends UifKeyValuesFinderBase {

	private static final long serialVersionUID = 7419336891652450867L;

	public List<KeyValue> getKeyValues(ViewModel model) {
		List<KeyValue> values = new ArrayList<KeyValue>();
		
		for (SecurityGroup securityGroup : KSRServiceLocator.getSecurityRequestDocumentService().getActiveSecurityGroups()) {
			values.add(new ConcreteKeyValue(securityGroup.getSecurityGroupId().toString(), securityGroup.getSecurityGroupName()));
		}
		
		return values;
	}
}
