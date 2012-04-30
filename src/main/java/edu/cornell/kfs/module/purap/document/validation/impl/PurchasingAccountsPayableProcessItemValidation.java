/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

/**
 * @author dwf5
 *
 */
public class PurchasingAccountsPayableProcessItemValidation extends GenericValidation {

	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
	 */
	public boolean validate(AttributedDocumentEvent event) {
		return true;
	}

}
