package edu.cornell.kfs.fp.businessobject;


import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;
import org.kuali.kfs.sys.context.SpringContext;

public class CuDisbursementVoucherPayeeDetail extends org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail {

    /**
     * 
     */
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

}
