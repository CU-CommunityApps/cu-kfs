package edu.cornell.kfs.concur.batch.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.DelimitedFlatFileSpecification;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;

public class ConcurStandardAccountingExtractFileSpecification extends DelimitedFlatFileSpecification {
    @Override
    public void parseLineIntoObject(FlatFileObjectSpecification parseSpecification, String lineToParse, Object parseIntoObject, int lineNumber) {
        super.parseLineIntoObject(parseSpecification, removeEndOfLineCharacter(lineToParse), parseIntoObject, lineNumber);
    }

    protected String removeEndOfLineCharacter(String lineToParse) {
        if (StringUtils.isBlank(lineToParse))
            return lineToParse;
        else {
            return lineToParse.substring(0, lineToParse.length() - 1);
        }
    }
}
