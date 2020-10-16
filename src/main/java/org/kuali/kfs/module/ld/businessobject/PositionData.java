/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.ld.businessobject;

import org.kuali.kfs.gl.Constant;
import org.kuali.kfs.gl.businessobject.TransientBalanceInquiryAttributes;
import org.kuali.kfs.integration.ld.LaborLedgerPositionData;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.ld.CuLaborPropertyConstants;
import edu.cornell.kfs.module.ld.businessobject.PositionDataExtentedAttribute;
import edu.cornell.kfs.sys.CUKFSConstants;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Labor business object for PositionData
 */
public class PositionData extends PersistableBusinessObjectBase implements LaborLedgerPositionData {

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
    private TransientBalanceInquiryAttributes dummyBusinessObject;

    public PositionData() {
        super();
        this.dummyBusinessObject = new TransientBalanceInquiryAttributes();
        this.dummyBusinessObject.setLinkButtonOption(Constant.LOOKUP_BUTTON_VALUE);
    }

    @Override
    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    @Override
    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    @Override
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override
    public String getPositionEffectiveStatus() {
        return positionEffectiveStatus;
    }

    public void setPositionEffectiveStatus(String positionEffectiveStatus) {
        this.positionEffectiveStatus = positionEffectiveStatus;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Override
    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    @Override
    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String getPositionStatus() {
        return positionStatus;
    }

    public void setPositionStatus(String positionStatus) {
        this.positionStatus = positionStatus;
    }

    @Override
    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    @Override
    public String getBudgetedPosition() {
        return budgetedPosition;
    }

    public void setBudgetedPosition(String budgetedPosition) {
        this.budgetedPosition = budgetedPosition;
    }

    public BigDecimal getStandardHoursDefault() {
        return standardHoursDefault;
    }

    public void setStandardHoursDefault(BigDecimal standardHoursDefault) {
        this.standardHoursDefault = standardHoursDefault;
    }

    @Override
    public String getStandardHoursFrequency() {
        return standardHoursFrequency;
    }

    public void setStandardHoursFrequency(String standardHoursFrequency) {
        this.standardHoursFrequency = standardHoursFrequency;
    }

    @Override
    public String getPositionRegularTemporary() {
        return positionRegularTemporary;
    }

    public void setPositionRegularTemporary(String positionRegularTemporary) {
        this.positionRegularTemporary = positionRegularTemporary;
    }

    @Override
    public BigDecimal getPositionFullTimeEquivalency() {
        return positionFullTimeEquivalency;
    }

    public void setPositionFullTimeEquivalency(BigDecimal positionFullTimeEquivalency) {
        this.positionFullTimeEquivalency = positionFullTimeEquivalency;
    }

    @Override
    public String getPositionSalaryPlanDefault() {
        return positionSalaryPlanDefault;
    }

    public void setPositionSalaryPlanDefault(String positionSalaryPlanDefault) {
        this.positionSalaryPlanDefault = positionSalaryPlanDefault;
    }

    @Override
    public String getPositionGradeDefault() {
        return positionGradeDefault;
    }

    public void setPositionGradeDefault(String positionGradeDefault) {
        this.positionGradeDefault = positionGradeDefault;
    }

    @Override
    public TransientBalanceInquiryAttributes getDummyBusinessObject() {
        return dummyBusinessObject;
    }

    public void setDummyBusinessObject(TransientBalanceInquiryAttributes dummyBusinessObject) {
        this.dummyBusinessObject = dummyBusinessObject;
    }
    
    //CUMod: Added missing toString function. Needed for table purge functionality.
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_NUMBER).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionNumber).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.JOB_CODE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(jobCode).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.EFFECTIVE_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(effectiveDate)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_EFFECTIVE_STATUS).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionEffectiveStatus).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.DESCRIPTION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(description).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.SHORT_DESCRIPTION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(shortDescription).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.BUSINESS_UNIT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(businessUnit).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.DEPARTMENT_ID).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(departmentId).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_STATUS).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionStatus).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.STATUS_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(statusDate)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.BUDGETED_POSITION).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(budgetedPosition).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.STANDARD_HOURS_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(standardHoursDefault.toPlainString()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.STANDARD_HOURS_FREQUENCY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(standardHoursFrequency).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_REGULAR_TEMPORARY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionRegularTemporary).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_FULL_TIME_EQUIVALENCY).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionFullTimeEquivalency.toPlainString()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_SALARY_PLAN_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionSalaryPlanDefault).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.POSITION_GRADE_DEFAULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(positionGradeDefault).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        
        PositionDataExtentedAttribute extension = (PositionDataExtentedAttribute) this.getExtension();
        sb.append(CuLaborPropertyConstants.PositionData.EXTENSION_POSITION_NUMBER).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(extension.getPositionNumber()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.EXTENSION_EFFECTIVE_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(extension.getEffectiveDate())).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.EXTENSION_ORG_CODE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(extension.getOrgCode()).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(CuLaborPropertyConstants.PositionData.EXTENSION_INACTIVATION_DATE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(extension.getInactivationDate())).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        
        return sb.toString();
    }
}
