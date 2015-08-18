package edu.cornell.kfs.tax.batch;

/**
 * Helper object defining the output of a single field or piece
 * to the relevant tax output file.
 * 
 * <p>Contains the following properties:</p>
 * 
 * <ol>
 *   <li>name - The name of the tax field; intended for developer or definition-XML-creator convenience.</li>
 *   <li>length - Either the max length or the exact length of the field, depending on the output format; must be a nonnegative integer.</li>
 *   <li>type - The type of field; must equal the string name of one of the CUTaxBatchConstants.TaxFieldSource enum constants.</p>
 *   <li>value - A generic value; can be blank, a literal value, or the string name/alias of a tax source field, depending on the type.</li>
 * </ol>
 */
public class TaxOutputField {
    // The name of the field; primarily for convenience, not actually used for processing.
    private String name;
    // The max length or exact length of the field, depending on the format.
    private Integer length;
    // The type of field; should match one of the CUTaxBatchConstants.TaxFieldSource enum constants.
    private String type;
    // The value; can be a literal value or a field name/alias, depending on the type.
    private String value;

    public TaxOutputField() {
        // Do nothing.
    }



    /**
     * Returns the user-defined name of the field; primarily used as a convenience for
     * those reading the XML file or debugging the code, not actually used by the tax processing.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the exact length or max length of this field, depending on the configuration
     * of the TaxOutputSection that this field belongs to.
     */
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Returns the type/source of the field; must match the String name of a
     * CUTaxBatchConstants.TaxFieldSource enum constant.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the value of this field; may be a literal value or a field name/alias,
     * depending on the type.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
