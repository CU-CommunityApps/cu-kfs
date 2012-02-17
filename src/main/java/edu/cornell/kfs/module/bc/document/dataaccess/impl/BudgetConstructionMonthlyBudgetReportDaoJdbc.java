package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import edu.cornell.kfs.module.bc.businessobject.MonthlyBudgetReportLine;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionMonthlyBudgetReportDao;

import edu.cornell.kfs.module.bc.util.ExportUtil;

public class BudgetConstructionMonthlyBudgetReportDaoJdbc extends BudgetConstructionDaoJdbcBase implements
        BudgetConstructionMonthlyBudgetReportDao {

	private ExportUtil exportUtil = new ExportUtil();
	
    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionMonthlyBudgetReportDao#getMonthlyBudgetReportLines(java.lang.String)
     */
    public Collection<MonthlyBudgetReportLine> getMonthlyBudgetReportLines(String universalId) {

        Collection<MonthlyBudgetReportLine> resultMonthlyReportLines = new ArrayList<MonthlyBudgetReportLine>();

        StringBuffer sqlText = new StringBuffer();

        sqlText.append("SELECT bcmonth.fdoc_nbr, \n");
        sqlText.append("  bcmonth.univ_fiscal_yr, \n");
        sqlText.append("  bcmonth.fin_coa_cd, \n");
        sqlText.append("  bcmonth.account_nbr, \n");
        sqlText.append("  acctrpts.rpts_to_org_cd, \n");
        sqlText.append("  bcmonth.sub_acct_nbr, \n");
        sqlText.append("  bcmonth.fin_object_cd, \n");
        sqlText.append("  bcmonth.fin_sub_obj_cd, \n");
        sqlText.append("  bcmonth.fin_balance_typ_cd, \n");
        sqlText.append("  bcmonth.fin_obj_typ_cd, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo1_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo2_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo3_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo4_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo5_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo6_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo7_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo8_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo9_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo10_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo11_amt, \n");
        sqlText.append("  bcmonth.fdoc_ln_mo12_amt, \n");
        sqlText.append("  orgrpts.rc_cd \n");
        sqlText.append("FROM \n");
        sqlText.append("  (SELECT ctrl.univ_fiscal_yr AS univ_fiscal_yr, \n");
        sqlText.append("    ctrl.fin_coa_cd           AS fin_coa_cd, \n");
        sqlText.append("    ctrl.account_nbr          AS account_nbr, \n");
        sqlText.append("    ctrl.sub_acct_nbr sub_acct_nbr, \n");
        sqlText.append("    ctrl.ver_nbr \n");
        sqlText.append("  FROM ld_bcn_ctrl_list_t ctrl, \n");
        sqlText.append("    ld_bcn_subfund_pick_t pick \n");
        sqlText.append("  WHERE ctrl.person_unvl_id = ? \n");
        sqlText.append("  AND pick.person_unvl_id   = ctrl.person_unvl_id \n");
        sqlText.append("  AND pick.sub_fund_grp_cd  = ctrl.sel_sub_fund_grp \n");
        sqlText.append("  AND pick.report_flag      > 0 \n");
        sqlText.append("  ) acctdump, \n");
        sqlText.append("  LD_BCNSTR_MONTH_T bcmonth, \n");
        sqlText.append("  LD_BCN_ACCT_RPTS_T acctrpts , \n");
        sqlText.append("  LD_BCN_ORG_RPTS_T orgrpts \n");
        sqlText.append("WHERE acctdump.univ_fiscal_yr   = bcmonth.univ_fiscal_yr \n");
        sqlText.append("AND acctdump.fin_coa_cd         = bcmonth.fin_coa_cd \n");
        sqlText.append("AND acctdump.account_nbr        = bcmonth.account_nbr \n");
        sqlText.append("AND acctdump.sub_acct_nbr       = bcmonth.sub_acct_nbr \n");
        sqlText.append("AND bcmonth.fin_coa_cd          = acctrpts.fin_coa_cd \n");
        sqlText.append("AND bcmonth.account_nbr         = acctrpts.account_nbr \n");
        sqlText.append("AND acctrpts.rpts_to_fin_coa_cd = orgrpts.fin_coa_cd \n");
        sqlText.append("AND acctrpts.rpts_to_org_cd     = orgrpts.org_cd \n");
        sqlText.append("ORDER BY bcmonth.account_nbr, bcmonth.fin_object_cd, \n");
        sqlText.append("  bcmonth.fin_sub_obj_cd");

        String sqlString = sqlText.toString();

        ParameterizedRowMapper<MonthlyBudgetReportLine> mapper = new ParameterizedRowMapper<MonthlyBudgetReportLine>() {
            public MonthlyBudgetReportLine mapRow(ResultSet rs, int rowNum) throws SQLException {
                MonthlyBudgetReportLine monthlyBudgetReportLine = new MonthlyBudgetReportLine();
                monthlyBudgetReportLine.setDocNumber(exportUtil.removeNulls(rs.getString("fdoc_nbr"), false));
                monthlyBudgetReportLine.setUniversityFiscalYear(rs.getInt("univ_fiscal_yr"));
                monthlyBudgetReportLine.setChartCode(exportUtil.removeNulls(rs.getString("fin_coa_cd"), false));
                monthlyBudgetReportLine.setAccountNumber(exportUtil.removeNulls(rs.getString("account_nbr"), false));
                monthlyBudgetReportLine.setReportsToOrgCode(exportUtil.removeNulls(rs.getString("rpts_to_org_cd"), false));
                monthlyBudgetReportLine.setSubAccountNumber(exportUtil.removeNulls(rs.getString("sub_acct_nbr"), false));
                monthlyBudgetReportLine.setFinancialObjectCode(exportUtil.removeNulls(rs.getString("fin_object_cd"), false));
                monthlyBudgetReportLine.setFinancialSubObjectCode(exportUtil.removeNulls(rs.getString("fin_sub_obj_cd"), false));
                monthlyBudgetReportLine.setFinancialBalanceType(exportUtil.removeNulls(rs.getString("fin_balance_typ_cd"), false));
                monthlyBudgetReportLine.setFinancialObjectTypeCode(exportUtil.removeNulls(rs.getString("fin_obj_typ_cd"), false));
                monthlyBudgetReportLine.setFinancialDocMonth1LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo1_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth2LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo2_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth3LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo3_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth4LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo4_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth5LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo5_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth6LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo6_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth7LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo7_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth8LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo8_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth9LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo9_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth10LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo10_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth11LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo11_amt"), true));
                monthlyBudgetReportLine.setFinancialDocMonth12LineAmt(exportUtil.removeNulls(rs.getString("fdoc_ln_mo12_amt"), true));
                monthlyBudgetReportLine.setReponsibilityCenterCode(exportUtil.removeNulls(rs.getString("rc_cd"), false));

                return monthlyBudgetReportLine;
            }
        };

        resultMonthlyReportLines = this.getSimpleJdbcTemplate().query(
                sqlString, mapper, universalId);

        return resultMonthlyReportLines;
    }

}
