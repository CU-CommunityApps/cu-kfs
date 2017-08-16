package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.gl.businessobject.Entry;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;

public enum EntryFixture {
	
	VCM_ONE(CuCamsTestConstants.DOC_5319793, "CM"), VCM_TWO(CuCamsTestConstants.DOC_5686500, "CM"), VCM_THREE("0", "CM"),
	VCM_FOUR(CuCamsTestConstants.DOC_5686501, "CM"),
	PREQ_ONE(CuCamsTestConstants.DOC_5773686, "PREQ"), PREQ_TWO(CuCamsTestConstants.DOC_5773687, "PREQ"), PREQ_THREE("0", "PREQ");
	
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
