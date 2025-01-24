package edu.cornell.kfs.tax.batch.dto;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

/**
 * This object will be fully implemented by one of the follow-up Sprintax user stories.
 */
@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = TransactionDetail.class)
})
public class SprintaxInfo1042S {

    @TaxDtoField(actualBOField = "vendorName")
    private String vendorNameForOutput;

    public String getVendorNameForOutput() {
        return vendorNameForOutput;
    }

    public void setVendorNameForOutput(final String vendorNameForOutput) {
        this.vendorNameForOutput = vendorNameForOutput;
    }

}
