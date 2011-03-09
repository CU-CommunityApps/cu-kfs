package edu.cornell.kfs.coa.document;

import org.kuali.kfs.coa.businessobject.AccountGlobalExtendedAttribute;
import org.kuali.kfs.coa.document.AccountGlobalMaintainableImpl;

public class CUAccountGlobalMaintainableImpl extends AccountGlobalMaintainableImpl {

	@Override
	public void saveBusinessObject() {
		
		AccountGlobalExtendedAttribute agea = (AccountGlobalExtendedAttribute)businessObject.getExtension();
		agea.setDocumentNumber(documentNumber);
		// TODO Auto-generated method stub
		super.saveBusinessObject();
	}
	
}
