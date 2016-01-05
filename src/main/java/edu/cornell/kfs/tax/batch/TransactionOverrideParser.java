package edu.cornell.kfs.tax.batch;

import org.kuali.kfs.sys.batch.FlatFileParserBase;

/**
 * Flat-file parser of TransactionOverride batch files that does not save the uploaded file.
 */
public class TransactionOverrideParser extends FlatFileParserBase {

    @Override
    public boolean shouldSave() {
        return false;
    }

}
