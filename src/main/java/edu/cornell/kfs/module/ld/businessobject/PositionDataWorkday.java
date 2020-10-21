package edu.cornell.kfs.module.ld.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import java.math.BigDecimal;
import java.sql.Date;

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
}
