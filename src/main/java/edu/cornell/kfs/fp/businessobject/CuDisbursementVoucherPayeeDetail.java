package edu.cornell.kfs.fp.businessobject;


import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;

public class CuDisbursementVoucherPayeeDetail extends org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail {

    private static final long serialVersionUID = 1L;

    /**
     * Checks the payee type code for student type
     */
    public boolean isStudent() {
        return SpringContext.getBean(CuDisbursementVoucherPayeeService.class).isStudent(this);
    }

    /**
     * Checks the payee type code for alumni type
     */
    public boolean isAlumni() {
        return SpringContext.getBean(CuDisbursementVoucherPayeeService.class).isAlumni(this);
    }

    /**
     * Overridden to separate the DisbursementVoucherPayeeService retrieval to a separate method for unit testing
     * convenience, and to also append the getPayeeTypeSuffixForDisplay() value from the extension attribute.
     * 
     * @see org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail#getDisbursementVoucherPayeeTypeName()
     */
    @Override
    public String getDisbursementVoucherPayeeTypeName() {
        return getDisbursementVoucherPayeeService().getPayeeTypeDescription(getDisbursementVoucherPayeeTypeCode())
                + ((CuDisbursementVoucherPayeeDetailExtension) this.getExtension()).getPayeeTypeSuffixForDisplay();
    }

    protected DisbursementVoucherPayeeService getDisbursementVoucherPayeeService() {
        return SpringContext.getBean(DisbursementVoucherPayeeService.class);
    }

}
