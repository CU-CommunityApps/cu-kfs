package edu.cornell.kfs.tax.batch;

import java.io.File;

import edu.cornell.kfs.sys.batch.service.DigesterXmlBatchInputFileType;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;

/**
 * A batch input file type for creating TaxOutputDefinition instances from XML.
 */
public class TaxOutputDefinitionFileType extends DigesterXmlBatchInputFileType {

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return null;
    }

    @Override
    public String getFileTypeIdentifier() {
        return CUTaxConstants.TAX_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(File file) {
        return null;
    }

    @Override
    public String getTitleKey() {
        return CUTaxKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_TAX_OUTPUT_DEFINITION;
    }

}
