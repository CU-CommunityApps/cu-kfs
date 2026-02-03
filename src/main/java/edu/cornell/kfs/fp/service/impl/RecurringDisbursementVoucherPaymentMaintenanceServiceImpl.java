package edu.cornell.kfs.fp.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentChangeCode;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentGroupHistory;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.krad.bo.CodedBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherPaymentMaintenanceService;

public class RecurringDisbursementVoucherPaymentMaintenanceServiceImpl implements RecurringDisbursementVoucherPaymentMaintenanceService {

	private static final Logger LOG = LogManager.getLogger(RecurringDisbursementVoucherPaymentMaintenanceServiceImpl.class);
    
    protected PermissionService permissionService;
    protected BusinessObjectService businessObjectService;
    protected PaymentGroupService paymentGroupService;
    
    @Override
    public boolean hasCancelPermission(Person user) {
        boolean cancelPerm = getPermissionService().hasPermission(user.getPrincipalId(), KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_CANCEL_PAYMENTS_PERMISSION_NAME);
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasCancelPermission():  User " + user.getName() + " has cancel permission: " + cancelPerm);
        }
        return cancelPerm;
    }

    @Override
    public boolean cancelPendingPayment(Integer paymentGroupId, Integer paymentDetailId, String note, Person user) {
        if (LOG.isDebugEnabled()) { 
            LOG.debug("cancelPendingPayment() Enter method to cancel pending payment with group id = " + paymentGroupId + 
                " and payment detail ID of " + paymentDetailId);
        }
        PaymentGroup paymentGroup = this.paymentGroupService.get(paymentGroupId);
        if (ObjectUtils.isNotNull(paymentGroup)) {
            String paymentStatusCode = paymentGroup.getPaymentStatus().getCode();
            checkCancelPermissionAndError(user, paymentStatusCode);
            if (!(PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(paymentStatusCode))) {
                return cancelPaymentDetail(paymentDetailId, note, user, paymentGroup, paymentStatusCode);
            } else {
                LOG.debug("cancelPendingPayment() Pending payment group has already been cancelled; exit method.");
                return true;
            }
        } else {
            LOG.debug("cancelPendingPayment() Pending payment not found.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_NOT_FOUND);
            return false;
        }
    }

    private void checkCancelPermissionAndError(Person user, String paymentStatusCode) {
        if (!hasCancelPermission(user)) {
            LOG.warn("checkCancelPermissionAndError() Payment status is " + paymentStatusCode + "; user does not have rights to cancel. This should not happen.");
            throw new RuntimeException("checkCancelPermissionAndError() Payment status is " + paymentStatusCode + "; user does not have rights to cancel. This should not happen.");
        }
    }

    private boolean cancelPaymentDetail(Integer paymentDetailId, String noteText, Person user, PaymentGroup paymentGroup, String paymentStatus) {
        LOG.debug("cancelPaymentDetail() Payment status is " + paymentStatus + "; continue with cancel.");
        if (PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus) || PdpConstants.PaymentStatusCodes.HELD_CD.equals(paymentStatus)) {
            changePaymentStatusToCancelAndAddUpdatePaymentDetail(paymentDetailId, noteText, user, paymentGroup, paymentStatus);
        } else {
            LOG.error("cancelPaymentDetail(): Recurring DVs should only cancel OPEN payments, we found a status of " + paymentStatus);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_INVALID_STATUS_TO_CANCEL);
            return false;
        }
        return true;
    }
    
    private void changePaymentStatusToCancelAndAddUpdatePaymentDetail(Integer paymentDetailId, String noteText, Person user, PaymentGroup paymentGroup, String paymentStatus) {
        changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT, PdpConstants.PaymentChangeCodes.CANCEL_PAYMENT_CHNG_CD, noteText, user);
        updatePaymentDetail(paymentDetailId, noteText);
        LOG.debug("changePaymentStatusToCancelAndAddUpdatePaymentDetail() Pending payment cancelled; exit method.");
    }
    
    /**}
     * Copied from PaymentMaintenanceServiceImpl
     * @param paymentGroup
     * @param newPaymentStatus
     * @param changeStatus
     * @param note
     * @param user
     */
    protected void changeStatus(PaymentGroup paymentGroup, String newPaymentStatus, String changeStatus, String note, Person user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeStatus() enter method with new status of " + newPaymentStatus);
        }

        PaymentGroupHistory paymentGroupHistory = new PaymentGroupHistory();
        CodedBase cd = businessObjectService.findBySinglePrimaryKey(PaymentChangeCode.class, changeStatus);
        paymentGroupHistory.setPaymentChange((PaymentChangeCode) cd);
        paymentGroupHistory.setOrigPaymentStatus(paymentGroup.getPaymentStatus());
        paymentGroupHistory.setChangeUser(user);
        paymentGroupHistory.setChangeNoteText(note);
        paymentGroupHistory.setPaymentGroup(paymentGroup);
        paymentGroupHistory.setChangeTime(new Timestamp(new Date().getTime()));

        this.businessObjectService.save(paymentGroupHistory);

        CodedBase code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, newPaymentStatus);
        paymentGroup.setPaymentStatus((PaymentStatus) code);
        this.businessObjectService.save(paymentGroup);
        LOG.debug("changeStatus() Status has been changed; exit method.");
    }

    private void updatePaymentDetail(Integer paymentDetailId, String noteText) {
        Map<String, Integer> primaryKeys = new HashMap<String, Integer>();
        primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_ID, paymentDetailId);
        PaymentDetail pd = this.businessObjectService.findByPrimaryKey(PaymentDetail.class, primaryKeys);
        
        if (pd != null) {
            pd.setPrimaryCancelledPayment(Boolean.TRUE);
            PaymentNoteText payNoteText = new PaymentNoteText();
            payNoteText.setCustomerNoteLineNbr(new KualiInteger(pd.getNotes().size() + 1));
            payNoteText.setCustomerNoteText(noteText);
            pd.addNote(payNoteText);
            this.businessObjectService.save(pd);
        } else {
            LOG.error("updatePaymentDetail() Unable to retieve payment detail with an ID of " + paymentDetailId);
        }
        
    }
    
    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public PaymentGroupService getPaymentGroupService() {
        return paymentGroupService;
    }

    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

}