/**
 * 
 */
package edu.cornell.kfs.coa.document;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.KualiAccountMaintainableImpl;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;

/**
 * @author kwk43
 *
 */
public class CUAccountMaintainableImpl extends KualiAccountMaintainableImpl {

	@Override
	public void saveBusinessObject() {
		Account account = (Account) getBusinessObject();
		AccountExtendedAttribute aea = (AccountExtendedAttribute)(account.getExtension());
		aea.setSubFundGroupCode(account.getSubFundGroupCode());
		
		// TODO Auto-generated method stub
		super.saveBusinessObject();
	}
	
}
