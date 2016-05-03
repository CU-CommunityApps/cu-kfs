package edu.cornell.kfs.tax.businessobject;

import java.util.List;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

/**
 * Helper BO for holding data about invalid transaction override lines as needed.
 */
public class TransactionOverrideHeader extends TransientBusinessObjectBase {
    private static final long serialVersionUID = 8622645115646743556L;

    private String header;
    private List<String> invalidLines;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<String> getInvalidLines() {
        return invalidLines;
    }

    public void setInvalidLines(List<String> invalidLines) {
        this.invalidLines = invalidLines;
    }

}
