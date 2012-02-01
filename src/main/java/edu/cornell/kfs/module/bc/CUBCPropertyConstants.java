package edu.cornell.kfs.module.bc;

import org.kuali.kfs.module.bc.BCPropertyConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAccountReports;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAdministrativePost;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAppointmentFundingReason;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionIntendedIncumbent;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionOrganizationReports;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPosition;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionAppointmentFunding;

public class CUBCPropertyConstants extends BCPropertyConstants {

    public static final class PendingBudgetConstructionGeneralLedgerProperties {

        public static final String DOC_NBR = "documentNumber";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJECT_CODE = "financialSubObjectCode";
        public static final String FINANCIAL_BALANCE_TYP_CD = "financialBalanceTypeCode";
        public static final String FINANCIAL_OBJ_TYP_CD = "financialObjectTypeCode";
        public static final String ACC_LINE_ANN_BAL_AMT = "accountLineAnnualBalanceAmount";
        public static final String FIN_BEG_BAL_LINE_AMT = "financialBeginningBalanceLineAmount";
    }

    public static final class PendingBudgetConstructionAppointmentFundingProperties {
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJECT_CODE = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String APPT_FUND_DURATION_CD = "appointmentFundingDurationCode";
        public static final String APPT_REQ_CSF_AMT = "appointmentRequestedCsfAmount";
        public static final String APPT_REQ_CSF_FTE_QUANTITY = "appointmentRequestedCsfFteQuantity";
        public static final String APPT_TOTAL_INTENDED_AMT = "appointmentTotalIntendedAmount";
        public static final String APPT_TOTAL_INTENDED_FTE_QUANTITY = "appointmentTotalIntendedFteQuantity";
        public static final String APPT_REQ_AMT = "appointmentRequestedAmount";
        public static final String APPT_REQ_TIME_PERCENT = "appointmentRequestedTimePercent";
        public static final String APPT_REQ_FTE_QUANTITY = "appointmentRequestedFteQuantity";
        public static final String APPT_REQ_PAY_RATE = "appointmentRequestedPayRate";
        public static final String APPT_FUNDING_DELETE_IND = "appointmentFundingDeleteIndicator";
        public static final String APPT_FUNDING_MONTH = "appointmentFundingMonth";
    }

    public static final class BudgetConstructionPositionProperties {
        public static final String POSITION_DESC = "positionDescription";
        public static final String SETID_SALARY = "setidSalary";
        public static final String POSITION_SALARY_PLAN_DEFAULT = "positionSalaryPlanDefault";
        public static final String POSITION_GRADE_DEFAULT = "positionGradeDefault";
        public static final String NORMAL_WORK_MONTHS = "iuNormalWorkMonths";
        public static final String PAY_MONTHS = "iuPayMonths";
    }
    
    public static final class BudgetConstructionAccountReportsProperties {
        public static final String REPORTS_TO_ORG_CD = "reportsToOrganizationCode";
    }

    public static class BudgetConstructionIntendedIncumbentProperties {
        public static final String NAME = "name";
        public static final String CLASSIFICATION_LEVEL = "iuClassificationLevel";
    }

    public static final class BudgetConstructionAdministrativePostProperties {
        public static final String ADMINISTRATIVE_POST = "administrativePost";
    }

    public static final class BudgetConstructionCalculatedSalaryFoundationTrackerProperties {
        public static final String CSF_AMT = "csfAmount";
        public static final String CSF_FULL_TIME_EMPL_QUANTITY = "csfFullTimeEmploymentQuantity";
        public static final String CSF_TIME_PERCENT = "csfTimePercent";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_OF_ACCOUNTS = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FIN_OBJ_CD = "financialObjectCode";
        public static final String FIN_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String CSF_FUNDING_STATUS_CD = "csfFundingStatusCode";
    }

    public static final class BudgetConstructionAppointmentFundingReasonProperties {
        public static final String APPT_FUNDING_REASON_CD = "appointmentFundingReasonCode";
    }

    public static final class BudgetConstructionOrganizationReportsProperties {
        public static final String RESP_CENTER_CD = "responsibilityCenterCode";
    }

    public static final class BudgetConstructionMonthlyProperties {
        public static final String DOC_NBR = "documentNumber";
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_CD = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FINANCIAL_OBJ_CD = "financialObjectCode";
        public static final String FINANCIAL_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String FINANCIAL_BAL_TYP_CD = "financialBalanceTypeCode";
        public static final String FINANCIAL_OBJ_TYP_CD = "financialBalanceTypeCode";

        public static final String FINANCIAL_DOC_MONTH_1_LINE_AMT = "financialDocumentMonth1LineAmount";
        public static final String FINANCIAL_DOC_MONTH_2_LINE_AMT = "financialDocumentMonth2LineAmount";
        public static final String FINANCIAL_DOC_MONTH_3_LINE_AMT = "financialDocumentMonth3LineAmount";
        public static final String FINANCIAL_DOC_MONTH_4_LINE_AMT = "financialDocumentMonth4LineAmount";
        public static final String FINANCIAL_DOC_MONTH_5_LINE_AMT = "financialDocumentMonth5LineAmount";
        public static final String FINANCIAL_DOC_MONTH_6_LINE_AMT = "financialDocumentMonth6LineAmount";
        public static final String FINANCIAL_DOC_MONTH_7_LINE_AMT = "financialDocumentMonth7LineAmount";
        public static final String FINANCIAL_DOC_MONTH_8_LINE_AMT = "financialDocumentMonth8LineAmount";
        public static final String FINANCIAL_DOC_MONTH_9_LINE_AMT = "financialDocumentMonth9LineAmount";
        public static final String FINANCIAL_DOC_MONTH_10_LINE_AMT = "financialDocumentMonth10LineAmount";
        public static final String FINANCIAL_DOC_MONTH_11_LINE_AMT = "financialDocumentMonth11LineAmount";
        public static final String FINANCIAL_DOC_MONTH_12_LINE_AMT = "financialDocumentMonth12LineAmount";
    }

    public static final class CalculateSalaryFoundationTrackerProperties {
        public static final String UNIVERSITY_FISCAL_YEAR = "universityFiscalYear";
        public static final String CHART_OF_ACCOUNTS = "chartOfAccountsCode";
        public static final String ACCOUNT_NBR = "accountNumber";
        public static final String SUB_ACCOUNT_NBR = "subAccountNumber";
        public static final String FIN_OBJ_CD = "financialObjectCode";
        public static final String FIN_SUB_OBJ_CD = "financialSubObjectCode";
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String CREATE_TIMESTAMP = "csfCreateTimestamp";
    }

    public static final class PSPositionInfoProperties {
        public static final String POSITION_NBR = "positionNumber";
    }

    public static final class PSJobDataProperties {
        public static final String POSITION_NBR = "positionNumber";
        public static final String EMPLID = "emplid";
        public static final String EMPL_TYP = "employeeType";
    }

    public static final class PSJobCodeProperties {
        public static final String JOB_CD = "jobCode";
    }
    
    // Required since column headers for just this export need to have underscores and no spaces.
    public static final class BudgetConstructionFundingExportProperties {
        public static final String UNIVERSITY_FISCAL_YEAR = "University_Fiscal_Year";
        public static final String CHART_CD = "Chart";
        public static final String ACCOUNT_NBR = "Account_Number";
        public static final String REPORTS_TO_ORG_CD = "Reports_To_Organization";
        public static final String SUB_ACCOUNT_NBR = "Sub_Account_Number";
        public static final String FINANCIAL_OBJECT_CODE = "Financial_Object_Code";
        public static final String FINANCIAL_SUB_OBJECT_CODE = "Financial_Sub_Object_Code";
        public static final String POSITION_NBR = "Position_Number";
        public static final String POSITION_DESC = "Position_Description";
        public static final String SETID_SALARY = "Salary_Set_Id";
        public static final String POSITION_SALARY_PLAN_DEFAULT = "Position_Salary_Plan_Default";
        public static final String POSITION_GRADE_DEFAULT = "Position_Salary_Grade_Default";
        public static final String NORMAL_WORK_MONTHS = "Normal_Work_Months";
        public static final String PAY_MONTHS = "Pay_Months";
        public static final String EMPLID = "Emplid";
        public static final String NAME = "Person_Name";
        public static final String CLASSIFICATION_LEVEL = "Classification_Level";
        public static final String ADMINISTRATIVE_POST = "Administrative_Post";
        public static final String CSF_AMT = "CSF_Amount";
        public static final String CSF_FULL_TIME_EMPL_QUANTITY = "Csf_Full_Time_Employment_Quantity";
        public static final String CSF_TIME_PERCENT = "Csf_Time_Percent";
        public static final String APPT_FUND_DURATION_CD = "Appointment_Funding_Duration_Code";
        public static final String APPT_FUND_DURATION_DESC = "Appointment_Funding_Duration_Description";
        public static final String APPT_REQ_CSF_AMT = "Appointment_Requested_Csf_Amount";
        public static final String APPT_REQ_CSF_FTE_QUANTITY = "Appointment_Requested_Csf_Fte_Quantity";
        public static final String APPT_REQ_CSF_TIME_PERCENT = "Appointment_Csf_Requested_Time_Percent";
        public static final String APPT_TOTAL_INTENDED_AMT = "Appointment_Total_Intended_Amount";
        public static final String APPT_TOTAL_INTENDED_FTE_QUANTITY = "Appointment_Total_Intended_Fte_Quantity";
        public static final String APPT_REQ_AMT = "Appointment Requested_Amount";
        public static final String APPT_REQ_TIME_PERCENT = "Appointment_Requested_Time_Percent";
        public static final String APPT_REQ_FTE_QUANTITY = "Appointment_Requested_Fte_Quantity";
        public static final String APPT_REQ_PAY_RATE = "Appointment_Requested_Pay_Rate";
        public static final String APPT_FUNDING_DELETE_IND = "Delete_Indicator";
        public static final String APPT_FUNDING_MONTH = "Appointment_Funding_Month";
        public static final String APPT_FUNDING_REASON_CD = "Appointment_Funding_Reason_Code";
        public static final String RESP_CENTER_CD = "Responsibility_Center_Code";
        public static final String SUB_FUND_GRP_CD = "Sub_Fund_Group_Code";
        public static final String PROGRAM_CD = "Sub_Fund_Program_Group_Code";
    }
    
    public static final class BudgetConstructionSIPExportProperties {
        public static final String UNITID = "UnitID";
        public static final String HRDEPT = "HR_Dept_ID";
        public static final String DEPTID = "KFS_Dept_ID";
        public static final String DEPT_NAME = "Department_Name";
        public static final String POSITION_NBR = "Position_Nbr";
        public static final String POSITION_DESC = "Position_Description";
        public static final String EMPLID = "EmplID";
        public static final String PERSON_NAME = "Person_Name";
        public static final String SIP_ELIGIBILITY = "SIP_Eligible";
        public static final String EMPLOYEE_TYPE = "SIP_Employee_Type";
        public static final String EMPL_RCD = "Employee_RCD";
        public static final String JOBCODE = "Job_Code";
        public static final String JOBCODE_SHORT_DESC = "Job_Code_Short_Desc";
        public static final String JOB_FAMILY = "Job_Family";
        public static final String CU_PLANNED_FTE = "CU_Planned_FTE";
        public static final String POS_GRADE_DFLT = "Grade";
        public static final String CU_STATE_CERT = "CU_State_Cert";
        public static final String COMP_FREQ = "Comp_Frequency";
        public static final String ANNL_RT = "Annual_Rate";
        public static final String COMP_RT = "Comp_Rate";
        public static final String JOB_STD_HRS = "Job_Standard_Hours";
        public static final String WORK_MONTHS = "Work_Months";
        public static final String JOB_FUNC = "Job_Function";
        public static final String JOB_FUNC_DESC = "Job_Function_Description";
        public static final String INCREASE_TO_MINIMUM = "Increase_to_Minimum";
        public static final String EQUITY = "Equity";
        public static final String MERIT = "Merit";
        public static final String NOTE = "Note";
        public static final String DEFERRED = "Deferred";
        public static final String CU_ABBR_FLAG = "CU_ABBR_Flag";
        public static final String APPT_TOT_INTND_AMT = "TOTAL_Job_Planned_Commit";
        public static final String APPT_RQST_FTE_QTY = "TOTAL_Job_Planned_FTE";
//        public static final String LEAVE_CODE = "Leave Code";
//        public static final String LEAVE_DESC = "Leave Description";
//        public static final String LEAVE_AMT = "Leave Amount";
        public static final String BGP_FLSA = "BGP FLSA";
    }

}
