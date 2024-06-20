package edu.cornell.kfs.module.purap.batch;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantDocumentBatchFeed;
import edu.cornell.kfs.sys.batch.CuXmlBatchInputFileTypeBase;

public class IWantDocumentInputFileType extends CuXmlBatchInputFileTypeBase<IWantDocumentBatchFeed> {
    
    @Override
    public String getFileTypeIdentifier() {
        return CUPurapConstants.I_WANT_DOC_FEED_FILE_TYPE_INDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getTitleKey() {
        return CUPurapKeyConstants.MESSAGE_BATCH_FEED_TITLE_IWANT_DOC;
    }

}
