package edu.cornell.kfs.fp.document.service.impl;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.kuali.kfs.fp.batch.DvToPdpExtractStep;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherExtractionHelperServiceImpl;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherExtractionHelperService;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;


public class CuDisbursementVoucherExtractionHelperServiceImpl extends DisbursementVoucherExtractionHelperServiceImpl implements CuDisbursementVoucherExtractionHelperService {
    static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherExtractionHelperServiceImpl.class);
    protected CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;

    @Override
    public PaymentGroup createPaymentGroup(DisbursementVoucherDocument document, Date processRunDate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createPaymentGroupForDisbursementVoucher() started");
        }

        PaymentGroup pg = new PaymentGroup();
        pg.setCombineGroups(Boolean.TRUE);
        pg.setCampusAddress(Boolean.FALSE);

        CuDisbursementVoucherPayeeDetail pd = 
        		businessObjectService.findBySinglePrimaryKey(CuDisbursementVoucherPayeeDetail.class, document.getDocumentNumber());
        String rc = pd.getDisbVchrPaymentReasonCode();

        if (KFSConstants.PaymentPayeeTypes.CUSTOMER.equals(document.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode())) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.CUSTOMER);
            pg.setTaxablePayment(Boolean.FALSE);
        }
        // If the payee is an employee, set these flags accordingly
        else if ((pd.isVendor() && SpringContext.getBean(VendorService.class).isVendorInstitutionEmployee(pd.getDisbVchrVendorHeaderIdNumberAsInteger()))
                    || document.getDvPayeeDetail().isEmployee()) {
            pg.setEmployeeIndicator(Boolean.TRUE);
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.EMPLOYEE);
            pg.setTaxablePayment(
                    !/*REFACTORME*/getParameterEvaluatorService().getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.PAYMENT_REASON_CODE_RENTAL_PAYMENT_PARM_NM).equals(rc)
                        && !getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.PAYMENT_REASON_CODE_ROYALTIES_PARM_NM).equals(rc));
        }
        // KFSUPGRADE-973 : Cu mods
        // If the payee is an alumni or student, set these flags accordingly
        else if(pd.isStudent() || pd.isAlumni()) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.ENTITY);

            // All payments are taxable except research participant, rental & royalties
            pg.setTaxablePayment(
                    !SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.RENTAL_PAYMENT.equals(rc)
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.ROYALTIES.equals(rc));
        }
        else {

            // These are taxable
            VendorDetail vendDetail = getVendorService().getVendorDetail(pd.getDisbVchrVendorHeaderIdNumberAsInteger(), pd.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            String vendorOwnerCode = vendDetail.getVendorHeader().getVendorOwnershipCode();
            String vendorOwnerCategoryCode = vendDetail.getVendorHeader().getVendorOwnershipCategoryCode();
            String payReasonCode = pd.getDisbVchrPaymentReasonCode();

            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

            // Assume it is not taxable until proven otherwise
            pg.setTaxablePayment(Boolean.FALSE);
            pg.setPayeeOwnerCd(vendorOwnerCode);

            ParameterEvaluator parameterEvaluator1 = /*REFACTORME*/getParameterEvaluatorService().getParameterEvaluator(DvToPdpExtractStep.class, PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, vendorOwnerCode, payReasonCode);
            ParameterEvaluator parameterEvaluator2 = /*REFACTORME*/getParameterEvaluatorService().getParameterEvaluator(DvToPdpExtractStep.class, PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME, PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME, vendorOwnerCategoryCode, payReasonCode);

            if ( parameterEvaluator1.evaluationSucceeds() ) {
                pg.setTaxablePayment(Boolean.TRUE);
            }
            else if (getParameterService().getParameterValueAsString(DvToPdpExtractStep.class, PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals("CP") &&
                      StringUtils.isEmpty(vendorOwnerCategoryCode) &&
                      /*REFACTORME*/getParameterEvaluatorService().getParameterEvaluator(DvToPdpExtractStep.class, PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_FOR_BLANK_CORPORATION_OWNERSHIP_TYPE_CATEGORIES_PARAMETER_NAME, payReasonCode).evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            }
            else if (getParameterService().getParameterValueAsString(DvToPdpExtractStep.class, PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals("CP")
                        && !StringUtils.isEmpty(vendorOwnerCategoryCode)
                        && parameterEvaluator2.evaluationSucceeds() ) {
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
        pg.setProcessImmediate(document.isImmediatePaymentIndicator());
        pg.setPymtAttachment(document.isDisbVchrAttachmentCode());
        pg.setPymtSpecialHandling(document.isDisbVchrSpecialHandlingCode());
        pg.setNraPayment(pd.isDisbVchrAlienPaymentCode());

        pg.setBankCode(document.getDisbVchrBankCode());
        pg.setPaymentStatusCode(PdpConstants.PaymentStatusCodes.OPEN);

        // now add the payment detail
        final PaymentDetail paymentDetail = buildPaymentDetail(document, processRunDate);
        pg.addPaymentDetails(paymentDetail);
        paymentDetail.setPaymentGroup(pg);

        return pg;
    }

    public Map<String, List<DisbursementVoucherDocument>> retrievePaymentSourcesByCampus(boolean immediatesOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrievePaymentSourcesByCampus() started");
        }

        if (immediatesOnly) {
            throw new UnsupportedOperationException("DisbursementVoucher PDP does immediates extraction through normal document processing; immediates for DisbursementVoucher should not be run through batch.");
        }

        Map<String, List<DisbursementVoucherDocument>> documentsByCampus = new HashMap<String, List<DisbursementVoucherDocument>>();

        Collection<DisbursementVoucherDocument> docs = disbursementVoucherDao.getDocumentsByHeaderStatus(KFSConstants.DocumentStatusCodes.APPROVED, false);
        for (DisbursementVoucherDocument element : docs) {
            String dvdCampusCode = element.getCampusCode();
            if (StringUtils.isNotBlank(dvdCampusCode)) {
                if (documentsByCampus.containsKey(dvdCampusCode) 
                		// KFSUPGRADE-973
                		&& getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(element.getDisbVchrPaymentMethodCode())) {

                    List<DisbursementVoucherDocument> documents = documentsByCampus.get(dvdCampusCode);
                    documents.add(element);
                }
                else {
                    List<DisbursementVoucherDocument> documents = new ArrayList<DisbursementVoucherDocument>();
                    documents.add(element);
                    documentsByCampus.put(dvdCampusCode, documents);
                }
            }
        }

        return documentsByCampus;
    }

    // KFSPTS-1891 : calling PaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp
    // TODO :  this method does not seem to be referenced.  
//  @Override
    protected Set<String> getCampusListByDocumentStatusCode(String statusCode) {
        LOG.debug("getCampusListByDocumentStatusCode() started");

        Set<String> campusSet = new HashSet<String>();

        Collection<DisbursementVoucherDocument> docs = disbursementVoucherDao.getDocumentsByHeaderStatus(statusCode, false);
        for (DisbursementVoucherDocument doc : docs) {
            if ( getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(doc.getDisbVchrPaymentMethodCode()) ) {
                campusSet.add(doc.getCampusCode());
            }
        }

        return campusSet;
    }

	public CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
		return paymentMethodGeneralLedgerPendingEntryService;
	}

	public void setPaymentMethodGeneralLedgerPendingEntryService(
			CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
		this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
	}
	   
	   @Override
	   public boolean shouldExtractPayment(DisbursementVoucherDocument paymentSource) {
	       LOG.debug("paymentSource: " + paymentSource.getClass());
	       if (isRecurringDV(paymentSource)) {
	           LOG.debug("shouldExtractPayment: found a recurring DV, returning false for " + paymentSource.getDocumentNumber());
	           return false;
	       }
	       boolean shouldExtract = super.shouldExtractPayment(paymentSource);
	       LOG.debug("shouldExtractPayment: Found a non recurring DV with a document number of " + paymentSource.getDocumentNumber() + " and returning " + shouldExtract);
	       return shouldExtract;
	   }
	   
	   private boolean isRecurringDV(DisbursementVoucherDocument paymentSource) {
	       boolean isRecurringDV = false;
	       if (CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME.equalsIgnoreCase(
                   paymentSource.getDocumentHeader().getWorkflowDocument().getDocumentTypeName())) {
               isRecurringDV = true;
           }
	       return isRecurringDV;
	   }
}
