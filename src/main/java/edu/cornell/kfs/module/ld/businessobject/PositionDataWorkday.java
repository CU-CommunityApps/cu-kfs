package edu.cornell.kfs.module.ld.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.ld.CuLaborPropertyConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class PositionDataWorkday extends PersistableBusinessObjectBase {

    private String positionNumber;
    private String jobCode;
    private Date effectiveDate;
    private String positionEffectiveStatus;
    private String description;
    private String shortDescription;
    private String businessUnit;
    private String departmentId;
    private String positionStatus;
    private Date statusDate;
    private String budgetedPosition;
    private BigDecimal standardHoursDefault;
    private String standardHoursFrequency;
    private String positionRegularTemporary;
    private BigDecimal positionFullTimeEquivalency;
    private String positionSalaryPlanDefault;
    private String positionGradeDefault;
    private Date inactivationDate;

    public PositionDataWorkday() {
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_NUMBER).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionNumber).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.JOB_CODE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(jobCode).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.EFFECTIVE_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(effectiveDate)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_EFFECTIVE_STATUS).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionEffectiveStatus).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.DESCRIPTION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(description).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.SHORT_DESCRIPTION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(shortDescription).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.BUSINESS_UNIT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(businessUnit).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.DEPARTMENT_ID).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(departmentId).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_STATUS).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionStatus).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.STATUS_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(statusDate)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.BUDGETED_POSITION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(budgetedPosition).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.STANDARD_HOURS_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(standardHoursDefault.toPlainString()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.STANDARD_HOURS_FREQUENCY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(standardHoursFrequency).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_REGULAR_TEMPORARY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionRegularTemporary).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_FULL_TIME_EQUIVALENCY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionFullTimeEquivalency.toPlainString()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_SALARY_PLAN_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionSalaryPlanDefault).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.POSITION_GRADE_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionGradeDefault).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionDataWorkday.INACTIVATION_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(inactivationDate)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        
        return sb.toString();
    }
}
