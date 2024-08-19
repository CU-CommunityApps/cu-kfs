package edu.cornell.kfs.fp.batch;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentUnmarshalListener;
import edu.cornell.kfs.sys.batch.CuXmlBatchInputFileTypeBase;

public class AccountingXmlDocumentInputFileType extends CuXmlBatchInputFileTypeBase<AccountingXmlDocumentListWrapper> {

    @Override
    public String getFileTypeIdentifier() {
        return CuFPConstants.ACCOUNTING_XML_DOCUMENT_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public String getTitleKey() {
        return CuFPKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_ACCOUNTING_XML_DOCUMENT;
    }

    @Override
    public AccountingXmlDocumentListWrapper parse(final byte[] fileByteContent) {
        return parse(fileByteContent, new AccountingXmlDocumentUnmarshalListener());
    }

}
