package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionBudgetedSalaryLineExportDao;

public class BudgetConstructionBudgetedSalaryLineExportDaoJdbc 
			extends BudgetConstructionDaoJdbcBase 
			implements BudgetConstructionBudgetedSalaryLineExportDao {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BudgetConstructionBudgetedSalaryLineExportDaoJdbc.class);
	
	public BudgetConstructionBudgetedSalaryLineExportDaoJdbc() {
		
	}

    public Collection<BSLExportData> getBSLExtractByPersonUnivId(String univId) {
       
        try {
        	// Build the SQL
	        StringBuilder sqlBuilder = new StringBuilder(3500);

	        sqlBuilder.append("SELECT LD_PNDBC_APPTFND_T.UNIV_FISCAL_YR,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.FIN_COA_CD,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.ACCOUNT_NBR,  ");
	        sqlBuilder.append("  LD_BCN_ACCT_RPTS_T.RPTS_TO_ORG_CD,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.SUB_ACCT_NBR,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.FIN_OBJECT_CD,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.FIN_SUB_OBJ_CD,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.POSITION_NBR,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.POS_DESCR,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.SETID_SALARY,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.POS_SAL_PLAN_DFLT,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.POS_GRADE_DFLT,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.IU_NORM_WORK_MONTHS,  ");
	        sqlBuilder.append("  LD_BCN_POS_T.IU_PAY_MONTHS,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.EMPLID,  ");
	        sqlBuilder.append("  LD_BCN_INTINCBNT_T.PERSON_NM,  ");
	        sqlBuilder.append("  LD_BCN_INTINCBNT_T.IU_CLASSIF_LEVEL,  ");
	        sqlBuilder.append("  LD_BCN_ADM_POST_T.ADMIN_POST,  ");
	        sqlBuilder.append("  LD_BCN_CSF_TRCKR_T.POS_CSF_AMT,  ");
	        sqlBuilder.append("  LD_BCN_CSF_TRCKR_T.POS_CSF_FTE_QTY,  ");
	        sqlBuilder.append("  LD_BCN_CSF_TRCKR_T.POS_CSF_TM_PCT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_FND_DUR_CD,  ");
	        sqlBuilder.append("  LD_BCN_DURATION_T.APPT_DUR_DESC,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQST_CSF_AMT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQCSF_FTE_QTY,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQCSF_TM_PCT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_TOT_INTND_AMT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_TOTINTFTE_QTY,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQST_AMT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQST_TM_PCT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQST_FTE_QTY,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_RQST_PAY_RT,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_FND_DLT_CD,  ");
	        sqlBuilder.append("  LD_PNDBC_APPTFND_T.APPT_FND_MO,  ");
	        sqlBuilder.append("  LD_BCN_AF_REASON_T.APPT_FND_REASON_CD,  ");
	        sqlBuilder.append("  CA_ACCOUNT_T.SUB_FUND_GRP_CD,  ");
	        sqlBuilder.append("  LD_BCN_ORG_RPTS_T.RC_CD,  ");
	        sqlBuilder.append("  CA_ACCOUNT_TX.PROGRAM_CD  ");
	        sqlBuilder.append("FROM  ");
	        sqlBuilder.append("  (SELECT ctrl.UNIV_FISCAL_YR,  ");
	        sqlBuilder.append("    ctrl.fin_coa_cd,  ");
	        sqlBuilder.append("    ctrl.account_nbr,  ");
	        sqlBuilder.append("    ctrl.sub_acct_nbr,  ");
	        sqlBuilder.append("    ctrl.ver_nbr  ");
	        sqlBuilder.append("  FROM ld_bcn_ctrl_list_t ctrl,  ");
	        sqlBuilder.append("    ld_bcn_subfund_pick_t pick  ");
	        sqlBuilder.append("  WHERE ctrl.person_unvl_id = ?  ");
	        sqlBuilder.append("  AND pick.person_unvl_id   = ctrl.person_unvl_id  ");
	        sqlBuilder.append("  AND pick.sub_fund_grp_cd  = ctrl.sel_sub_fund_grp  ");
	        sqlBuilder.append("  AND pick.report_flag      > 0  ");
	        sqlBuilder.append("  ) USERSELECTED  ");
	        sqlBuilder.append("INNER JOIN LD_PNDBC_APPTFND_T  ");
	        sqlBuilder.append("ON LD_PNDBC_APPTFND_T.FIN_COA_CD     = USERSELECTED.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_PNDBC_APPTFND_T.ACCOUNT_NBR   = USERSELECTED.ACCOUNT_NBR  ");
	        sqlBuilder.append("AND LD_PNDBC_APPTFND_T.UNIV_FISCAL_YR=USERSELECTED.UNIV_FISCAL_YR  ");
	        sqlBuilder.append("AND LD_PNDBC_APPTFND_T.SUB_ACCT_NBR  =USERSELECTED.SUB_ACCT_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_POS_T  ");
	        sqlBuilder.append("ON LD_BCN_POS_T.UNIV_FISCAL_YR=LD_PNDBC_APPTFND_T.UNIV_FISCAL_YR  ");
	        sqlBuilder.append("AND LD_BCN_POS_T.POSITION_NBR =LD_PNDBC_APPTFND_T.POSITION_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_INTINCBNT_T  ");
	        sqlBuilder.append("ON LD_BCN_INTINCBNT_T.emplid=LD_PNDBC_APPTFND_T.emplid  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_ADM_POST_T  ");
	        sqlBuilder.append("ON LD_BCN_ADM_POST_T.position_nbr = LD_PNDBC_APPTFND_T.position_nbr  ");
	        sqlBuilder.append("AND LD_BCN_ADM_POST_T.emplid      = LD_PNDBC_APPTFND_T.emplid  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_CSF_TRCKR_T  ");
	        sqlBuilder.append("ON LD_BCN_CSF_TRCKR_T.UNIV_FISCAL_YR  = USERSELECTED.UNIV_FISCAL_YR  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.FIN_COA_CD     = LD_PNDBC_APPTFND_T.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.ACCOUNT_NBR    = LD_PNDBC_APPTFND_T.ACCOUNT_NBR  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.SUB_ACCT_NBR   = LD_PNDBC_APPTFND_T.SUB_ACCT_NBR  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.FIN_OBJECT_CD  = LD_PNDBC_APPTFND_T.FIN_OBJECT_CD  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.FIN_SUB_OBJ_CD = LD_PNDBC_APPTFND_T.FIN_SUB_OBJ_CD  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.POSITION_NBR   = LD_PNDBC_APPTFND_T.POSITION_NBR  ");
	        sqlBuilder.append("AND LD_BCN_CSF_TRCKR_T.EMPLID         = LD_PNDBC_APPTFND_T.EMPLID  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_ACCT_RPTS_T  ");
	        sqlBuilder.append("ON LD_BCN_ACCT_RPTS_T.FIN_COA_CD   = LD_PNDBC_APPTFND_T.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_ACCT_RPTS_T.ACCOUNT_NBR = LD_PNDBC_APPTFND_T.ACCOUNT_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_DURATION_T  ");
	        sqlBuilder.append("ON LD_BCN_DURATION_T.APPT_DUR_CD = LD_PNDBC_APPTFND_T.APPT_FND_DUR_CD  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_AF_REASON_T  ");
	        sqlBuilder.append("ON LD_BCN_AF_REASON_T.UNIV_FISCAL_YR  = LD_PNDBC_APPTFND_T.UNIV_FISCAL_YR  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.FIN_COA_CD     = LD_PNDBC_APPTFND_T.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.ACCOUNT_NBR    = LD_PNDBC_APPTFND_T.ACCOUNT_NBR  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.SUB_ACCT_NBR   = LD_PNDBC_APPTFND_T.SUB_ACCT_NBR  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.FIN_OBJECT_CD  = LD_PNDBC_APPTFND_T.FIN_OBJECT_CD  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.FIN_SUB_OBJ_CD = LD_PNDBC_APPTFND_T.FIN_SUB_OBJ_CD  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.POSITION_NBR   = LD_PNDBC_APPTFND_T.POSITION_NBR  ");
	        sqlBuilder.append("AND LD_BCN_AF_REASON_T.EMPLID         = LD_PNDBC_APPTFND_T.EMPLID  ");
	        sqlBuilder.append("LEFT JOIN CA_ACCOUNT_T  ");
	        sqlBuilder.append("ON CA_ACCOUNT_T.FIN_COA_CD  =USERSELECTED.FIN_COA_CD  ");
	        sqlBuilder.append("AND CA_ACCOUNT_T.ACCOUNT_NBR=USERSELECTED.ACCOUNT_NBR  ");
	        sqlBuilder.append("LEFT JOIN CA_ACCOUNT_TX  ");
	        sqlBuilder.append("ON CA_ACCOUNT_TX.FIN_COA_CD  =CA_ACCOUNT_T.FIN_COA_CD  ");
	        sqlBuilder.append("AND CA_ACCOUNT_TX.ACCOUNT_NBR=CA_ACCOUNT_T.ACCOUNT_NBR  ");
	        sqlBuilder.append("LEFT JOIN LD_BCN_ORG_RPTS_T  ");
	        sqlBuilder.append("ON LD_BCN_ORG_RPTS_T.FIN_COA_CD=CA_ACCOUNT_T.FIN_COA_CD  ");
	        sqlBuilder.append("AND LD_BCN_ORG_RPTS_T.ORG_CD   =CA_ACCOUNT_T.ORG_CD  ");
	        sqlBuilder.append("WHERE LD_BCN_POS_T.POS_EFFDT   =  ");
	        sqlBuilder.append("  (SELECT MAX(LD_BCN_POS_T_2.POS_EFFDT)  ");
	        sqlBuilder.append("  FROM LD_BCN_POS_T LD_BCN_POS_T_2  ");
	        sqlBuilder.append("  WHERE LD_BCN_POS_T_2.position_nbr=LD_BCN_POS_T.position_nbr  ");
	        sqlBuilder.append("  )  ");
	        sqlBuilder.append("ORDER BY LD_PNDBC_APPTFND_T.position_nbr");

	        String sqlString = sqlBuilder.toString();

            // Get the BSL data from the data base, map it and build a result set to be returned to the user.
        
	        ParameterizedRowMapper<BSLExportData> mapper = new ParameterizedRowMapper<BSLExportData>() {
	            public BSLExportData mapRow(ResultSet rs, int rowNum) throws SQLException {
	            	BSLExportData bslExportData = new BSLExportData();
	                bslExportData.setUnivFiscalYear(rs.getString("UNIV_FISCAL_YR"));
	                bslExportData.setFinCoaCd(rs.getString("FIN_COA_CD"));
	                bslExportData.setAccountNbr(rs.getString("ACCOUNT_NBR"));
	                bslExportData.setRptsToOrgCd(rs.getString("RPTS_TO_ORG_CD"));
	                bslExportData.setSubAcctNbr(rs.getString("SUB_ACCT_NBR"));
	                bslExportData.setFinObjectCd(rs.getString("FIN_OBJECT_CD"));
	                bslExportData.setFinSubObjCd(rs.getString("FIN_SUB_OBJ_CD"));
	                bslExportData.setPositionNbr(rs.getString("POSITION_NBR"));
	                bslExportData.setPosDescr(rs.getString("POS_DESCR"));
	                bslExportData.setSetidSalary(rs.getString("SETID_SALARY"));
	                bslExportData.setPosSalPlanDflt(rs.getString("POS_SAL_PLAN_DFLT"));
	                bslExportData.setPosGradeDflt(rs.getString("POS_GRADE_DFLT"));
	                bslExportData.setIuNormWorkMonths(rs.getString("IU_NORM_WORK_MONTHS"));
	                bslExportData.setIuPayMonths(rs.getString("IU_PAY_MONTHS"));
	                bslExportData.setEmplId(rs.getString("EMPLID"));
	                bslExportData.setPersonNm(rs.getString("PERSON_NM"));
	                bslExportData.setIuClassifLevel(rs.getString("IU_CLASSIF_LEVEL"));
	                bslExportData.setAdminPost(rs.getString("ADMIN_POST"));
	                bslExportData.setPosCsfAmt(rs.getString("POS_CSF_AMT"));
	                bslExportData.setPosCsfFteQty(rs.getString("POS_CSF_FTE_QTY"));
	                bslExportData.setPosCsfTmPct(rs.getString("POS_CSF_TM_PCT"));
	                bslExportData.setApptFndDurCd(rs.getString("APPT_FND_DUR_CD"));
	                bslExportData.setApptDurDesc(rs.getString("APPT_DUR_DESC"));
	                bslExportData.setApptRqstCsfAmt(rs.getString("APPT_RQST_CSF_AMT"));
	                bslExportData.setApptRqcsfFteQty(rs.getString("APPT_RQCSF_FTE_QTY"));
	                bslExportData.setApptRqcsfTmPct(rs.getString("APPT_RQCSF_TM_PCT"));
	                bslExportData.setApptTotIntndAmt(rs.getString("APPT_TOT_INTND_AMT"));
	                bslExportData.setApptTotintfteQty(rs.getString("APPT_TOTINTFTE_QTY"));
	                bslExportData.setApptRqstAmt(rs.getString("APPT_RQST_AMT"));
	                bslExportData.setApptRqstTmPct(rs.getString("APPT_RQST_TM_PCT"));
	                bslExportData.setApptRqstFteQty(rs.getString("APPT_RQST_FTE_QTY"));
	                bslExportData.setApptRqstPayRt(rs.getString("APPT_RQST_PAY_RT"));
	                bslExportData.setApptFndDltCd(rs.getString("APPT_FND_DLT_CD"));
	                bslExportData.setApptFndMo(rs.getString("APPT_FND_MO"));
	                bslExportData.setApptFndReasonCd(rs.getString("APPT_FND_REASON_CD"));
	                bslExportData.setSubFundGrpCd(rs.getString("SUB_FUND_GRP_CD"));
	                bslExportData.setRcCd(rs.getString("RC_CD"));
	                bslExportData.setProgramCd(rs.getString("PROGRAM_CD"));

	                return bslExportData;
	            }
	        };
	        
	        return this.getSimpleJdbcTemplate().query(sqlString, mapper, univId);
        }
        
        catch (Exception ex) {
        	LOG.info("BudgetConstructionBudgetedSalaryLineExportDaoJdbc Exception: " + ex.getMessage());
        	return null;
        }
    }
    
    public class BSLExportData
    {
        private String univFiscalYear;
        private String finCoaCd;
        private String accountNbr;
        private String rptsToOrgCd;
        private String subAcctNbr;
        private String finObjectCd;
        private String finSubObjCd;
        private String positionNbr;
        private String posDescr;
        private String setidSalary;
        private String posSalPlanDflt;
        private String posGradeDflt;
        private String iuNormWorkMonths;
        private String iuPayMonths;
        private String emplId;
        private String personNm;
        private String iuClassifLevel;
        private String adminPost;
        private String posCsfAmt;
        private String posCsfFteQty;
        private String posCsfTmPct;
        private String apptFndDurCd;
        private String apptDurDesc;
        private String apptRqstCsfAmt;
        private String apptRqcsfFteQty;
        private String apptRqcsfTmPct;
        private String apptTotIntndAmt;
        private String apptTotintfteQty;
        private String apptRqstAmt;
        private String apptRqstTmPct;
        private String apptRqstFteQty;
        private String apptRqstPayRt;
        private String apptFndDltCd;
        private String apptFndMo;
        private String apptFndReasonCd;
        private String subFundGrpCd;
        private String rcCd;
        private String programCd;
        /**
         * Default constructor.
         */
        public BSLExportData() {
            super();
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
		public String getPositionNbr() {
			return positionNbr;
		}
		public void setPositionNbr(String positionNbr) {
			this.positionNbr = positionNbr;
		}
		public String getPosDescr() {
			return posDescr;
		}
		public void setPosDescr(String posDescr) {
			this.posDescr = posDescr;
		}
		public String getSetidSalary() {
			return setidSalary;
		}
		public void setSetidSalary(String setidSalary) {
			this.setidSalary = setidSalary;
		}
		public String getPosSalPlanDflt() {
			return posSalPlanDflt;
		}
		public void setPosSalPlanDflt(String posSalPlanDflt) {
			this.posSalPlanDflt = posSalPlanDflt;
		}
		public String getPosGradeDflt() {
			return posGradeDflt;
		}
		public void setPosGradeDflt(String posGradeDflt) {
			this.posGradeDflt = posGradeDflt;
		}
		public String getIuNormWorkMonths() {
			return iuNormWorkMonths;
		}
		public void setIuNormWorkMonths(String iuNormWorkMonths) {
			this.iuNormWorkMonths = iuNormWorkMonths;
		}
		public String getIuPayMonths() {
			return iuPayMonths;
		}
		public void setIuPayMonths(String iuPayMonths) {
			this.iuPayMonths = iuPayMonths;
		}
		public String getEmplId() {
			return emplId;
		}
		public void setEmplId(String emplId) {
			this.emplId = emplId;
		}
		public String getPersonNm() {
			return personNm;
		}
		public void setPersonNm(String personNm) {
			this.personNm = personNm;
		}
		public String getIuClassifLevel() {
			return iuClassifLevel;
		}
		public void setIuClassifLevel(String iuClassifLevel) {
			this.iuClassifLevel = iuClassifLevel;
		}
		public String getAdminPost() {
			return adminPost;
		}
		public void setAdminPost(String adminPost) {
			this.adminPost = adminPost;
		}
		public String getPosCsfAmt() {
			return posCsfAmt;
		}
		public void setPosCsfAmt(String posCsfAmt) {
			this.posCsfAmt = posCsfAmt;
		}
		public String getPosCsfFteQty() {
			return posCsfFteQty;
		}
		public void setPosCsfFteQty(String posCsfFteQty) {
			this.posCsfFteQty = posCsfFteQty;
		}
		public String getPosCsfTmPct() {
			return posCsfTmPct;
		}
		public void setPosCsfTmPct(String posCsfTmPct) {
			this.posCsfTmPct = posCsfTmPct;
		}
		public String getApptFndDurCd() {
			return apptFndDurCd;
		}
		public void setApptFndDurCd(String apptFndDurCd) {
			this.apptFndDurCd = apptFndDurCd;
		}
		public String getApptDurDesc() {
			return apptDurDesc;
		}
		public void setApptDurDesc(String apptDurDesc) {
			this.apptDurDesc = apptDurDesc;
		}
		public String getApptRqstCsfAmt() {
			return apptRqstCsfAmt;
		}
		public void setApptRqstCsfAmt(String apptRqstCsfAmt) {
			this.apptRqstCsfAmt = apptRqstCsfAmt;
		}
		public String getApptRqcsfFteQty() {
			return apptRqcsfFteQty;
		}
		public void setApptRqcsfFteQty(String apptRqcsfFteQty) {
			this.apptRqcsfFteQty = apptRqcsfFteQty;
		}
		public String getApptRqcsfTmPct() {
			return apptRqcsfTmPct;
		}
		public void setApptRqcsfTmPct(String apptRqcsfTmPct) {
			this.apptRqcsfTmPct = apptRqcsfTmPct;
		}
		public String getApptTotIntndAmt() {
			return apptTotIntndAmt;
		}
		public void setApptTotIntndAmt(String apptTotIntndAmt) {
			this.apptTotIntndAmt = apptTotIntndAmt;
		}
		public String getApptTotintfteQty() {
			return apptTotintfteQty;
		}
		public void setApptTotintfteQty(String apptTotintfteQty) {
			this.apptTotintfteQty = apptTotintfteQty;
		}
		public String getApptRqstAmt() {
			return apptRqstAmt;
		}
		public void setApptRqstAmt(String apptRqstAmt) {
			this.apptRqstAmt = apptRqstAmt;
		}
		public String getApptRqstTmPct() {
			return apptRqstTmPct;
		}
		public void setApptRqstTmPct(String apptRqstTmPct) {
			this.apptRqstTmPct = apptRqstTmPct;
		}
		public String getApptRqstFteQty() {
			return apptRqstFteQty;
		}
		public void setApptRqstFteQty(String apptRqstFteQty) {
			this.apptRqstFteQty = apptRqstFteQty;
		}
		public String getApptRqstPayRt() {
			return apptRqstPayRt;
		}
		public void setApptRqstPayRt(String apptRqstPayRt) {
			this.apptRqstPayRt = apptRqstPayRt;
		}
		public String getApptFndDltCd() {
			return apptFndDltCd;
		}
		public void setApptFndDltCd(String apptFndDltCd) {
			this.apptFndDltCd = apptFndDltCd;
		}
		public String getApptFndMo() {
			return apptFndMo;
		}
		public void setApptFndMo(String apptFndMo) {
			this.apptFndMo = apptFndMo;
		}
		public String getApptFndReasonCd() {
			return apptFndReasonCd;
		}
		public void setApptFndReasonCd(String apptFndReasonCd) {
			this.apptFndReasonCd = apptFndReasonCd;
		}
		public String getSubFundGrpCd() {
			return subFundGrpCd;
		}
		public void setSubFundGrpCd(String subFundGrpCd) {
			this.subFundGrpCd = subFundGrpCd;
		}
		public String getRcCd() {
			return rcCd;
		}
		public void setRcCd(String rcCd) {
			this.rcCd = rcCd;
		}
		public String getProgramCd() {
			return programCd;
		}
		public void setProgramCd(String programCd) {
			this.programCd = programCd;
		}
    }
}
