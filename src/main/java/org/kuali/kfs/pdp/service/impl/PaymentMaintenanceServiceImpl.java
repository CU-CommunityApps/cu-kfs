/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Aug 12, 2004
 */
package org.kuali.kfs.pdp.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.KualiCode;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.PaymentChangeCode;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentGroupHistory;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.dataaccess.PaymentDetailDao;
import org.kuali.kfs.pdp.dataaccess.PaymentGroupDao;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PaymentMaintenanceService;
import org.kuali.kfs.pdp.service.PdpAuthorizationService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Cornell Customization to make private variable protected
 *
 */
@Transactional
public class PaymentMaintenanceServiceImpl implements PaymentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger();

    protected PaymentGroupDao paymentGroupDao;
    protected PaymentDetailDao paymentDetailDao;
    protected PendingTransactionService glPendingTransactionService;
    protected ParameterService parameterService;
    protected BankService bankService;
    protected BusinessObjectService businessObjectService;
    protected PaymentGroupService paymentGroupService;
    protected PdpEmailService pdpEmailService;
    protected PdpAuthorizationService pdpAuthorizationService;
    // CU customization to change access from private to protected
    protected GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
    private PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService;

    /**
     * This method changes status for a payment group.
     *
     * @param paymentGroup     the payment group
     * @param newPaymentStatus the new payment status
     * @param changeStatus     the changed payment status
     * @param note             a note for payment status change
     * @param user             the user that changed the status
     */
    protected void changeStatus(
            final PaymentGroup paymentGroup, final String newPaymentStatus, final String changeStatus, final String note,
            final Person user) {
        LOG.debug("changeStatus() started with new status of {}", newPaymentStatus);

        final PaymentGroupHistory paymentGroupHistory = new PaymentGroupHistory();
        final KualiCode cd = businessObjectService.findBySinglePrimaryKey(PaymentChangeCode.class, changeStatus);
        paymentGroupHistory.setPaymentChange((PaymentChangeCode) cd);
        paymentGroupHistory.setOrigPaymentStatus(paymentGroup.getPaymentStatus());
        paymentGroupHistory.setChangeUser(user);
        paymentGroupHistory.setChangeNoteText(note);
        paymentGroupHistory.setPaymentGroup(paymentGroup);
        paymentGroupHistory.setChangeTime(new Timestamp(new Date().getTime()));

        businessObjectService.save(paymentGroupHistory);

        final KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, newPaymentStatus);
        paymentGroup.setPaymentStatus((PaymentStatus) code);
        businessObjectService.save(paymentGroup);
        LOG.debug("changeStatus() Status has been changed; exit method.");
    }

    /**
     * This method changes the state of a paymentGroup.
     *
     * @param paymentGroup        the payment group to change the state for
     * @param newPaymentStatus    the new payment status
     * @param changeStatus        the status that is changed
     * @param note                the note entered by the user
     * @param user                the user that changed the
     * @param paymentGroupHistory
     */
    protected void changeStatus(
            final PaymentGroup paymentGroup, final String newPaymentStatus, final String changeStatus, final String note,
            final Person user, final PaymentGroupHistory paymentGroupHistory) {
        LOG.debug("changeStatus() started with new status of {}", newPaymentStatus);

        final KualiCode cd = businessObjectService.findBySinglePrimaryKey(PaymentChangeCode.class, changeStatus);
        paymentGroupHistory.setPaymentChange((PaymentChangeCode) cd);
        paymentGroupHistory.setOrigPaymentStatus(paymentGroup.getPaymentStatus());
        paymentGroupHistory.setChangeUser(user);
        paymentGroupHistory.setChangeNoteText(note);
        paymentGroupHistory.setPaymentGroup(paymentGroup);
        paymentGroupHistory.setChangeTime(new Timestamp(new Date().getTime()));

        businessObjectService.save(paymentGroupHistory);

        final KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, newPaymentStatus);
        if (paymentGroup.getPaymentStatus() != code) {
            paymentGroup.setPaymentStatus((PaymentStatus) code);
        }
        businessObjectService.save(paymentGroup);

        LOG.debug("changeStatus() Status has been changed; exit method.");
    }

    @Override
    public boolean cancelPendingPayment(final Integer paymentGroupId, final Integer paymentDetailId, final String note, final Person user) {
        LOG.debug("cancelPendingPayment() started");

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("cancelPendingPayment() Pending payment not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_NOT_FOUND);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(paymentStatus)) {
            LOG.debug("cancelPendingPayment() Payment status is {}; continue with cancel.", paymentStatus);

            if (PdpConstants.PaymentStatusCodes.HELD_TAX_EMPLOYEE_CD.equals(paymentStatus)
                || PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_CD.equals(paymentStatus)
                || PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_EMPL_CD.equals(paymentStatus)) {
                if (!pdpAuthorizationService.hasRemovePaymentTaxHoldPermission(user.getPrincipalId())) {
                    LOG.warn(
                            "cancelPendingPayment() Payment status is {}; user does not have rights to cancel. This "
                            + "should not happen unless user is URL spoofing.",
                            paymentStatus
                    );
                    throw new RuntimeException("cancelPendingPayment() Payment status is " + paymentStatus +
                            "; user does not have rights to cancel. This should not happen unless user is URL spoofing.");
                }

                changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT,
                        PdpConstants.PaymentChangeCodes.CANCEL_PAYMENT_CHNG_CD, note, user);

                // set primary cancel indicator for EPIC to use
                final Map<String, Integer> primaryKeys = new HashMap<>();
                primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_ID, paymentDetailId);

                final PaymentDetail pd = businessObjectService.findByPrimaryKey(PaymentDetail.class, primaryKeys);
                if (pd != null) {
                    pd.setPrimaryCancelledPayment(Boolean.TRUE);
                }
                businessObjectService.save(pd);
                pdpEmailService.sendCancelEmail(paymentGroup, note, user);

                LOG.debug("cancelPendingPayment() Pending payment cancelled and mail was sent; exit method.");
            } else if (PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)
                    || PdpConstants.PaymentStatusCodes.HELD_CD.equals(paymentStatus)) {
                if (!pdpAuthorizationService.hasCancelPaymentPermission(user.getPrincipalId())) {
                    LOG.warn(
                            "cancelPendingPayment() Payment status is {}; user does not have rights to cancel. This "
                            + "should not happen unless user is URL spoofing.",
                            paymentStatus
                    );
                    throw new RuntimeException("cancelPendingPayment() Payment status is " + paymentStatus +
                            "; user does not have rights to cancel. This should not happen unless user is URL spoofing.");
                }

                changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT,
                        PdpConstants.PaymentChangeCodes.CANCEL_PAYMENT_CHNG_CD, note, user);

                // set primary cancel indicator for EPIC to use
                final Map<String, Integer> primaryKeys = new HashMap<>();
                primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_ID, paymentDetailId);

                final PaymentDetail pd = businessObjectService.findByPrimaryKey(PaymentDetail.class, primaryKeys);
                if (pd != null) {
                    pd.setPrimaryCancelledPayment(Boolean.TRUE);
                    final PaymentNoteText payNoteText = new PaymentNoteText();
                    payNoteText.setCustomerNoteLineNbr(new KualiInteger(pd.getNotes().size() + 1));
                    payNoteText.setCustomerNoteText(note);
                    pd.addNote(payNoteText);
                }

                businessObjectService.save(pd);

                LOG.debug("cancelPendingPayment() Pending payment cancelled; exit method.");
            } else {
                LOG.debug(
                        "cancelPendingPayment() Payment status is {}; cannot cancel payment in this status",
                        paymentStatus
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_INVALID_STATUS_TO_CANCEL);
                return false;
            }
        } else {
            LOG.debug("cancelPendingPayment() Pending payment group has already been cancelled; exit method.");
        }
        return true;
    }

    @Override
    public boolean holdPendingPayment(final Integer paymentGroupId, final String note, final Person user) {
        LOG.debug("holdPendingPayment() started");
        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasHoldPaymentPermission(user.getPrincipalId())) {
            LOG.warn(
                    "holdPendingPayment() User {} does not have rights to hold payments. This should not happen unless user is URL spoofing.",
                    user::getPrincipalId
            );
            throw new RuntimeException("holdPendingPayment() User " + user.getPrincipalId() +
                    " does not have rights to hold payments. This should not happen unless user is URL spoofing.");
        }

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("holdPendingPayment() Pending payment not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_NOT_FOUND);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.HELD_CD.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)) {
                LOG.debug("holdPendingPayment() Payment status is {}; continue with hold.", paymentStatus);

                changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.HELD_CD,
                        PdpConstants.PaymentChangeCodes.HOLD_CHNG_CD, note, user);

                LOG.debug("holdPendingPayment() Pending payment was put on hold; exit method.");
            } else {
                LOG.debug("holdPendingPayment() Payment status is {}; cannot hold payment in this status",
                        paymentStatus
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_INVALID_STATUS_TO_HOLD);
                return false;
            }
        } else {
            LOG.debug("holdPendingPayment() Pending payment group has already been held; exit method.");
        }
        return true;

    }

    @Override
    public boolean removeHoldPendingPayment(final Integer paymentGroupId, final String note, final Person user) {
        LOG.debug("removeHoldPendingPayment() started");

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("removeHoldPendingPayment() Payment not found; throw exception.");

            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_NOT_FOUND);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)) {
            LOG.debug("removeHoldPendingPayment() Payment status is {}; continue with hold removal.", paymentStatus);

            if (PdpConstants.PaymentStatusCodes.HELD_TAX_EMPLOYEE_CD.equals(paymentStatus)
                || PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_CD.equals(paymentStatus)
                || PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_EMPL_CD.equals(paymentStatus)) {
                if (!pdpAuthorizationService.hasRemovePaymentTaxHoldPermission(user.getPrincipalId())) {
                    LOG.warn(
                            "removeHoldPendingPayment() User {} does not have rights to remove tax holds. This should not happen unless user is URL spoofing.",
                            user::getPrincipalId
                    );
                    throw new RuntimeException("removeHoldPendingPayment() User " + user.getPrincipalId() +
                            " does not have rights to remove tax holds. This should not happen unless user is URL spoofing.");
                }

                changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.OPEN,
                        PdpConstants.PaymentChangeCodes.REMOVE_HOLD_CHNG_CD, note, user);
                LOG.debug("removeHoldPendingPayment() Pending payment was taken off hold; exit method.");
            } else if (PdpConstants.PaymentStatusCodes.HELD_CD.equals(paymentStatus)) {
                if (!pdpAuthorizationService.hasHoldPaymentPermission(user.getPrincipalId())) {
                    LOG.warn(
                            "removeHoldPendingPayment() User {} does not have rights to hold payments. This should not happen unless user is URL spoofing.",
                            user::getPrincipalId
                    );
                    throw new RuntimeException("removeHoldPendingPayment() User " + user.getPrincipalId() +
                            " does not have rights to hold payments. This should not happen unless user is URL spoofing.");
                }

                changeStatus(paymentGroup, PdpConstants.PaymentStatusCodes.OPEN,
                        PdpConstants.PaymentChangeCodes.REMOVE_HOLD_CHNG_CD, note, user);

                LOG.debug("removeHoldPendingPayment() Pending payment was taken off hold; exit method.");
            } else {
                LOG.debug(
                        "removeHoldPendingPayment() Payment status is {}; cannot remove hold on payment in this status",
                        paymentStatus
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_INVALID_STATUS_TO_REMOVE_HOLD);
                return false;
            }
        } else {
            LOG.debug("removeHoldPendingPayment() Pending payment group has already been un-held; exit method.");
        }
        return true;
    }

    @Override
    public void changeImmediateFlag(final Integer paymentGroupId, final String note, final Person user) {
        LOG.debug("changeImmediateFlag() started");

        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasSetAsImmediatePayPermission(user.getPrincipalId())) {
            LOG.warn(
                    "changeImmediateFlag() User {} does not have rights to set payments as immediate. This should not happen unless user is URL spoofing.",
                    user::getPrincipalId
            );
            throw new RuntimeException("changeImmediateFlag() User " + user.getPrincipalId() +
                    " does not have rights to payments as immediate. This should not happen unless user is URL spoofing.");
        }

        final PaymentGroupHistory paymentGroupHistory = new PaymentGroupHistory();
        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);

        paymentGroupHistory.setOrigProcessImmediate(paymentGroup.getProcessImmediate());

        if (paymentGroup.getProcessImmediate().equals(Boolean.TRUE)) {
            paymentGroup.setProcessImmediate(Boolean.FALSE);
        } else {
            paymentGroup.setProcessImmediate(Boolean.TRUE);
        }

        changeStatus(paymentGroup, paymentGroup.getPaymentStatus().getCode(),
                PdpConstants.PaymentChangeCodes.CHANGE_IMMEDIATE_CHNG_CD, note, user, paymentGroupHistory);
    }

    @Override
    public boolean cancelDisbursement(final Integer paymentGroupId, final Integer paymentDetailId, final String note, final Person user) {
        LOG.debug("cancelDisbursement() started");

        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasCancelPaymentPermission(user.getPrincipalId())) {
            LOG.warn(
                    "cancelDisbursement() User {} does not have rights to cancel payments. This should not happen unless user is URL spoofing.",
                    user::getPrincipalId
            );
            throw new RuntimeException("cancelDisbursement() User " + user.getPrincipalId() +
                    " does not have rights to cancel payments. This should not happen unless user is URL spoofing.");
        }

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);

        if (paymentGroup == null) {
            LOG.debug("cancelDisbursement() Disbursement not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_NOT_FOUND);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.EXTRACTED.equals(paymentStatus)
                && ObjectUtils.isNotNull(paymentGroup.getDisbursementDate())
                || PdpConstants.PaymentStatusCodes.PENDING_ACH.equals(paymentStatus)) {
                LOG.debug("cancelDisbursement() Payment status is {}; continue with cancel.", paymentStatus);

                final List<PaymentGroup> allDisbursementPaymentGroups = paymentGroupService.getByDisbursementNumber(
                        paymentGroup.getDisbursementNbr().intValue());

                for (final PaymentGroup element : allDisbursementPaymentGroups) {
                    final PaymentGroupHistory pgh = new PaymentGroupHistory();

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

                    final boolean wasHandled = deleteGlpesForCancelDisbursement(element);
                    if (!wasHandled) {
                        glPendingTransactionService.generateCancellationGeneralLedgerPendingEntry(element);
                    }

                    // set primary cancel indicator for EPIC to use
                    // these payment details will be canceled when running processPdpCancelAndPaidJOb
                    final Map<String, KualiInteger> primaryKeys = new HashMap<>();

                    primaryKeys.put(PdpPropertyConstants.PaymentDetail.PAYMENT_DETAIL_PAYMENT_GROUP_ID, element.getId());

                    // cancel all  payment details for payment group
                    final List<PaymentDetail> pds = (List<PaymentDetail>) businessObjectService.findMatching(PaymentDetail.class, primaryKeys);
                    if (pds != null && !pds.isEmpty()) {
                        for (final PaymentDetail pd : pds) {
                            pd.setPrimaryCancelledPayment(Boolean.TRUE);
                            businessObjectService.save(pd);
                        }
                    }
                }

                LOG.debug("cancelDisbursement() Disbursement cancelled; exit method.");
            } else {
                LOG.debug(
                        "cancelDisbursement() Payment status is {} and disbursement date is {}; cannot cancel payment in this status",
                        () -> paymentStatus,
                        paymentGroup::getDisbursementDate
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL);
                return false;
            }
        } else {
            LOG.debug("cancelDisbursement() Disbursement has already been cancelled; exit method.");
        }
        return true;
    }

    private static boolean isCheckAchDisbursement(final PaymentGroup paymentGroup) {
        return PdpConstants.DisbursementTypeCodes.CHECK.equals(paymentGroup.getDisbursementTypeCode())
                || PdpConstants.DisbursementTypeCodes.ACH.equals(paymentGroup.getDisbursementTypeCode());
    }

    /*
     * If the payment group is not a check or ACH disbursement, then it could be that the disbursement is being
     * cancelled before the disbursement GLPEs have been processed.  In that case, we just want to delete the GLPEs, we
     * don't need to create cancel GLPEs.
     * @return true if the GLPEs were deleted and there is no need to create further GLPEs
     */
    private boolean deleteGlpesForCancelDisbursement(final PaymentGroup paymentGroup) {
        if (isCheckAchDisbursement(paymentGroup)) {
            return false;
        }
        LOG.debug(
                "deleteGlpesForCancelDisbursement(...) - Check for GLPEs to delete: paymentGroupId={}",
                paymentGroup::getId
        );
        boolean withGlpes = false;
        boolean withoutGlpes = false;
        for (final PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            final String documentId = paymentDetail.getCustPaymentDocNbr();
            final Map<String, Object> fieldValues = Map.of(KFSPropertyConstants.DOCUMENT_NUMBER, documentId);
            final Collection<?> glpes = generalLedgerPendingEntryService.findPendingEntries(fieldValues, false);
            if (glpes.isEmpty()) {
                LOG.debug(
                        "deleteGlpesForCancelDisbursement(...) -  No GPLEs found; documentId={}, paymentDetailId={}",
                        paymentDetail::getCustPaymentDocNbr,
                        paymentDetail::getId
                );
                withoutGlpes = true;
            } else {
                LOG.debug(
                        "deleteGlpesForCancelDisbursement(...) - Delete GLPEs; documentId={}, paymentDetailId={}",
                        paymentDetail::getCustPaymentDocNbr,
                        paymentDetail::getId
                );
                generalLedgerPendingEntryService.delete(documentId);
                withGlpes = true;
            }
            if (withGlpes && withoutGlpes) {
                throw new IllegalStateException("Some payments in payment group " + paymentGroup.getId()
                        + " have GLPEs while others have already been processed, cannot cancel payment group.");
            }
        }
        return withGlpes;
    }

    @Override
    public boolean reissueDisbursement(final Integer paymentGroupId, final String note, final Person user) {
        LOG.debug("reissueDisbursement() started");

        // All actions must be performed on entire group not individual detail record

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("reissueDisbursement() Disbursement not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_NOT_FOUND);
            return false;
        }

        final String disbursementTypeCode = paymentGroup.getDisbursementTypeCode();
        if (disbursementTypeCode != null
                && !PdpConstants.DisbursementTypeCodes.ACH.equals(disbursementTypeCode)
                && !PdpConstants.DisbursementTypeCodes.CHECK.equals(disbursementTypeCode)) {
            LOG.debug("reissueDisbursement() Disbursement type cannot be {}", disbursementTypeCode);
            GlobalVariables.getMessageMap().putError(
                    KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL_AND_REISSUE);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT.equals(paymentStatus)
                    && ObjectUtils.isNotNull(paymentGroup.getDisbursementDate())) {
                LOG.debug("reissueDisbursement() Payment status is {}; continue with reissue.", paymentStatus);

                final List<PaymentGroup> allDisbursementPaymentGroups = paymentGroupService.getByDisbursementNumber(
                        paymentGroup.getDisbursementNbr().intValue());

                for (final PaymentGroup pg : allDisbursementPaymentGroups) {
                    final PaymentGroupHistory pgh = new PaymentGroupHistory();

                    if (!pg.getPaymentDetails().get(0).isDisbursementActionAllowed()) {
                        LOG.warn("cancelDisbursement() Payment does not allow disbursement action. This should " +
                                "not happen unless user is URL spoofing.");
                        throw new RuntimeException("cancelDisbursement() Payment does not allow disbursement " +
                                "action. This should not happen unless user is URL spoofing.");
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

                    glPendingTransactionService.generateReissueGeneralLedgerPendingEntries(pg);

                    LOG.debug(
                            "cancelReissueDisbursement() Status is '{}; delete row from AchAccountNumber table.",
                            paymentStatus
                    );

                    final AchAccountNumber achAccountNumber = pg.getAchAccountNumber();

                    if (ObjectUtils.isNotNull(achAccountNumber)) {
                        businessObjectService.delete(achAccountNumber);
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
                    pg.setDisbursementType(null);
                    pg.setProcess(null);
                    pg.setProcessImmediate(false);

                    processPdpReissue(pg);

                    changeStatus(pg, PdpConstants.PaymentStatusCodes.OPEN,
                            PdpConstants.PaymentChangeCodes.REISSUE_DISBURSEMENT, note, user, pgh);
                }

                LOG.debug("reissueDisbursement() Disbursement reissued; exit method.");
            } else {
                LOG.debug(
                        "cancelReissueDisbursement() Payment status is {} and disbursement date is {}; cannot cancel payment",
                        () -> paymentStatus,
                        paymentGroup::getDisbursementDate
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL_AND_REISSUE);
                return false;
            }
        } else {
            LOG.debug("cancelReissueDisbursement() Disbursement already cancelled and reissued; exit method.");
        }
        return true;
    }

    /**
     * @param paymentGroup to execute reissue logic on per purap integration service
     */
    void processPdpReissue(final PaymentGroup paymentGroup) {
        final List<PaymentDetail> paymentDetails = new ArrayList<>(paymentGroup.getPaymentDetails());

        paymentGroup.setEpicPaymentCancelledExtractedDate(null);

        for (final PaymentDetail paymentDetail : paymentDetails) {
            final String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
            final String documentNumber = paymentDetail.getCustPaymentDocNbr();

            if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
                purchasingAccountsPayableModuleService.handlePurchasingBatchReissue(documentNumber, documentTypeCode);
            }
        }
    }

    @Override
    public boolean cancelReissueDisbursement(final Integer paymentGroupId, final String note, final Person user) {
        LOG.debug("cancelReissueDisbursement() started");

        // All actions must be performed on entire group not individual detail record

        if (!pdpAuthorizationService.hasCancelPaymentPermission(user.getPrincipalId())) {
            LOG.warn(
                    "cancelReissueDisbursement() User {} does not have rights to cancel payments. This should not happen unless user is URL spoofing.",
                    user::getPrincipalId
            );
            throw new RuntimeException("cancelReissueDisbursement() User " + user.getPrincipalId() + " does not " +
                    "have rights to cancel payments. This should not happen unless user is URL spoofing.");
        }

        final PaymentGroup paymentGroup = paymentGroupService.get(paymentGroupId);
        if (paymentGroup == null) {
            LOG.debug("cancelReissueDisbursement() Disbursement not found; throw exception.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_NOT_FOUND);
            return false;
        }

        final String disbursementTypeCode = paymentGroup.getDisbursementTypeCode();
        if (disbursementTypeCode != null
                && !PdpConstants.DisbursementTypeCodes.ACH.equals(disbursementTypeCode)
                && !PdpConstants.DisbursementTypeCodes.CHECK.equals(disbursementTypeCode)) {
            LOG.debug("cancelReissueDisbursement() Disbursement type cannot be {}", disbursementTypeCode);
            GlobalVariables.getMessageMap().putError(
                    KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL_AND_REISSUE);
            return false;
        }

        final String paymentStatus = paymentGroup.getPaymentStatus().getCode();

        if (!PdpConstants.PaymentStatusCodes.OPEN.equals(paymentStatus)) {
            if (PdpConstants.PaymentStatusCodes.EXTRACTED.equals(paymentStatus)
                && ObjectUtils.isNotNull(paymentGroup.getDisbursementDate())
                || PdpConstants.PaymentStatusCodes.PENDING_ACH.equals(paymentStatus)) {
                LOG.debug("cancelReissueDisbursement() Payment status is {}; continue with cancel.", paymentStatus);

                final List<PaymentGroup> allDisbursementPaymentGroups = paymentGroupService.getByDisbursementNumber(
                        paymentGroup.getDisbursementNbr().intValue());

                for (final PaymentGroup pg : allDisbursementPaymentGroups) {
                    final PaymentGroupHistory pgh = new PaymentGroupHistory();

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

                    LOG.debug(
                            "cancelReissueDisbursement() Status is '{}; delete row from AchAccountNumber table.",
                            paymentStatus
                    );

                    final AchAccountNumber achAccountNumber = pg.getAchAccountNumber();

                    if (ObjectUtils.isNotNull(achAccountNumber)) {
                        businessObjectService.delete(achAccountNumber);
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
                    pg.setDisbursementType(null);
                    pg.setProcess(null);
                    pg.setProcessImmediate(false);

                    changeStatus(pg, PdpConstants.PaymentStatusCodes.OPEN,
                            PdpConstants.PaymentChangeCodes.CANCEL_REISSUE_DISBURSEMENT, note, user, pgh);
                }

                LOG.debug("cancelReissueDisbursement() Disbursement cancelled and reissued; exit method.");
            } else {
                LOG.debug(
                        "cancelReissueDisbursement() Payment status is {} and disbursement date is {}; cannot cancel payment",
                        () -> paymentStatus,
                        paymentGroup::getDisbursementDate
                );

                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_DISBURSEMENT_INVALID_TO_CANCEL_AND_REISSUE);
                return false;
            }
        } else {
            LOG.debug("cancelReissueDisbursement() Disbursement already cancelled and reissued; exit method.");
        }
        return true;
    }

    public void setPaymentGroupDao(final PaymentGroupDao dao) {
        paymentGroupDao = dao;
    }

    public void setPaymentDetailDao(final PaymentDetailDao dao) {
        paymentDetailDao = dao;
    }

    public void setGlPendingTransactionService(final PendingTransactionService service) {
        glPendingTransactionService = service;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBankService(final BankService bankService) {
        this.bankService = bankService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentGroupService(final PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setPdpEmailService(final PdpEmailService pdpEmailService) {
        this.pdpEmailService = pdpEmailService;
    }

    public void setPdpAuthorizationService(final PdpAuthorizationService pdpAuthorizationService) {
        this.pdpAuthorizationService = pdpAuthorizationService;
    }

    public void setGeneralLedgerPendingEntryService(
            final GeneralLedgerPendingEntryService generalLedgerPendingEntryService) {
        this.generalLedgerPendingEntryService = generalLedgerPendingEntryService;
    }

    public void setPurchasingAccountsPayableModuleService(
            final PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService) {
        this.purchasingAccountsPayableModuleService = purchasingAccountsPayableModuleService;
    }
}
