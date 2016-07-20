package edu.cornell.kfs.fp.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.service.impl.PaymentMaintenanceServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherPaymentMaintenanceService;

public class RecurringDisbursementVoucherPaymentMaintenanceServiceImpl extends PaymentMaintenanceServiceImpl implements RecurringDisbursementVoucherPaymentMaintenanceService {

    private static final long serialVersionUID = -1061956872976755617L;
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecurringDisbursementVoucherPaymentMaintenanceServiceImpl.class);
    
    protected PermissionService permissionService;

    @Override
    public boolean cancelPendingPayment(Integer paymentGroupId, Integer paymentDetailId, String note, Person user) {
        LOG.debug("cancelPendingPayment() Enter method to cancel pending payment with group id = " + paymentGroupId + 
                " and payment detail ID of " + paymentDetailId);

        PaymentGroup paymentGroup = this.paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("cancelPendingPayment() Pending payment not found.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_NOT_FOUND);
            return false;
        }

        String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!(PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(paymentStatus))) {
            return processPaymentToBeCanceled(paymentDetailId, note, user, paymentGroup, paymentStatus);
        } else {
            LOG.debug("cancelPendingPayment() Pending payment group has already been cancelled; exit method.");
            return true;
        }
    }

    private boolean processPaymentToBeCanceled(Integer paymentDetailId, String note, Person user, PaymentGroup paymentGroup, String paymentStatus) {
        LOG.debug("cancelPendingPayment() Payment status is " + paymentStatus + "; continue with cancel.");

        if (PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus) || PdpConstants.PaymentStatusCodes.HELD_CD.equals(paymentStatus)) {
            proccessOpenPaymentCancellation(paymentDetailId, note, user, paymentGroup, paymentStatus);
        } else {
            LOG.error("cancelPendingPayment(): Recurring DVs should only cancel OPEN payments, we found a status of " + paymentStatus);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_INVALID_STATUS_TO_CANCEL);
            return false;
        }
        return true;
    }
    
    private void proccessOpenPaymentCancellation(Integer paymentDetailId, String note, Person user, PaymentGroup paymentGroup, String paymentStatus) {
        if (!hasCancelPermission(user)) {
            LOG.warn("proccessOpenPaymentCancellation() Payment status is " + paymentStatus + "; user does not have rights to cancel. This should not happen.");
            throw new RuntimeException("proccessOpenPaymentCancellation() Payment status is " + paymentStatus + "; user does not have rights to cancel. This should not happen.");
        }
        
        changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT, PdpConstants.PaymentChangeCodes.CANCEL_PAYMENT_CHNG_CD, note, user);

        Map primaryKeys = new HashMap();
        primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_ID, paymentDetailId);
        PaymentDetail pd = this.businessObjectService.findByPrimaryKey(PaymentDetail.class, primaryKeys);
        
        if (pd != null) {
            pd.setPrimaryCancelledPayment(Boolean.TRUE);
            PaymentNoteText payNoteText = new PaymentNoteText();
            payNoteText.setCustomerNoteLineNbr(new KualiInteger(pd.getNotes().size() + 1));
            payNoteText.setCustomerNoteText(note);
            pd.addNote(payNoteText);
        }
        this.businessObjectService.save(pd);

        LOG.debug("proccessOpenPaymentCancellation() Pending payment cancelled; exit method.");
    }
    
    @Override
    public boolean hasCancelPermission(Person user) {
        boolean cancelPerm = getPermissionService().hasPermission(user.getPrincipalId(), KFSConstants.ParameterNamespaces.FINANCIAL, 
                CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_CANCEL_PARMENTS_PERMISSION_NAME);
        LOG.info("hasCancelPermission():  User " + user.getName() + " has cancel permission: " + cancelPerm);
        return cancelPerm;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

}
