package edu.cornell.kfs.module.purap.batch;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.batch.CuXmlBatchInputFileTypeBase;

public class JaggaerXMLInputFileType extends CuXmlBatchInputFileTypeBase<SupplierSyncMessage> {

    @Override
    public String getFileTypeIdentifier() {
        return CUPurapConstants.JAGGAER_XML_INPUT_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getTitleKey() {
        return CUPurapKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_JAGGAER;
    }

}
