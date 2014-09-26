package edu.cornell.kfs.module.cab.fixture;

import java.util.List;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.document.CapitalAssetEditable;
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
