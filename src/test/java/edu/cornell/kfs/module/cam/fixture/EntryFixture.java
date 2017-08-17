package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.module.cam.CamsConstants;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;

public enum EntryFixture {
	
	VCM_5319793(CuCamsTestConstants.DOC_5319793, CamsConstants.CM),
	VCM_5686500(CuCamsTestConstants.DOC_5686500, CamsConstants.CM),
	VCM_5686501(CuCamsTestConstants.DOC_5686501, CamsConstants.CM),
	VCM_NONEXISTENT_DOC("0", CamsConstants.CM),
	PREQ_5773686(CuCamsTestConstants.DOC_5773686, CamsConstants.PREQ),
	PREQ_5773687(CuCamsTestConstants.DOC_5773687, CamsConstants.PREQ),
	PREQ_NONEXISTENT_DOC("0", CamsConstants.PREQ);
	
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
