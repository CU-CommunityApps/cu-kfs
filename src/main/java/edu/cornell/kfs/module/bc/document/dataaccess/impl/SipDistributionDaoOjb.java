package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionGeneralLedger;
import org.kuali.kfs.module.bc.util.BudgetParameterFinder;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.util.KualiInteger;
import org.kuali.rice.kns.util.TransactionalServiceUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao;

public class SipDistributionDaoOjb extends PlatformAwareDaoBaseOjb implements SipDistributionDao {

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#getExpendituresTotal(java.lang.String,
     * int)
     */
    public KualiInteger getExpendituresTotal(String documentNumber, int fiscalYear) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR,
                documentNumber);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                fiscalYear);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD,
                KFSConstants.BALANCE_TYPE_BASE_BUDGET);
        criteria.addIn(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD,
                BudgetParameterFinder.getExpenditureObjectTypes());

        ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(PendingBudgetConstructionGeneralLedger.class,
                criteria);
        reportQuery.setAttributes(new String[]{"sum("
                + CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACC_LINE_ANN_BAL_AMT + ")"});

        Iterator iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
        if (iterator.hasNext()) {
            KualiInteger returnResult = new KualiInteger(((BigDecimal) ((Object[]) TransactionalServiceUtils
                    .retrieveFirstAndExhaustIterator(iterator))[0]).intValue());
            return returnResult;
        } else {
            return KualiInteger.ZERO;
        }
    }

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#getRevenuesTotal(java.lang.String,
     * int)
     */
    public KualiInteger getRevenuesTotal(String documentNumber, int fiscalYear) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR,
                documentNumber);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                fiscalYear);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD,
                KFSConstants.BALANCE_TYPE_BASE_BUDGET);
        criteria.addIn(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD,
                BudgetParameterFinder.getRevenueObjectTypes());

        ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(PendingBudgetConstructionGeneralLedger.class,
                criteria);
        reportQuery.setAttributes(new String[]{"sum("
                + CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACC_LINE_ANN_BAL_AMT + ")"});

        Iterator iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
        if (iterator.hasNext()) {
            Object[] returnResult = (Object[]) TransactionalServiceUtils
                    .retrieveFirstAndExhaustIterator(iterator);
            if (returnResult != null && returnResult[0] != null) {
                KualiInteger revenueTotal = new KualiInteger(((BigDecimal) (returnResult)[0]).intValue());
                return revenueTotal;
            } else
                return KualiInteger.ZERO;
        } else {
            return KualiInteger.ZERO;
        }
    }

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#get2PLGAmount(java.lang.String,
     * int)
     */
    public KualiInteger get2PLGAmount(String documentNumber, int fiscalYear) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR,
                documentNumber);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                fiscalYear);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD,
                KFSConstants.BALANCE_TYPE_BASE_BUDGET);
        criteria.addIn(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD,
                BudgetParameterFinder.getExpenditureObjectTypes());
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJECT_CODE,
                KFSConstants.BudgetConstructionConstants.OBJECT_CODE_2PLG);

        ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(PendingBudgetConstructionGeneralLedger.class,
                criteria);
        reportQuery
                .setAttributes(new String[]{CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACC_LINE_ANN_BAL_AMT});

        Iterator iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
        if (iterator.hasNext()) {
            KualiInteger returnResult = new KualiInteger(((BigDecimal) ((Object[]) TransactionalServiceUtils
                    .retrieveFirstAndExhaustIterator(iterator))[0]).intValue());
            return returnResult;
        } else {
            return KualiInteger.ZERO;
        }
    }

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#getSipPoolAmount(java.lang.String,
     * java.util.List, int)
     */
    public KualiInteger getSipPoolAmount(String documentNumber, List<String> sipLevelObjectCodes, int fiscalYear) {

        KualiInteger sipPoolAmount = KualiInteger.ZERO;

        if (sipLevelObjectCodes != null && sipLevelObjectCodes.size() > 0) {
            Criteria criteria = new Criteria();

            criteria.addEqualTo(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR,
                    documentNumber);
            criteria.addEqualTo(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                    fiscalYear);
            criteria.addEqualTo(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD,
                    KFSConstants.BALANCE_TYPE_BASE_BUDGET);
            criteria.addIn(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJECT_CODE,
                    sipLevelObjectCodes);
            criteria.addIn(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD,
                    BudgetParameterFinder.getExpenditureObjectTypes());

            ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(
                    PendingBudgetConstructionGeneralLedger.class,
                    criteria);
            reportQuery
                    .setAttributes(new String[]{"sum("
                            + CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACC_LINE_ANN_BAL_AMT
                            + ")"});

            Iterator iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
            if (iterator.hasNext()) {
                Object[] returnResult = (Object[]) TransactionalServiceUtils
                        .retrieveFirstAndExhaustIterator(iterator);
                if (returnResult != null && returnResult[0] != null) {
                    sipPoolAmount = new KualiInteger(((BigDecimal) (returnResult)[0]).intValue());
                    return sipPoolAmount;
                } else
                    return KualiInteger.ZERO;
            } else {
                return KualiInteger.ZERO;
            }
        }

        return sipPoolAmount;

    }

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#getSipPoolEntries(java.util.List,
     * java.util.List, int)
     */
    public List<PendingBudgetConstructionGeneralLedger> getSipPoolEntries(List<String> docNbrs,
            List<String> sipLevelObjectCodes, int fiscalYear) {

        if (sipLevelObjectCodes != null && sipLevelObjectCodes.size() > 0) {
            Criteria criteria = new Criteria();

            criteria.addIn(CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR, docNbrs);
            criteria.addEqualTo(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                    fiscalYear);
            criteria.addEqualTo(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD,
                    KFSConstants.BALANCE_TYPE_BASE_BUDGET);
            criteria.addIn(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJECT_CODE,
                    sipLevelObjectCodes);
            criteria.addIn(
                    CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD,
                    BudgetParameterFinder.getExpenditureObjectTypes());

            QueryByCriteria queryByCriteria = QueryFactory.newQuery(PendingBudgetConstructionGeneralLedger.class,
                    criteria);

            return (List<PendingBudgetConstructionGeneralLedger>) getPersistenceBrokerTemplate().getCollectionByQuery(
                    queryByCriteria);
        } else
            return null;

    }

    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipDistributionDao#getSipLevelObjectCodes(int)
     */
    public List<String> getSipLevelObjectCodes(int fiscalYear) {

        List<String> sipLevelObjectCodes = new ArrayList<String>();

        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL_CODE,
                CUBCConstants.SIP_FINANCIAL_OBJECT_LEVEL_CODE);
        criteria.addEqualTo(
                CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR,
                fiscalYear);

        ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(ObjectCode.class, criteria);
        reportQuery.setAttributes(new String[]{KFSPropertyConstants.FINANCIAL_OBJECT_CODE});

        Iterator<Object[]> iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);

        while (iterator.hasNext()) {
            String objectCode = (String) iterator.next()[0];
            sipLevelObjectCodes.add(objectCode);
        }

        return sipLevelObjectCodes;
    }

}
