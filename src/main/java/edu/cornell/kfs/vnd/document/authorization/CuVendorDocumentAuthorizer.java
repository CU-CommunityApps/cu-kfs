package edu.cornell.kfs.vnd.document.authorization;

import java.util.Set;

import org.kuali.kfs.vnd.document.authorization.VendorDocumentAuthorizer;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;

public class CuVendorDocumentAuthorizer extends VendorDocumentAuthorizer {
	@Override
	public Set<String> getDocumentActions(Document document, Person user,
			Set<String> documentActions) {
		Set<String> documentActions1 = super.getDocumentActions(document, user, documentActions);
		// KFSPTS-2055
		if (documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_APPROVE)
				&& !documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_SAVE)) {
			documentActions1.add(KRADConstants.KUALI_ACTION_CAN_SAVE);
		}
		return documentActions1;
	}    

}
