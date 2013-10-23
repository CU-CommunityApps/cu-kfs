package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.batch.DvToPdpExtractStep;
import org.kuali.kfs.fp.batch.service.impl.DisbursementVoucherExtractServiceImpl;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherExtractServiceImpl extends DisbursementVoucherExtractServiceImpl {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DisbursementVoucherExtractServiceImpl.class);
    private String CP = "CP";
    protected PaymentGroup buildPaymentGroup(CuDisbursementVoucherDocument document, Batch batch) {
        log.debug("buildPaymentGroup() started");

        PaymentGroup pg = new PaymentGroup();
        pg.setBatch(batch);
        pg.setCombineGroups(Boolean.TRUE);
        pg.setCampusAddress(Boolean.FALSE);

        document.refreshReferenceObject(KFSPropertyConstants.DV_PAYEE_DETAIL);
        CuDisbursementVoucherPayeeDetail pd = document.getDvPayeeDetail();
        String rc = pd.getDisbVchrPaymentReasonCode();

        // If the payee is an employee, set these flags accordingly
        if ((pd.isVendor() && SpringContext.getBean(VendorService.class).isVendorInstitutionEmployee(pd.getDisbVchrVendorHeaderIdNumberAsInteger()))
                || document.getDvPayeeDetail().isEmployee()) {
            pg.setEmployeeIndicator(Boolean.TRUE);
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.EMPLOYEE);
            pg.setTaxablePayment(
                    !/*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class,
                                DisbursementVoucherConstants.PAYMENT_REASON_CODE_RENTAL_PAYMENT_PARM_NM).equals(rc)
                        && !parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class,
                                DisbursementVoucherConstants.PAYMENT_REASON_CODE_ROYALTIES_PARM_NM).equals(rc));
        } else if (pd.isStudent() || pd.isAlumni()) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.ENTITY);

            // All payments are taxable except research participant, rental & royalties
            pg.setTaxablePayment(
                    !SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.RENTAL_PAYMENT.equals(rc)
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.ROYALTIES.equals(rc));
        } else {

            // These are taxable
            VendorDetail vendDetail = SpringContext.getBean(VendorService.class).getVendorDetail(pd.getDisbVchrVendorHeaderIdNumberAsInteger(), 
                    pd.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            String vendorOwnerCode = vendDetail.getVendorHeader().getVendorOwnershipCode();
            String vendorOwnerCategoryCode = vendDetail.getVendorHeader().getVendorOwnershipCategoryCode();
            String payReasonCode = pd.getDisbVchrPaymentReasonCode();

            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

            // Assume it is not taxable until proven otherwise
            pg.setTaxablePayment(Boolean.FALSE);
            pg.setPayeeOwnerCd(vendorOwnerCode);

            ParameterEvaluator parameterEvaluator1 = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                    DvToPdpExtractStep.class, PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, 
                       PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, vendorOwnerCode, payReasonCode);
            ParameterEvaluator parameterEvaluator2 = /*REFACTORME*/SpringContext.getBean(
                    ParameterEvaluatorService.class).getParameterEvaluator(DvToPdpExtractStep.class,
                            PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME, 
                            PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME,
                            vendorOwnerCategoryCode, payReasonCode);

            if (parameterEvaluator1.evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (this.parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals(CP) 
                    && StringUtils.isEmpty(vendorOwnerCategoryCode) 
                    && /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DvToPdpExtractStep.class,
                            PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_FOR_BLANK_CORPORATION_OWNERSHIP_TYPE_CATEGORIES_PARAMETER_NAME,
                            payReasonCode).evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (this.parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals(CP)
                        && !StringUtils.isEmpty(vendorOwnerCategoryCode)
                        && parameterEvaluator2.evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            }
        }

        pg.setCity(pd.getDisbVchrPayeeCityName());
        pg.setCountry(pd.getDisbVchrPayeeCountryCode());
        pg.setLine1Address(pd.getDisbVchrPayeeLine1Addr());
        pg.setLine2Address(pd.getDisbVchrPayeeLine2Addr());
        pg.setPayeeName(pd.getDisbVchrPayeePersonName());
        pg.setPayeeId(pd.getDisbVchrPayeeIdNumber());
        pg.setState(pd.getDisbVchrPayeeStateCode());
        pg.setZipCd(pd.getDisbVchrPayeeZipCode());
        pg.setPaymentDate(document.getDisbursementVoucherDueDate());

        // It doesn't look like the DV has a way to do immediate processes
        pg.setProcessImmediate(Boolean.FALSE);
        pg.setPymtAttachment(document.isDisbVchrAttachmentCode());
        pg.setPymtSpecialHandling(document.isDisbVchrSpecialHandlingCode());
        pg.setNraPayment(pd.isDisbVchrAlienPaymentCode());

        pg.setBankCode(document.getDisbVchrBankCode());
        pg.setPaymentStatusCode(KFSConstants.PdpConstants.PAYMENT_OPEN_STATUS_CODE);

        return pg;
    }

}
