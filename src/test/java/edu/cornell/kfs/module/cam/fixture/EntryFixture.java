package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.gl.businessobject.Entry;

public enum EntryFixture {
	
	VCM_ONE("5319793", "CM"), VCM_TWO("5686500", "CM"), VCM_THREE("0","CM"),
	PREQ_ONE("5773686","PREQ"), PREQ_TWO("5773687", "PREQ"), PREQ_THREE("0", "PREQ");
	
	public final String documentNumber;
	public final String financialDocumentTypeCode;
	
	private EntryFixture(String documentNumber, String financialDocumentTypeCode) {
		this.documentNumber = documentNumber;
		this.financialDocumentTypeCode = financialDocumentTypeCode;
	}

	public Entry createEntry() {
		Entry entry = new Entry();
		entry.setDocumentNumber(documentNumber);
		entry.setFinancialDocumentTypeCode(financialDocumentTypeCode);
		return entry;
	}
}
