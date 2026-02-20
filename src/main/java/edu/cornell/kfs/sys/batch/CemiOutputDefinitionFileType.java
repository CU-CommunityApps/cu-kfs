package edu.cornell.kfs.sys.batch;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;

public class CemiOutputDefinitionFileType extends CuXmlBatchInputFileTypeBase<CemiOutputDefinition> {

    @Override
    public String getFileTypeIdentifier() {
        return CemiBaseConstants.CEMI_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public String getTitleKey() {
        return CUKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_CEMI_OUTPUT_DEFINITION;
    }

}
