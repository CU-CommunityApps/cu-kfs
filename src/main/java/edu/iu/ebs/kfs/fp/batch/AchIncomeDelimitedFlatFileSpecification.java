package edu.iu.ebs.kfs.fp.batch;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.DelimitedFlatFileSpecification;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;


public class AchIncomeDelimitedFlatFileSpecification extends DelimitedFlatFileSpecification {
    private String endOfLineCharacter;

    @Override
    public void parseLineIntoObject(FlatFileObjectSpecification parseSpecification, String lineToParse, Object parseIntoObject, int lineNumber) {
        super.parseLineIntoObject(parseSpecification, StringUtils.removeEnd(lineToParse.trim(), endOfLineCharacter), parseIntoObject, lineNumber);
    }

    public void setEndOfLineCharacter(String endOfLineCharacter) {
        this.endOfLineCharacter = endOfLineCharacter;
    }
}
