/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.fp.document.dataaccess.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferEntry;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferSourceAccountingLine;
import org.kuali.kfs.fp.document.dataaccess.GeneralLedgerTransferEntryDao;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.ArrayList;
import java.util.List;

public class GeneralLedgerTransferEntryDaoOjb extends PlatformAwareDaoBaseOjb implements GeneralLedgerTransferEntryDao {

    @Override
    public GeneralLedgerTransferEntry findSourceEntryByGeneralLedgerTransferSourceAccountingLine(
        GeneralLedgerTransferSourceAccountingLine gltSourceAccountingLine) {
        Criteria entryCriteria = createGeneralLedgerTransferEntryPrimaryKeyMap(gltSourceAccountingLine, true);

        Query query = QueryFactory.newQuery(GeneralLedgerTransferEntry.class, entryCriteria);
        return (GeneralLedgerTransferEntry) getPersistenceBrokerTemplate().getObjectByQuery(query);
    }

    @Override
    public List<GeneralLedgerTransferEntry> findSourceEntriesByGeneralLedgerTransferSourceAccountingLines(
        List<GeneralLedgerTransferSourceAccountingLine> gltSourceAccountingLines, String newDocumentNumber) {

        if (gltSourceAccountingLines.isEmpty()) {
            return new ArrayList<>();
        }

        Criteria criteria = new Criteria();
        for (GeneralLedgerTransferSourceAccountingLine gltSourceAccountingLine : gltSourceAccountingLines) {
            Criteria entryCriteria = createGeneralLedgerTransferEntryPrimaryKeyMap(gltSourceAccountingLine, true);
            if (newDocumentNumber == null) {
                entryCriteria.addEqualTo(KFSPropertyConstants.GENERAL_LEDGER_TRANSFER_DOCUMENT_NUMBER,
                        gltSourceAccountingLine.getDocumentNumber());
            }
            criteria.addOrCriteria(entryCriteria);
        }

        return (List<GeneralLedgerTransferEntry>) getPersistenceBrokerTemplate().
            getCollectionByQuery(new QueryByCriteria(GeneralLedgerTransferEntry.class, criteria));
    }

    @Override
    public List<GeneralLedgerTransferEntry> findGeneratedEntriesByGeneralLedgerTransferSourceAccountingLines(
        List<GeneralLedgerTransferSourceAccountingLine> gltSourceAccountingLines) {

        if (gltSourceAccountingLines.isEmpty()) {
            return new ArrayList<>();
        }

        Criteria criteria = new Criteria();
        for (GeneralLedgerTransferSourceAccountingLine gltSourceAccountingLine : gltSourceAccountingLines) {
            Criteria entryCriteria = createGeneralLedgerTransferEntryPrimaryKeyMap(gltSourceAccountingLine, false);
            criteria.addOrCriteria(entryCriteria);
        }

        return (List<GeneralLedgerTransferEntry>) getPersistenceBrokerTemplate().
            getCollectionByQuery(new QueryByCriteria(GeneralLedgerTransferEntry.class, criteria));
    }

    private Criteria createGeneralLedgerTransferEntryPrimaryKeyMap(
        GeneralLedgerTransferSourceAccountingLine gltSourceAccountingLine, boolean sourceEntriesMode) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                gltSourceAccountingLine.getChartOfAccountsCode());
        criteria.addEqualTo(KFSPropertyConstants.ACCOUNT_NUMBER, gltSourceAccountingLine.getAccountNumber());
        criteria.addEqualTo(KFSPropertyConstants.SUB_ACCOUNT_NUMBER,
                StringUtils.isEmpty(gltSourceAccountingLine.getSubAccountNumber())
            ? KFSConstants.getDashSubAccountNumber() : gltSourceAccountingLine.getSubAccountNumber());
        criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_OBJECT_CODE,
                StringUtils.isEmpty(gltSourceAccountingLine.getFinancialObjectCode())
            ? KFSConstants.getDashFinancialObjectCode() : gltSourceAccountingLine.getFinancialObjectCode());
        criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE,
                StringUtils.isEmpty(gltSourceAccountingLine.getFinancialSubObjectCode())
            ? KFSConstants.getDashFinancialSubObjectCode() : gltSourceAccountingLine.getFinancialSubObjectCode());

        if (sourceEntriesMode) {
            criteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                    gltSourceAccountingLine.getTransferPostingYear().toString());
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE,
                    gltSourceAccountingLine.getBalanceTypeCode());
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE,
                    gltSourceAccountingLine.getTransferObjectTypeCode());
            criteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE,
                    gltSourceAccountingLine.getTransferFiscalPeriodCode());
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE,
                    gltSourceAccountingLine.getTransferDocumentTypeCode());
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE,
                    gltSourceAccountingLine.getReferenceOriginCode());
            criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_NUMBER, gltSourceAccountingLine.getReferenceNumber());
            criteria.addEqualTo(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER,
                gltSourceAccountingLine.getTransferTransactionLedgerEntrySequenceNumber().toString());
        } else {
            // Not including UNIVERSITY_FISCAL_YEAR and UNIVERSITY_FISCAL_PERIOD_CODE because this could process old
            // documents
            criteria.addEqualTo(KFSPropertyConstants.PROJECT_CODE,
                    StringUtils.isEmpty(gltSourceAccountingLine.getProjectCode())
                ? KFSConstants.getDashProjectCode() : gltSourceAccountingLine.getProjectCode());

            if (StringUtils.isEmpty(gltSourceAccountingLine.getOrganizationReferenceId())) {
                // CUMod KFSPTS-19396: change to addIsNull criteria instead of addEqualTo empty String as that does not work in Oracle
                criteria.addIsNull(KFSPropertyConstants.ORGANIZATION_REFERENCE_ID);
            } else {
                criteria.addEqualTo(KFSPropertyConstants.ORGANIZATION_REFERENCE_ID,
                    gltSourceAccountingLine.getOrganizationReferenceId());
            }

            criteria.addEqualTo(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT,
                gltSourceAccountingLine.getAmount().toString());
            criteria.addEqualTo(KFSPropertyConstants.TRANSACTION_DEBIT_CREDIT_CODE,
                    gltSourceAccountingLine.getDebitCreditCode());
            criteria.addEqualTo(KFSPropertyConstants.REFERENCE_FIN_DOCUMENT_TYPE_CODE,
                gltSourceAccountingLine.getReferenceTypeCode());
            criteria.addEqualTo(KFSPropertyConstants.REFERENCE_FINANCIAL_SYSTEM_ORIGINATION_CODE,
                    gltSourceAccountingLine.getReferenceOriginCode());
            criteria.addEqualTo(KFSPropertyConstants.REFERENCE_FINANCIAL_DOCUMENT_NUMBER,
                    gltSourceAccountingLine.getReferenceNumber());
            criteria.addEqualTo(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC,
                gltSourceAccountingLine.getFinancialDocumentLineDescription());
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE,
                KFSConstants.FinancialDocumentTypeCodes.GENERAL_LEDGER_TRANSFER);
            criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, KFSConstants.ORIGIN_CODE_KUALI);
            criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_NUMBER, gltSourceAccountingLine.getDocumentNumber());
        }

        return criteria;
    }

    public boolean hasTransferredEntries(String documentNumber) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_NUMBER, documentNumber);
        criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, KFSConstants.ORIGIN_CODE_KUALI);
        criteria.addNotNull(KFSPropertyConstants.GENERAL_LEDGER_TRANSFER_DOCUMENT_NUMBER);
        return getPersistenceBrokerTemplate().getCount(new QueryByCriteria(GeneralLedgerTransferEntry.class, criteria)) != 0;
    }
}
