package edu.cornell.kfs.fp.document.service.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPayeeServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;



public class CuDisbursementVoucherPayeeServiceImpl extends DisbursementVoucherPayeeServiceImpl implements CuDisbursementVoucherPayeeService {

	private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherPayeeServiceImpl.class);

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#getPayeeFromVendor(org.kuali.kfs.vnd.businessobject.VendorDetail)
     */
    @Override
    public CuDisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail) {
        CuDisbursementPayee disbursementPayee = new CuDisbursementPayee();

        disbursementPayee.setActive(vendorDetail.isActiveIndicator());

        disbursementPayee.setPayeeIdNumber(vendorDetail.getVendorNumber());
        disbursementPayee.setPayeeName(vendorDetail.getAltVendorName());
        disbursementPayee.setTaxNumber(vendorDetail.getVendorHeader().getVendorTaxNumber());

        String vendorTypeCode = vendorDetail.getVendorHeader().getVendorTypeCode();
        String payeeTypeCode = getVendorPayeeTypeCodeMapping().get(vendorTypeCode);
        disbursementPayee.setPayeeTypeCode(payeeTypeCode);

        String vendorAddress = MessageFormat.format(addressPattern, vendorDetail.getDefaultAddressLine1(), vendorDetail.getDefaultAddressCity(), vendorDetail.getDefaultAddressStateCode(), vendorDetail.getDefaultAddressCountryCode());
        disbursementPayee.setAddress(vendorAddress);

        return disbursementPayee;
    }
    
    
    @Override
    public String getPayeeTypeDescription(String payeeTypeCode) {
        String payeeTypeDescription = StringUtils.EMPTY;

        if (KFSConstants.PaymentPayeeTypes.EMPLOYEE.equals(payeeTypeCode) || 
        CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode) ||
        CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class, CuDisbursementVoucherConstants.NON_VENDOR_EMPLOYEE_PAYEE_TYPE_LABEL_PARM_NM);
        }
        else if (KFSConstants.PaymentPayeeTypes.VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class, CuDisbursementVoucherConstants.PO_AND_DV_PAYEE_TYPE_LABEL_PARM_NM);
        }
        else if (KFSConstants.PaymentPayeeTypes.REVOLVING_FUND_VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = this.getVendorTypeDescription(VendorConstants.VendorTypes.REVOLVING_FUND);
        }
        else if (KFSConstants.PaymentPayeeTypes.SUBJECT_PAYMENT_VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = this.getVendorTypeDescription(VendorConstants.VendorTypes.SUBJECT_PAYMENT);
        }
        else if (KFSConstants.PaymentPayeeTypes.CUSTOMER.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.PAYEE_TYPE_NAME);
        }

        return payeeTypeDescription;
    }
    
    
    public DisbursementPayee getPayeeFromPerson(Person person, String payeeTypeCode) {
        CuDisbursementPayee disbursementPayee = new CuDisbursementPayee();

        disbursementPayee.setActive(person.isActive());
        
        Collection<String> payableEmplStatusCodes = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(CuDisbursementVoucherDocument.class, CuDisbursementVoucherConstants.ALLOWED_EMPLOYEE_STATUSES_FOR_PAYMENT);

        if (StringUtils.equalsIgnoreCase(payeeTypeCode, KFSConstants.PaymentPayeeTypes.EMPLOYEE) && StringUtils.isNotBlank(person.getEmployeeId()) && payableEmplStatusCodes.contains(person.getEmployeeStatusCode())) {
            disbursementPayee.setPayeeIdNumber(person.getEmployeeId());
        } else {
            disbursementPayee.setPayeeIdNumber(person.getPrincipalId());
        }

        disbursementPayee.setPrincipalId(person.getPrincipalId());
        disbursementPayee.setPrincipalName(person.getPrincipalName()); 
        
        disbursementPayee.setPayeeName(person.getNameUnmasked());
        disbursementPayee.setTaxNumber(KFSConstants.BLANK_SPACE);

        disbursementPayee.setPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);

        disbursementPayee.setPayeeTypeCode(payeeTypeCode);
        
        String personAddress = MessageFormat.format(addressPattern, person.getAddressLine1Unmasked(), person.getAddressCityUnmasked(), person.getAddressStateProvinceCodeUnmasked(), person.getAddressCountryCode() == null ? "" : person.getAddressCountryCode());
        disbursementPayee.setAddress(personAddress);

        return (DisbursementPayee) disbursementPayee;
    }

    /**
     * Copied from superclass, and updated to use the initiator's principal name instead of principal ID when preparing the ad hoc FYI to the initiator.
     * 
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#checkPayeeAddressForChanges(org.kuali.kfs.fp.document.DisbursementVoucherDocument)
     */
    @Override
    public void checkPayeeAddressForChanges(DisbursementVoucherDocument dvDoc) {
        Map<String, String> pks = new HashMap<String, String>();
        pks.put("documentNumber", dvDoc.getDocumentNumber());

        DisbursementVoucherDocument savedDv = businessObjectService.findByPrimaryKey(DisbursementVoucherDocument.class, pks);
        DisbursementVoucherPayeeDetail newPayeeDetail = dvDoc.getDvPayeeDetail();
        DisbursementVoucherPayeeDetail oldPayeeDetail = savedDv.getDvPayeeDetail();

        if (ObjectUtils.isNotNull(oldPayeeDetail) && ObjectUtils.isNotNull(newPayeeDetail)) {
            if (!oldPayeeDetail.hasSameAddress(newPayeeDetail)) {// Addresses don't match, so let's start the recording of
                // changes

                // Put a note on the document to record the change to the address
                try {
                    String noteText = buildPayeeChangedNoteText(newPayeeDetail, oldPayeeDetail);

                    int noteMaxSize = dataDictionaryService.getAttributeMaxLength("Note", "noteText");

                    // Break up the note into multiple pieces if the note is too large to fit in the database field.
                    while (noteText.length() > noteMaxSize) {
                        int fromIndex = 0;
                        fromIndex = noteText.lastIndexOf(';', noteMaxSize);

                        String noteText1 = noteText.substring(0, fromIndex);
                        Note note1 = documentService.createNoteFromDocument(dvDoc, noteText1);
                        dvDoc.addNote(note1);
                        noteText = noteText.substring(fromIndex);
                    }

                    Note note = documentService.createNoteFromDocument(dvDoc, noteText);
                    dvDoc.addNote(note);
                }
                catch (Exception e) {
                    LOG.error("Exception while attempting to create or add note: " + e);
                }

                // Send out FYIs to all previous approvers so they're aware of the changes to the address
                try {
                    Set<Person> priorApprovers = dvDoc.getAllPriorApprovers();

                    // ==== Cornell Customization: Retrieve and use the initiator's principal name. ====
                    String initiatorUserId = KimApiServiceLocator.getIdentityService().getPrincipal(
                            dvDoc.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getPrincipalName();
                    setupFYIs(dvDoc, priorApprovers, initiatorUserId);
                }
                catch (WorkflowException we) {
                    LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
                }
                catch (Exception unfe) {
                    LOG.error("Exception while attempting to retrieve all prior approvers for a disbursement voucher: " + unfe);
                }
            }
        }
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#getFieldConversionBetweenPayeeAndPerson()
     */
    @Override
    public Map<String, String> getFieldConversionBetweenPayeeAndPerson() {
        Map<String, String> fieldConversionMap = super.getFieldConversionBetweenPayeeAndPerson();
        fieldConversionMap.put(KFSPropertyConstants.PERSON_USER_IDENTIFIER, KIMPropertyConstants.Person.PRINCIPAL_NAME);
        return fieldConversionMap;
    }
    
    public boolean isStudent(CuDisbursementVoucherPayeeDetail dvPayeeDetail) {
        String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementPayee)
     */
    public boolean isStudent(CuDisbursementPayee payee) {
        String payeeTypeCode = payee.getPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail)
     */
    public boolean isAlumni(CuDisbursementVoucherPayeeDetail dvPayeeDetail) {
        String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementPayee)
     */
    public boolean isAlumni(CuDisbursementPayee payee) {
        String payeeTypeCode = payee.getPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail)
     */
    

}
