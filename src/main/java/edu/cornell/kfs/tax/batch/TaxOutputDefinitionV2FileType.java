package edu.cornell.kfs.tax.batch;

import edu.cornell.kfs.sys.batch.CuXmlBatchInputFileTypeBase;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;

public class TaxOutputDefinitionV2FileType extends CuXmlBatchInputFileTypeBase<TaxOutputDefinition> {

    @Override
    public String getFileTypeIdentifier() {
        return CUTaxConstants.TAX_OUTPUT_DEFINITION_V2_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public String getTitleKey() {
        return CUTaxKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_TAX_OUTPUT_DEFINITION_V2;
    }

}
