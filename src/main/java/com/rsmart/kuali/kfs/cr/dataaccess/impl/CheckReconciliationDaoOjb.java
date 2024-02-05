/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.dataaccess.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.PdpConstants.DisbursementTypeCodes;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;

/**
 * Check Reconciliation DAO Impl for OJB
 * 
 * @author Derek Helbert
 * @version $revision$
 */
public class CheckReconciliationDaoOjb extends PlatformAwareDaoBaseOjb implements CheckReconciliationDao {

	private static final Logger LOG = LogManager.getLogger(CheckReconciliationDaoOjb.class);
    
    /**
     * Get All
     * 
     * @see com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao#getAll()
     */
    public List getAll() {
        LOG.info("getAll() started");

        QueryByCriteria qbc = new QueryByCriteria(CheckReconciliationDaoOjb.class);

        List list = (List) getPersistenceBrokerTemplate().getCollectionByQuery(qbc);

        return list;
    }

    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao#getCanceledChecks()
     */
    public Collection<Integer> getCanceledChecks() {        
        Collection<Integer> data = new ArrayList<Integer>();
        
        //String sql = "SELECT cr.cr_id FROM pdp_pmt_grp_t p, fp_bank_t b, cu_cr_check_recon_t cr where p.bnk_cd = b.bnk_cd and p.disb_typ_cd = 'CHCK' and cr.check_nbr = p.disb_nbr AND cr.bank_account_nbr = b.bnk_acct_nbr and p.lst_updt_ts > cr.lst_updt_ts and p.pmt_stat_cd in ('CDIS','CPAY') and gl_trans_ind = 'N'";
        // removed condition p.lst_updt_ts > cr.lst_updt_ts. Checks are by default set to issued status on check recon table. This will update them to cancelled if they are not move to gl.
        String sql = "SELECT cr.cr_id FROM pdp_pmt_grp_t p, fp_bank_t b, cu_cr_check_recon_t cr where cr.actv_ind='Y' and p.bnk_cd = b.bnk_cd and p.disb_typ_cd = 'CHCK' and cr.check_nbr = p.disb_nbr AND cr.bank_account_nbr = b.bnk_acct_nbr and  p.pmt_stat_cd in ('CDIS','CPAY') and gl_trans_ind = 'N'";
        
        try {
            Connection c = getPersistenceBroker(true).serviceConnectionManager().getConnection();
            Statement  s = c.createStatement();
            ResultSet rs = s.executeQuery(sql);
            
            String bnkCd = null;
            
            while(rs.next()) {
                data.add( rs.getInt(1) );
            }
            
            s.close();
        }
        catch (Exception e) {
            LOG.error("getCancelledChecks", e);
        }
        
        return data;
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao#getNewCheckReconciliations(java.util.Collection)
     */
    public Collection<CheckReconciliation> getNewCheckReconciliations(Collection<Bank> banks) {        
        Collection<CheckReconciliation> data = new ArrayList<CheckReconciliation>();
        CheckReconciliation cr = null;
        
     //   String sql = "SELECT p.disb_nbr, p.disb_ts, SUM(d.net_pmt_amt), b.bnk_cd,p.pmt_payee_nm, p.payee_id_typ_cd,p.payee_id FROM pdp_pmt_grp_t p, pdp_pmt_dtl_t d, fp_bank_t b WHERE p.bnk_cd = b.bnk_cd AND p.pmt_grp_id = d.pmt_grp_id AND p.disb_typ_cd = 'CHCK' AND NOT EXISTS ( SELECT 'x' from cu_cr_check_recon_t cr WHERE cr.check_nbr = p.disb_nbr AND cr.bank_account_nbr = b.bnk_acct_nbr) GROUP BY p.disb_nbr, p.disb_ts, b.bnk_cd";
       
        String sql ="SELECT p.disb_nbr, p.disb_ts, SUM(d.net_pmt_amt), b.bnk_cd,p.pmt_payee_nm, p.payee_id_typ_cd,p.payee_id  FROM pdp_pmt_grp_t p, pdp_pmt_dtl_t d, fp_bank_t b WHERE p.bnk_cd = b.bnk_cd AND p.pmt_grp_id = d.pmt_grp_id AND p.disb_ts is not null AND p.disb_typ_cd = 'CHCK' AND NOT EXISTS ( SELECT 'x' from cu_cr_check_recon_t cr WHERE cr.actv_ind='Y' and cr.check_nbr = p.disb_nbr AND cr.bank_account_nbr = b.bnk_acct_nbr) group by p.disb_nbr, p.disb_ts, b.bnk_cd, p.pmt_payee_nm, p.payee_id_typ_cd, p.payee_id";
        try {
            Connection c = getPersistenceBroker(true).serviceConnectionManager().getConnection();
            Statement  s = c.createStatement();
            ResultSet rs = s.executeQuery(sql);
            
            String bnkCd = null;
            
            while(rs.next()) {
                cr = new CheckReconciliation();
                cr.setCheckNumber(new KualiInteger(rs.getInt(1)));
                cr.setCheckDate(new java.sql.Date(rs.getDate(2).getTime()));  //This is last status change date.
                cr.setAmount(new KualiDecimal(rs.getDouble(3)));
                bnkCd = rs.getString(4);
                cr.setPayeeName(rs.getString(5));
                cr.setPayeeType(rs.getString(6));
                cr.setPayeeId(rs.getString(7));
                
                for( Bank bank : banks ) {
                    if( bank.getBankCode().equals(bnkCd) ) {
                        cr.setBankAccountNumber(bank.getBankAccountNumber());
                    }
                }
                
                cr.setGlTransIndicator(Boolean.FALSE);
                if(cr.getAmount().isZero()) {
                	cr.setStatus(CRConstants.EXCP);
                } else {
                	cr.setStatus(CRConstants.ISSUED);
                }
                cr.setSourceCode(CRConstants.PDP_SRC);
                cr.setBankCode(bnkCd);
                
                data.add(cr);
            }
            
            s.close();
        }
        catch (Exception e) {
            LOG.error("getNewCheckReconciliations", e);
        }
        
        return data;
    }
    
    /**
     * Get All Check Reconciliation For Search Criteria
     * 
     * @see com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao#getAllCheckReconciliationForSearchCriteria(java.util.Date, java.util.Date)
     */
    public List getAllCheckReconciliationForSearchCriteria(Date startDate, Date endDate) {
        LOG.info("getAllCheckReconciliationForSearchCriteria() starting");
        Criteria criteria = new Criteria();

        criteria.addEqualTo("status", CRConstants.ISSUED);
        criteria.addEqualTo("active", true);

        if (!(startDate == null)) {
            criteria.addGreaterOrEqualThan("checkDate", startDate);
        }
        if (!(endDate == null)) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(endDate);
//            gc.add(Calendar.DATE, 1);
            
            criteria.addLessOrEqualThan("checkDate", new Timestamp(gc.getTimeInMillis()));
        }

        QueryByCriteria qbc = new QueryByCriteria(CheckReconciliation.class, criteria);
        qbc.addOrderBy("bankAccountNumber", true);
        qbc.addOrderBy("checkDate", true);

        LOG.info("getAllCheckReconciliationForSearchCriteria() Query = " + qbc.toString());
        
        List list = (List) getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
        return list;
    }
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao#getAllPaymentGroupForSearchCriteria(java.lang.String, java.util.Collection)
     */
    public List<PaymentGroup> getAllPaymentGroupForSearchCriteria(KualiInteger disbNbr, Collection<String> bankCodes) {
        LOG.info("getAllPaymentGroupForSearchCriteria() starting");
        Criteria criteria = new Criteria();

        criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_NBR, disbNbr);
        criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, DisbursementTypeCodes.CHECK);
        criteria.addIn(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCodes);
        
        QueryByCriteria qbc = new QueryByCriteria(PaymentGroup.class, criteria);

        LOG.info("getAllPaymentGroupForSearchCriteria() Query = " + qbc.toString());
        
        List list = (List) getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
        
        return list;
    }

    public CheckReconciliation findByCheckNumber(String checkNumber, String bankCode) {
        Criteria criteria = new Criteria();

        criteria.addEqualTo(CRConstants.CU_CR_CHECK_RECON_T_CHECK_NBR_COL, checkNumber);
        criteria.addEqualTo(CRConstants.CU_CR_CHECK_RECON_T_BNK_CD_COL, bankCode);
        QueryByCriteria qbc = new QueryByCriteria(CheckReconciliation.class, criteria);

        CheckReconciliation checkReconciliation = (CheckReconciliation)getPersistenceBrokerTemplate().getObjectByQuery(qbc);
        return checkReconciliation;
    }
}
