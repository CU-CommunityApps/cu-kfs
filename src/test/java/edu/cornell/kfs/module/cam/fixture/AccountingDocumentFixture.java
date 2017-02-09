package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.fp.document.CapitalAssetInformationDocumentBase;
import org.kuali.kfs.sys.document.AccountingDocument;

public enum AccountingDocumentFixture {

	ONE();
	
	private AccountingDocumentFixture() {
		
	}
	
	public AccountingDocument createAccountingDocument() {
		
		CapitalAssetInformationDocumentBase c = new CapitalAssetInformationDocumentBase();

		
		
		return (AccountingDocument) c;
	}
}
