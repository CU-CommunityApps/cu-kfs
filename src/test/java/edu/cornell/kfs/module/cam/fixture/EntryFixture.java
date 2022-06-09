package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.module.purap.PurapConstants;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;

public enum EntryFixture {
	
	VCM_5319793(CuCamsTestConstants.DOC_5319793, PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT),
	VCM_5686500(CuCamsTestConstants.DOC_5686500, PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT),
	VCM_5686501(CuCamsTestConstants.DOC_5686501, PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT),
	VCM_NONEXISTENT_DOC("0", PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT),
	PREQ_5773686(CuCamsTestConstants.DOC_5773686, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT),
	PREQ_5773687(CuCamsTestConstants.DOC_5773687, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT),
	PREQ_NONEXISTENT_DOC("0", PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
	
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
