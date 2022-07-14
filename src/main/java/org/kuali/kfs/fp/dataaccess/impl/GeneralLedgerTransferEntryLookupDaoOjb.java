/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.fp.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferEntry;
import org.kuali.kfs.fp.dataaccess.GeneralLedgerTransferEntryLookupDao;
import org.kuali.kfs.gl.OJBUtility;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GeneralLedgerTransferEntryLookupDaoOjb extends PlatformAwareDaoBaseOjb implements GeneralLedgerTransferEntryLookupDao {
    @Override
    public List<GeneralLedgerTransferEntry> findCollectionBySearchHelper(Integer currentFiscalYear,
        Collection<String> fundGroups, Collection<String> subFundGroups, Map<String, String> formProps) {
        Criteria criteria = OJBUtility.buildCriteriaFromMap(formProps, new GeneralLedgerTransferEntry());
        criteria.addAndCriteria(buildExcludeCorrectingDocumentEntriesCriteria());
        criteria.addNotExists(buildDocumentInErrorEntriesCriteria());
        criteria.addAndCriteria(buildFundAndSubFundGroupCriteria(currentFiscalYear, fundGroups, subFundGroups));
        criteria.addNotIn(KFSPropertyConstants.DOCUMENT_NUMBER, buildDocumentExclusions());

        //PDP_PMT_DTL_T.DISB_NBR is a number filed while FDOC_NBR is a varchar field. This causes an invalid number error to be generated
        //in Oracle which in turn causes a stacktrace. Adding TO_CHAR to the disbursement number fixes the issue for Oracle databases.
        String paymentDisbursementNumberCriteriaValue = new String("TO_CHAR(" + PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER + ")");
        
        
        // PDP stores custPaymentDocNbr in documentNumber when it is a FIN document. It stores disbursementNumber in
        // documentNumber when it is a loaded pdp file. Hence we need to do exclusions each way for PDP
        criteria.addNotIn(KFSPropertyConstants.DOCUMENT_NUMBER,
            buildExcludeCancelledPdpPayments(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR));
        criteria.addNotIn(KFSPropertyConstants.DOCUMENT_NUMBER,
            buildExcludeCancelledPdpPayments(paymentDisbursementNumberCriteriaValue));
        criteria.addAndCriteria(buildMaxSequenceNumberIfPdpReissuedCriteria(
            PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR));

        criteria.addAndCriteria(buildPdpPaymentFileReissuedExtraCriteria());

        return (List<GeneralLedgerTransferEntry>) getPersistenceBrokerTemplate().
            getCollectionByQuery(new QueryByCriteria(GeneralLedgerTransferEntry.class, criteria));
    }

    private Criteria buildExcludeCorrectingDocumentEntriesCriteria() {
        Criteria notFinancialsDocumentCriteria = new Criteria();
        notFinancialsDocumentCriteria.addNotEqualTo(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE,
            KFSConstants.ORIGIN_CODE_KUALI);

        Criteria excludeCorrectingDocumentEntriesCriteria = new Criteria();
        excludeCorrectingDocumentEntriesCriteria.addIsNull(KFSConstants.DOCUMENT_HEADER_PROPERTY_NAME + "."
            + KFSPropertyConstants.FINANCIAL_DOCUMENT_IN_ERROR_NUMBER);

        Criteria criteria = new Criteria();
        criteria.addOrCriteria(excludeCorrectingDocumentEntriesCriteria);
        criteria.addOrCriteria(notFinancialsDocumentCriteria);

        // Checking WORKFLOW_DOCUMENT_STATUS_CODE is not necessary on these because they won't post if they are
        // cancelled or disapproved

        return criteria;
    }

    private QueryByCriteria buildDocumentInErrorEntriesCriteria() {
        Criteria criteria = new Criteria();
        criteria.addEqualToField(KFSPropertyConstants.FINANCIAL_DOCUMENT_IN_ERROR_NUMBER,
            Criteria.PARENT_QUERY_PREFIX + KFSPropertyConstants.DOCUMENT_NUMBER);
        criteria.addNotEqualTo(KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
            KFSConstants.DocumentStatusCodes.CANCELLED);
        criteria.addNotEqualTo(KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
            KFSConstants.DocumentStatusCodes.DISAPPROVED);

        return QueryFactory.newQuery(FinancialSystemDocumentHeader.class, criteria);
    }

    private Criteria buildFundAndSubFundGroupCriteria(Integer currentFiscalYear, Collection<String> fundGroups,
            Collection<String> subFundGroups) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, currentFiscalYear);
        if (!fundGroups.isEmpty()) {
            Criteria criteriaFundGroup = new Criteria();
            criteriaFundGroup.addIn(KFSPropertyConstants.ACCOUNT + "." + KFSPropertyConstants.SUB_FUND_GROUP
                + "." + KFSPropertyConstants.FUND_GROUP_CODE, fundGroups);
            criteria.addOrCriteria(criteriaFundGroup);
        }
        if (!subFundGroups.isEmpty()) {
            Criteria criteriaSubFundGroup = new Criteria();
            criteriaSubFundGroup.addIn(KFSPropertyConstants.ACCOUNT
                + "." + KFSPropertyConstants.SUB_FUND_GROUP_CODE, subFundGroups);
            criteria.addOrCriteria(criteriaSubFundGroup);
        }

        return criteria;
    }
    
    private ReportQueryByCriteria buildDocumentExclusions() {
        Criteria criteria = new Criteria();
        criteria.addIn(KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME,
                Arrays.asList(KFSConstants.FinancialDocumentTypeCodes.PAYMENT_REQUEST,
                    KFSConstants.FinancialDocumentTypeCodes.VENDOR_CREDIT_MEMO));
        criteria.addNotIn(KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                Arrays.asList(KFSConstants.DocumentStatusCodes.PROCESSED, KFSConstants.DocumentStatusCodes.FINAL));

        ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(FinancialSystemDocumentHeader.class, criteria);
        subQuery.setAttributes(new String[]{KFSPropertyConstants.DOCUMENT_NUMBER});
        return subQuery;
    }

    private ReportQueryByCriteria buildExcludeCancelledPdpPayments(String entryDocumentNumberField) {
        Criteria criteria = new Criteria();
        criteria.addIn(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE,
            Arrays.asList(PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT,
                PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT));
        criteria.addEqualToField(entryDocumentNumberField,
            Criteria.PARENT_QUERY_PREFIX + KFSPropertyConstants.DOCUMENT_NUMBER);

        ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(PaymentDetail.class, criteria);
        subQuery.setAttributes(new String[]{entryDocumentNumberField});
        return subQuery;
    }

    private Criteria buildMaxSequenceNumberIfPdpReissuedCriteria(String entryDocumentNumberField) {
        Criteria maxSequenceNumberAndPdpReissuedCriteria = new Criteria();
        maxSequenceNumberAndPdpReissuedCriteria.addIn(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER,
            buildSequenceNumberAndPdpReissuedCriteria(true, entryDocumentNumberField));

        Criteria ifExistsCriteria = new Criteria();
        ifExistsCriteria.addNotExists(buildSequenceNumberAndPdpReissuedCriteria(false, entryDocumentNumberField));

        maxSequenceNumberAndPdpReissuedCriteria.addOrCriteria(ifExistsCriteria);

        return maxSequenceNumberAndPdpReissuedCriteria;
    }

    private ReportQueryByCriteria buildSequenceNumberAndPdpReissuedCriteria(boolean includeMax, String entryDocumentNumberField) {
        Criteria paymentDetailCriteria = new Criteria();
        paymentDetailCriteria.addEqualToField(entryDocumentNumberField,
            Criteria.PARENT_QUERY_PREFIX + KFSPropertyConstants.DOCUMENT_NUMBER);
        paymentDetailCriteria.addIn(PdpPropertyConstants.PAYMENT_GROUP + "."
                + PdpPropertyConstants.PAYMENT_GROUP_HISTORY + "." + PdpPropertyConstants.PAYMENT_CHANGE_CODE,
            Arrays.asList(PdpConstants.PaymentChangeCodes.CANCEL_REISSUE_DISBURSEMENT,
                PdpConstants.PaymentChangeCodes.REISSUE_DISBURSEMENT));
        ReportQueryByCriteria paymentDetailQuery = QueryFactory.newReportQuery(PaymentDetail.class, paymentDetailCriteria);
        paymentDetailQuery.setAttributes(new String[]{entryDocumentNumberField});

        Criteria entryCriteria = new Criteria();
        entryCriteria.addIn(KFSPropertyConstants.DOCUMENT_NUMBER, paymentDetailQuery);
        entryCriteria.addEqualToField(KFSPropertyConstants.DOCUMENT_NUMBER,
            Criteria.PARENT_QUERY_PREFIX + KFSPropertyConstants.DOCUMENT_NUMBER);

        ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(GeneralLedgerTransferEntry.class, entryCriteria);
        if (includeMax) {
            subQuery.setAttributes(new String[]{"max(" + KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER + ")"});
        } else {
            subQuery.setAttributes(new String[]{KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER});
        }
        return subQuery;
    }

    private Criteria buildPdpPaymentFileReissuedExtraCriteria() {
        // Reissuing a PDP payment file causes the disbursementNumber to get reissued. Because we don't want the old
        // Entries to be selectable, we check that every entry has a PaymentGroup entry. This is not applicable to
        // Payment Groups that originate as FIN documents
        Criteria paymentGroupCriteria = new Criteria();
        paymentGroupCriteria.addEqualToField(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_NBR,
            Criteria.PARENT_QUERY_PREFIX + KFSPropertyConstants.DOCUMENT_NUMBER);
        QueryByCriteria paymentGroupQuery = QueryFactory.newQuery(PaymentGroup.class, paymentGroupCriteria);

        // Only applicable to the following PDP documentTypes
        Criteria documentTypeCriteria = new Criteria();
        documentTypeCriteria.addNotIn(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE,
            Arrays.asList(PdpConstants.FDOC_TYP_CD_PROCESS_ACH, PdpConstants.FDOC_TYP_CD_PROCESS_CHECK,
                PdpConstants.FDOC_TYP_CD_CANCEL_REISSUE_ACH, PdpConstants.FDOC_TYP_CD_CANCEL_REISSUE_CHECK,
                PdpConstants.FDOC_TYP_CD_CANCEL_ACH, PdpConstants.FDOC_TYP_CD_CANCEL_CHECK));

        Criteria criteria = new Criteria();
        criteria.addExists(paymentGroupQuery);
        criteria.addOrCriteria(documentTypeCriteria);

        return criteria;
    }
}