package edu.cornell.kfs.pdp.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentGroupHistory;
import org.kuali.kfs.pdp.service.impl.PaymentMaintenanceServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CuPaymentMaintenanceServiceImpl extends PaymentMaintenanceServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public boolean cancelDisbursement(Integer paymentGroupId, Integer paymentDetailId, String note, Person user) {
        LOG.debug("cancelDisbursement() started");

        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasCancelPaymentPermission(user.getPrincipalId())) {
            LOG.warn("cancelDisbursement() User " + user.getPrincipalId() + " does not have rights to cancel " +
                    "payments. This should not happen unless user is URL spoofing.");
            throw new RuntimeException("cancelDisbursement() User " + user.getPrincipalId() +
                    " does not have rights to cancel payments. This should not happen unless user is URL spoofing.");
        }

        PaymentGroup paymentGroup = this.paymentGroupService.get(paymentGroupId);

        if (paymentGroup == null) {
            LOG.debug("cancelDisbursement() Disbursement not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_NOT_FOUND);
            return false;
        }
        //get the target PaymentGroup info
        PaymentDetail targetPd = getPaymentDetail(paymentDetailId);
        KualiInteger targetGroupId = targetPd.getPaymentGroupId();
        PaymentGroup targetPg = getPaymentGroup(targetGroupId);
        String targetDvTypeCode = targetPg.getDisbursementTypeCode();
        String targetDvBankCode = targetPg.getBankCode();
        
        String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.EXTRACTED.equals(paymentStatus)
                && ObjectUtils.isNotNull(paymentGroup.getDisbursementDate())
                || PdpConstants.PaymentStatusCodes.PENDING_ACH.equals(paymentStatus)) {
                LOG.debug("cancelDisbursement() Payment status is " + paymentStatus + "; continue with cancel.");

                List<PaymentGroup> allDisbursementPaymentGroups = this.paymentGroupService.getByDisbursementNumber(
                        paymentGroup.getDisbursementNbr().intValue());

                for (PaymentGroup element : allDisbursementPaymentGroups) {
                    // should be the same DV type and the same bank
                    if (!(element.getDisbursementTypeCode().equalsIgnoreCase(targetDvTypeCode) && element.getBankCode().equalsIgnoreCase(targetDvBankCode))) {
                        continue;
                    }
                    
                    PaymentGroupHistory pgh = new PaymentGroupHistory();

                    if (!element.getPaymentDetails().get(0).isDisbursementActionAllowed()) {
                        LOG.warn("cancelDisbursement() Payment does not allow disbursement action. This should " +
                                "not happen unless user is URL spoofing.");
                        throw new RuntimeException("cancelDisbursement() Payment does not allow disbursement action. " +
                                "This should not happen unless user is URL spoofing.");
                    }

                    if (ObjectUtils.isNotNull(element.getDisbursementType())
                            && element.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.CHECK)) {
                        pgh.setPmtCancelExtractStat(Boolean.FALSE);
                    }

                    changeStatus(element, PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT,
                            PdpConstants.PaymentChangeCodes.CANCEL_DISBURSEMENT, note, user, pgh);

                    glPendingTransactionService.generateCancellationGeneralLedgerPendingEntry(element);

                    // set primary cancel indicator for EPIC to use
                    // these payment details will be canceled when running processPdpCancelAndPaidJOb
                    Map<String, KualiInteger> primaryKeys = new HashMap<>();

                    primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_DETAIL_PAYMENT_GROUP_ID, element.getId());

                    // cancel all  payment details for payment group
                    List<PaymentDetail> pds = (List<PaymentDetail>) this.businessObjectService.findMatching(PaymentDetail.class, primaryKeys);
                    if (pds != null && !pds.isEmpty()) {
                        for (PaymentDetail pd : pds) {
                            pd.setPrimaryCancelledPayment(Boolean.TRUE);
                            this.businessObjectService.save(pd);
                        }
                    }
                }

                LOG.debug("cancelDisbursement() Disbursement cancelled; exit method.");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("cancelDisbursement() Payment status is " + paymentStatus +
                            " and disbursement date is " + paymentGroup.getDisbursementDate() +
                            "; cannot cancel payment in this status");
                }

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL);
                return false;
            }
        } else {
            LOG.debug("cancelDisbursement() Disbursement has already been cancelled; exit method.");
        }
        return true;
    }

    @Override
    public boolean cancelReissueDisbursement(Integer paymentGroupId, String note, Person user) {
        LOG.debug("cancelReissueDisbursement() started");

        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasCancelPaymentPermission(user.getPrincipalId())) {
            LOG.warn("cancelReissueDisbursement() User " + user.getPrincipalId() + " does not have rights to " +
                    " cancel payments. This should not happen unless user is URL spoofing.");
            throw new RuntimeException("cancelReissueDisbursement() User " + user.getPrincipalId() + " does not " +
                    "have rights to cancel payments. This should not happen unless user is URL spoofing.");
        }

        PaymentGroup paymentGroup = this.paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("cancelReissueDisbursement() Disbursement not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_NOT_FOUND);
            return false;
        }

        String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.EXTRACTED.equals(paymentStatus)
                    && ObjectUtils.isNotNull(paymentGroup.getDisbursementDate())
                    || PdpConstants.PaymentStatusCodes.PENDING_ACH.equals(paymentStatus)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("cancelReissueDisbursement() Payment status is " + paymentStatus +
                            "; continue with cancel.");
                }

                List<PaymentGroup> allDisbursementPaymentGroups = this.paymentGroupService.getByDisbursementNumber(
                        paymentGroup.getDisbursementNbr().intValue());

                for (PaymentGroup pg : allDisbursementPaymentGroups) {
                    PaymentGroupHistory pgh = new PaymentGroupHistory();

                    if (!pg.getPaymentDetails().get(0).isDisbursementActionAllowed()) {
                        LOG.warn("cancelDisbursement() Payment does not allow disbursement action. This should " +
                                "not happen unless user is URL spoofing.");
                        throw new RuntimeException("cancelDisbursement() Payment does not allow disbursement " +
                                "action. This should not happen unless user is URL spoofing.");
                    }

                    if (ObjectUtils.isNotNull(pg.getDisbursementType())
                            && pg.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.CHECK)) {
                        pgh.setPmtCancelExtractStat(Boolean.FALSE);
                    }

                    pgh.setOrigProcessImmediate(pg.getProcessImmediate());
                    pgh.setOrigPmtSpecHandling(pg.getPymtSpecialHandling());
                    pgh.setBank(pg.getBank());
                    pgh.setOrigPaymentDate(pg.getPaymentDate());
                    //put a check for null since disbursement date was not set in testMode / dev
                    if (ObjectUtils.isNotNull(pg.getDisbursementDate())) {
                        pgh.setOrigDisburseDate(new Timestamp(pg.getDisbursementDate().getTime()));
                    }
                    pgh.setOrigAchBankRouteNbr(pg.getAchBankRoutingNbr());
                    pgh.setOrigDisburseNbr(pg.getDisbursementNbr());
                    pgh.setOrigAdviceEmail(pg.getAdviceEmailAddress());
                    pgh.setDisbursementType(pg.getDisbursementType());
                    pgh.setProcess(pg.getProcess());

                    glPendingTransactionService.generateCancelReissueGeneralLedgerPendingEntry(pg);

                    LOG.debug("cancelReissueDisbursement() Status is '" + paymentStatus +
                            "; delete row from AchAccountNumber table.");

                    AchAccountNumber achAccountNumber = pg.getAchAccountNumber();

                    if (ObjectUtils.isNotNull(achAccountNumber)) {
                        this.businessObjectService.delete(achAccountNumber);
                        pg.setAchAccountNumber(null);
                    }

                    // if bank functionality is not enabled or the group bank is inactive clear bank code
                    if (!bankService.isBankSpecificationEnabled() || !pg.getBank().isActive()) {
                        pg.setBank(null);
                    }

                    pg.setDisbursementDate((java.sql.Date) null);
                    pg.setAchBankRoutingNbr(null);
                    pg.setAchAccountType(null);
                    pg.setPhysCampusProcessCd(null);
                    pg.setDisbursementNbr((KualiInteger) null);
                    pg.setAdviceEmailAddress(null);
                    // KFSPTS-1413 - do not reset the disb type as it prevents these payments from being picked up properly on reissue.
                    //pg.setDisbursementType(null);
                    pg.setProcess(null);
                    pg.setProcessImmediate(false);

                    changeStatus(pg, PdpConstants.PaymentStatusCodes.OPEN,
                            PdpConstants.PaymentChangeCodes.CANCEL_REISSUE_DISBURSEMENT, note, user, pgh);
                }

                LOG.debug("cancelReissueDisbursement() Disbursement cancelled and reissued; exit method.");
            } else {
                LOG.debug("cancelReissueDisbursement() Payment status is " + paymentStatus +
                        " and disbursement date is " + paymentGroup.getDisbursementDate() + "; cannot cancel payment");

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL_AND_REISSUE);
                return false;
            }
        } else {
            LOG.debug("cancelReissueDisbursement() Disbursement already cancelled and reissued; exit method.");
        }
        return true;
    }
    
    protected PaymentDetail getPaymentDetail(Integer paymentDetailId) {
        Map<String, Integer> primaryKeys = new HashMap<String, Integer>();
        primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_ID, paymentDetailId);
        
        return (PaymentDetail) this.businessObjectService.findByPrimaryKey(PaymentDetail.class, primaryKeys);
    }

    protected PaymentGroup getPaymentGroup(KualiInteger paymentGroupId) {
        Map<String, KualiInteger> primaryKeys = new HashMap<String, KualiInteger>();
        primaryKeys.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_ID, paymentGroupId);
        
        return (PaymentGroup) this.businessObjectService.findByPrimaryKey(PaymentGroup.class, primaryKeys);
    }
}
