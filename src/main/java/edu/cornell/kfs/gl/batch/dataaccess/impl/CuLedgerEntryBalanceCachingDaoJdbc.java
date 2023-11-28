package edu.cornell.kfs.gl.batch.dataaccess.impl;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.gl.batch.dataaccess.LedgerEntryBalanceCachingDao;
import org.kuali.kfs.gl.batch.dataaccess.impl.LedgerEntryBalanceCachingDaoJdbc;

import edu.cornell.kfs.gl.batch.dataaccess.CuLedgerEntryBalanceCachingDao;

public class CuLedgerEntryBalanceCachingDaoJdbc extends LedgerEntryBalanceCachingDaoJdbc implements CuLedgerEntryBalanceCachingDao {

	@Override
    public List compareEntryHistory(final String entryTable, final String historyTable, final int fiscalYear) {
        List<Map<String, Object>> data = null;
                
        final StringBuilder queryBuilder = new StringBuilder();
        final String drop = "delete from GL_ENTRY_STATS_T";
                
        final StringBuilder populate = new StringBuilder();
        
        populate.append("insert into gl_entry_stats_t ");
        populate.append("SELECT UNIV_FISCAL_YR, FIN_COA_CD, FIN_OBJECT_CD, FIN_BALANCE_TYP_CD, UNIV_FISCAL_PRD_CD, TRN_DEBIT_CRDT_CD, count(*) as entry_row_cnt, sum(TRN_LDGR_ENTR_AMT) as entry_amt ");
        populate.append("FROM GL_ENTRY_T where GL_ENTRY_T.UNIV_FISCAL_YR='");
        populate.append(fiscalYear);
        populate.append("' ");
        populate.append("GROUP BY UNIV_FISCAL_YR, FIN_COA_CD, FIN_OBJECT_CD, FIN_BALANCE_TYP_CD, UNIV_FISCAL_PRD_CD, TRN_DEBIT_CRDT_CD");
        
                
        queryBuilder.append("select eh.* ");
        queryBuilder.append("from " + historyTable + " eh ");
        queryBuilder.append("left join ");
        queryBuilder.append("GL_ENTRY_STATS_T e ");
        queryBuilder.append("on eh.univ_fiscal_yr = e.univ_fiscal_yr and eh.fin_coa_cd = e.fin_coa_cd and eh.fin_object_cd = e.fin_object_cd and ");
        queryBuilder.append("eh.fin_balance_typ_cd = e.fin_balance_typ_cd and eh.univ_fiscal_prd_cd = e.univ_fiscal_prd_cd and eh.trn_debit_crdt_cd = e.trn_debit_crdt_cd ");
        queryBuilder.append("where e.univ_fiscal_yr >= " + fiscalYear + " and (eh.row_cnt <> e.entry_row_cnt or eh.trn_ldgr_entr_amt <> e.entry_amt or e.entry_row_cnt is null) ");
        getJdbcTemplate().execute(drop.toString());
        getJdbcTemplate().execute(populate.toString());        
      
        data = getJdbcTemplate().queryForList(queryBuilder.toString());
        
        return data;

    }

}
