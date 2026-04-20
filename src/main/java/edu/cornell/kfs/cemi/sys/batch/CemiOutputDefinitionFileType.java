package edu.cornell.kfs.cemi.sys.batch;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiKFSKeyConstants;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.CuXmlBatchInputFileTypeBase;

public class CemiOutputDefinitionFileType extends CuXmlBatchInputFileTypeBase<CemiOutputDefinition> {

    @Override
    public String getFileTypeIdentifier() {
        return CemiBaseConstants.CEMI_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public String getTitleKey() {
        return CemiKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_CEMI_OUTPUT_DEFINITION;
    }

}
