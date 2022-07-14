package edu.cornell.kfs.pdp.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.DisbursementType;
import org.kuali.kfs.pdp.businessobject.FormatProcess;
import org.kuali.kfs.pdp.businessobject.FormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.pdp.service.impl.FormatServiceImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.api.identity.Person;

import edu.cornell.kfs.pdp.dataaccess.CuFormatPaymentDao;
import edu.cornell.kfs.pdp.service.CuFormatService;

public class CuFormatServiceImpl extends FormatServiceImpl implements CuFormatService {

    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public FormatProcessSummary startFormatProcess(Person user, String campus,
			List<CustomerProfile> customers, Date paydate, String paymentTypes,
			String paymentDistribution) {
        LOG.debug("startFormatProcess() started");

        for (CustomerProfile element : customers) {
            LOG.debug("startFormatProcess() Customer: " + element);
        }

        // Create the process
        Date d = new Date();
        PaymentProcess paymentProcess = new PaymentProcess();
        paymentProcess.setCampusCode(campus);
        paymentProcess.setProcessUser(user);
        paymentProcess.setProcessTimestamp(new Timestamp(d.getTime()));

        this.businessObjectService.save(paymentProcess);

        // add an entry in the format process table (to lock the format process)
        FormatProcess formatProcess = new FormatProcess();

        formatProcess.setPhysicalCampusProcessCode(campus);
        formatProcess.setBeginFormat(dateTimeService.getCurrentTimestamp());
        formatProcess.setPaymentProcIdentifier(paymentProcess.getId().intValue());

        this.businessObjectService.save(formatProcess);


        Timestamp now = new Timestamp((new Date()).getTime());
        java.sql.Date sqlDate = new java.sql.Date(paydate.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(sqlDate);
        c.set(Calendar.HOUR, 11);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 59);
        c.set(Calendar.AM_PM, Calendar.PM);
        Timestamp paydateTs = new Timestamp(c.getTime().getTime());

        LOG.debug("startFormatProcess() last update = " + now);
        LOG.debug("startFormatProcess() entered paydate = " + paydate);
        LOG.debug("startFormatProcess() actual paydate = " + paydateTs);

        PaymentStatus format = this.businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.FORMAT);

        List<KualiInteger> customerIds = new ArrayList<>();
        for (CustomerProfile element : customers) {
            customerIds.add(element.getId());
        }

        // Mark all of them ready for format
        Iterator groupIterator = ((CuFormatPaymentDao) formatPaymentDao).markPaymentsForFormat(customerIds, paydateTs, paymentTypes, paymentDistribution);

        while (groupIterator.hasNext()) {
            PaymentGroup paymentGroup = (PaymentGroup) groupIterator.next();
            paymentGroup.setLastUpdatedTimestamp(paydateTs);
            paymentGroup.setPaymentStatus(format);
            paymentGroup.setProcess(paymentProcess);
            businessObjectService.save(paymentGroup);
        }


        // summarize them
        FormatProcessSummary preFormatProcessSummary = new FormatProcessSummary();
        Iterator<PaymentGroup> iterator = this.paymentGroupService.getByProcess(paymentProcess);

        while (iterator.hasNext()) {
            PaymentGroup paymentGroup = iterator.next();
            preFormatProcessSummary.add(paymentGroup);
        }

        // if no payments found for format clear the format process
        if (preFormatProcessSummary.getProcessSummaryList().size() == 0) {
            LOG.debug("startFormatProcess() No payments to process.  Format process ending");
            clearUnfinishedFormat(paymentProcess.getId().intValue());
        }

        return preFormatProcessSummary;
    }

    @Override
    protected boolean processPaymentGroup(
            final PaymentGroup paymentGroup,
            final PaymentProcess paymentProcess,
            final boolean shouldUseIso20022Format
    ) {
        paymentGroup.setSortValue(paymentGroupService.getSortGroupId(paymentGroup));
        paymentGroup.setPhysCampusProcessCd(paymentProcess.getCampusCode());
        paymentGroup.setProcess(paymentProcess);

        populateDisbursementType(paymentGroup);
        
        if (StringUtils.equalsIgnoreCase(paymentGroup.getDisbursementTypeCode(), PdpConstants.DisbursementTypeCodes.CHECK)) {
            PaymentStatus paymentStatus = (PaymentStatus) businessObjectService
                    .findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.PENDING_CHECK);
            paymentGroup.setPaymentStatus(paymentStatus);
        } else {
            PaymentStatus paymentStatus = (PaymentStatus) businessObjectService
                    .findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.PENDING_ACH);
            paymentGroup.setPaymentStatus(paymentStatus);
        }
        
        return validateAndUpdatePaymentGroupBankCode(paymentGroup, paymentGroup.getDisbursementType(), paymentGroup.getBatch().getCustomerProfile(), shouldUseIso20022Format);
    }
    
    /**
     * This method sets the appropriate disbursement type on the Payment Group to either ACH or CHCK.
     * 
     * @param paymentGroup
     */
    protected void populateDisbursementType(PaymentGroup paymentGroup) {
        DisbursementType disbursementType;
        if (paymentGroup.isPayableByCheck()) {
            disbursementType = (DisbursementType) businessObjectService
                    .findBySinglePrimaryKey(DisbursementType.class, PdpConstants.DisbursementTypeCodes.CHECK);
            paymentGroup.setDisbursementType(disbursementType);
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        } else {
            disbursementType = (DisbursementType) businessObjectService.findBySinglePrimaryKey(DisbursementType.class, PdpConstants.DisbursementTypeCodes.ACH);
            paymentGroup.setDisbursementType(disbursementType);
            paymentGroup.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);

            CustomerProfile customer = paymentGroup.getBatch().getCustomerProfile();
            PayeeACHAccount payeeAchAccount = SpringContext.getBean(AchService.class)
                    .getAchInformation(paymentGroup.getPayeeIdTypeCd(), paymentGroup.getPayeeId(), customer.getAchTransactionType());
            
            paymentGroup.setAchBankRoutingNbr(payeeAchAccount.getBankRoutingNumber());
            paymentGroup.setAdviceEmailAddress(payeeAchAccount.getPayeeEmailAddress());
            paymentGroup.setAchAccountType(payeeAchAccount.getBankAccountTypeCode());

            AchAccountNumber achAccountNumber = new AchAccountNumber();
            achAccountNumber.setAchBankAccountNbr(payeeAchAccount.getBankAccountNumber());
            achAccountNumber.setId(paymentGroup.getId());
            paymentGroup.setAchAccountNumber(achAccountNumber);
        }
    }

}
