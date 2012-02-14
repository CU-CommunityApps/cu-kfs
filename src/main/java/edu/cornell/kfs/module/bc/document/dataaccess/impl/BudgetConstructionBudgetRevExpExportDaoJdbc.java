package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionBudgetRevExpExportDao;

public class BudgetConstructionBudgetRevExpExportDaoJdbc 
			extends BudgetConstructionDaoJdbcBase 
			implements BudgetConstructionBudgetRevExpExportDao {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BudgetConstructionBudgetRevExpExportDaoJdbc.class);
	
	public BudgetConstructionBudgetRevExpExportDaoJdbc() {
		
	}

    public Collection<BREExportData> getBREExtractByPersonUnivId(String univId) {
       
        try {
        	// Build the SQL
	        StringBuilder sqlBuilder = new StringBuilder(3500);

	        sqlBuilder.append("SELECT LD_PND_BCNSTR_GL_T.FDOC_NBR,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.UNIV_FISCAL_YR,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_COA_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.ACCOUNT_NBR,  ");
	        sqlBuilder.append("  LD_BCN_ACCT_RPTS_T.RPTS_TO_ORG_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.SUB_ACCT_NBR,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_OBJECT_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_SUB_OBJ_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_BALANCE_TYP_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_OBJ_TYP_CD,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.ACLN_ANNL_BAL_AMT,  ");
	        sqlBuilder.append("  LD_PND_BCNSTR_GL_T.FIN_BEG_BAL_LN_AMT,  ");
	        sqlBuilder.append("  LD_BCN_ORG_RPTS_T.RC_CD  ");
	        sqlBuilder.append("FROM  ");
	        sqlBuilder.append("  (SELECT ");
	        sqlBuilder.append("    ctrl.univ_fiscal_yr,  ");
	        sqlBuilder.append("    ctrl.fin_coa_cd,  ");
	        sqlBuilder.append("    ctrl.account_nbr,  ");
	        sqlBuilder.append("    ctrl.sub_acct_nbr,  ");
	        sqlBuilder.append("    ctrl.ver_nbr  ");
	        sqlBuilder.append("  FROM ld_bcn_ctrl_list_t ctrl,  ");
	        sqlBuilder.append("    ld_bcn_subfund_pick_t pick  ");
	        sqlBuilder.append("  WHERE ctrl.person_unvl_id = ? ");
	        sqlBuilder.append("  AND pick.person_unvl_id   = ctrl.person_unvl_id  ");
	        sqlBuilder.append("  AND pick.sub_fund_grp_cd  = ctrl.sel_sub_fund_grp  ");
	        sqlBuilder.append("  AND pick.report_flag      > 0  ");
	        sqlBuilder.append("  ) USERSELECTION  ");
	        sqlBuilder.append("LEFT JOIN LD_PND_BCNSTR_GL_T  ");
	        sqlBuilder.append("ON LD_PND_BCNSTR_GL_T.UNIV_FISCAL_YR = USERSELECTION.UNIV_FISCAL_YR  ");
	        sqlBuilder.append("AND LD_PND_BCNSTR_GL_T.FIN_COA_CD    = USERSELECTION.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_PND_BCNSTR_GL_T.ACCOUNT_NBR   = USERSELECTION.ACCOUNT_NBR  ");
	        sqlBuilder.append("AND LD_PND_BCNSTR_GL_T.SUB_ACCT_NBR  = USERSELECTION.SUB_ACCT_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_ACCT_RPTS_T  ");
	        sqlBuilder.append("ON LD_BCN_ACCT_RPTS_T.FIN_COA_CD   = USERSELECTION.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_ACCT_RPTS_T.ACCOUNT_NBR = USERSELECTION.ACCOUNT_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_ORG_RPTS_T  ");
	        sqlBuilder.append("ON LD_BCN_ORG_RPTS_T.FIN_COA_CD = LD_BCN_ACCT_RPTS_T.RPTS_TO_FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_ORG_RPTS_T.ORG_CD = LD_BCN_ACCT_RPTS_T.RPTS_TO_ORG_CD  ");
	        sqlBuilder.append("ORDER BY FIN_OBJECT_CD, FIN_SUB_OBJ_CD");
	        
	        String sqlString = sqlBuilder.toString();

            // Get the BRE data from the data base, map it and build a result set to be returned to the user.
        
	        ParameterizedRowMapper<BREExportData> mapper = new ParameterizedRowMapper<BREExportData>() {
	            public BREExportData mapRow(ResultSet rs, int rowNum) throws SQLException {
	            	BREExportData breExportData = new BREExportData();
					breExportData.setFdocNbr(rs.getString("FDOC_NBR"));
					breExportData.setUnivFiscalYear(rs.getString("UNIV_FISCAL_YR"));
					breExportData.setFinCoaCd(rs.getString("FIN_COA_CD"));
					breExportData.setAccountNbr(rs.getString("ACCOUNT_NBR"));
					breExportData.setRptsToOrgCd(rs.getString("RPTS_TO_ORG_CD"));
					breExportData.setSubAcctNbr(rs.getString("SUB_ACCT_NBR"));
					breExportData.setFinObjectCd(rs.getString("FIN_OBJECT_CD"));
					breExportData.setFinSubObjCd(rs.getString("FIN_SUB_OBJ_CD"));
					breExportData.setFinBalanceTypCd(rs.getString("FIN_BALANCE_TYP_CD"));
					breExportData.setFinObjTypCd(rs.getString("FIN_OBJ_TYP_CD"));
					breExportData.setAclnAnnlBalAmt(rs.getString("ACLN_ANNL_BAL_AMT"));
					breExportData.setFinBegBalLnAmt(rs.getString("FIN_BEG_BAL_LN_AMT"));
					breExportData.setRcCd(rs.getString("RC_CD"));

	                return breExportData;
	            }
	        };
	        
	        return this.getSimpleJdbcTemplate().query(sqlString, mapper, univId);
        }
        
        catch (Exception ex) {
        	LOG.info("BudgetConstructionBudgetRevExpExportDaoJdbc Exception: " + ex.getMessage());
        	return null;
        }
    }
    
    public class BREExportData
    {
    	private String fdocNbr;
        private String univFiscalYear;
        private String finCoaCd;
        private String accountNbr;
        private String rptsToOrgCd;
        private String subAcctNbr;
        private String finObjectCd;
        private String finSubObjCd;
    	private String finBalanceTypCd;
    	private String finObjTypCd;
    	private String aclnAnnlBalAmt;
    	private String finBegBalLnAmt;
        private String rcCd;

        /**
         * Default constructor.
         */
        public BREExportData() {
            super();
        }
		public String getFdocNbr() {
			return fdocNbr;
		}
		public void setFdocNbr(String fDOC_NBR) {
			fdocNbr = fDOC_NBR;
		}
		public String getUnivFiscalYear() {
			return univFiscalYear;
		}
		public void setUnivFiscalYear(String univFiscalYear) {
			this.univFiscalYear = univFiscalYear;
		}
		public String getFinCoaCd() {
			return finCoaCd;
		}
		public void setFinCoaCd(String finCoaCd) {
			this.finCoaCd = finCoaCd;
		}
		public String getAccountNbr() {
			return accountNbr;
		}
		public void setAccountNbr(String accountNbr) {
			this.accountNbr = accountNbr;
		}
		public String getRptsToOrgCd() {
			return rptsToOrgCd;
		}
		public void setRptsToOrgCd(String rptsToOrgCd) {
			this.rptsToOrgCd = rptsToOrgCd;
		}
		public String getSubAcctNbr() {
			return subAcctNbr;
		}
		public void setSubAcctNbr(String subAcctNbr) {
			this.subAcctNbr = subAcctNbr;
		}
		public String getFinObjectCd() {
			return finObjectCd;
		}
		public void setFinObjectCd(String finObjectCd) {
			this.finObjectCd = finObjectCd;
		}
		public String getFinSubObjCd() {
			return finSubObjCd;
		}
		public void setFinSubObjCd(String finSubObjCd) {
			this.finSubObjCd = finSubObjCd;
		}
		public String getFinBalanceTypCd() {
			return finBalanceTypCd;
		}
		public void setFinBalanceTypCd(String finBalanceTypCd) {
			this.finBalanceTypCd = finBalanceTypCd;
		}
		public String getFinObjTypCd() {
			return finObjTypCd;
		}
		public void setFinObjTypCd(String finObjTypCd) {
			this.finObjTypCd = finObjTypCd;
		}
		public String getAclnAnnlBalAmt() {
			return aclnAnnlBalAmt;
		}
		public void setAclnAnnlBalAmt(String aclnAnnlBalAmt) {
			this.aclnAnnlBalAmt = aclnAnnlBalAmt;
		}
		public String getFinBegBalLnAmt() {
			return finBegBalLnAmt;
		}
		public void setFinBegBalLnAmt(String finBegBalLnAmt) {
			this.finBegBalLnAmt = finBegBalLnAmt;
		}
		public String getRcCd() {
			return rcCd;
		}
		public void setRcCd(String rcCd) {
			this.rcCd = rcCd;
		}
    }
}
