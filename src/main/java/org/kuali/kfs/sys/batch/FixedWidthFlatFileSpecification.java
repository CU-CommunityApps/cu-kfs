package org.kuali.kfs.sys.batch;

/**
 * Concrete extension of AbstractFlatFileSpecificationBase which can handle the parsing of lines where substrings
 * are parsed by knowing the beginning and ending position of the substring
 */
public class FixedWidthFlatFileSpecification extends AbstractFlatFileSpecificationBase {
    /**
     * Parses a line by pulling out substrings given by the FlatFilePropertySpecification configuration objects passed in
     * @see org.kuali.kfs.sys.batch.FlatFileSpecification#parseLineIntoObject(FlatFileObjectSpecification, String, Object)
     */
    public void parseLineIntoObject(FlatFileObjectSpecification parseSpecification, String lineToParse, Object parseIntoObject) {
        // loop through the properties to format and set the property values
        // from the input line
        for (FlatFilePropertySpecification propertySpecification : parseSpecification.getParseProperties()) {
            int start = ((FixedWidthFlatFilePropertySpecification) propertySpecification).getStart();
            int end = ((FixedWidthFlatFilePropertySpecification) propertySpecification).getEnd();
            // if end is not specified, read to the end of line
            if (end == 0) {
                end = lineToParse.length();
            }
            String subString = lineToParse.substring(start, end);
            propertySpecification.setProperty(subString, parseIntoObject);
        }
    }
}
