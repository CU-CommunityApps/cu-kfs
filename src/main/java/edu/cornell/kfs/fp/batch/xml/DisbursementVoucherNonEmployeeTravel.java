package edu.cornell.kfs.fp.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalNullPossibleXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NonEmoployeeTravel", namespace = StringUtils.EMPTY)
public class DisbursementVoucherNonEmployeeTravel {
    
    @XmlElement(name = "travler_name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelerName;
    
    @XmlElement(name = "service_performed", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String servicePerformed;
    
    @XmlElement(name = "service_performed_location", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String servicePerformedLocationName;
    
    @XmlElement(name = "service_performed_employee_name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String servicePerformedRegularEmployeeName;
    
    @XmlElement(name = "travel_from_city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelFromCity;
    
    @XmlElement(name = "travel_from_state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelFromState;
    
    @XmlElement(name = "travel_from_country", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelFromCountry;
    
    @XmlElement(name = "travel_to_city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelToCity;
    
    @XmlElement(name = "travel_to_state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelToState;
    
    @XmlElement(name = "travel_to_country", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String travelToCountry;
    
    @XmlElement(name = "perdiem_start_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date perdiemStartDate;
    
    @XmlElement(name = "perdiem_end_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date perdiemEndDate;
    
    @XmlElement(name = "perdiem_category", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String perdiemCategory;
    
    @XmlElement(name = "auto_from_city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String autoFromCity;
    
    @XmlElement(name = "auto_from_state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String autoFromState;
    
    @XmlElement(name = "auto_to_city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String autoToCity;
    
    @XmlElement(name = "auto_to_state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String autoToState;
    
    @XmlElement(name = "perdiem_rate", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal perdiemRate;
    
    @XmlElement(name = "perdeim_actual_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    protected KualiDecimal perdiemActualAmount;
    
    @XmlElement(name = "round_trip_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String roundTripCode;
    
    @XmlElement(name = "personal_car_milage_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal personalCarMilageAmount;
    
    @XmlElement(name = "perdiem_change_reason_text", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String perdiemChangeReasonText;
    
    @XmlElement(name = "personal_car_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    protected KualiDecimal personalCarAmount;
    
    @XmlElementWrapper(name = "travler_expenses", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "expense", namespace = StringUtils.EMPTY, required = true)
    public List<DisbursementVoucherNonEmployeeExpense> travelerExpenses;
    
    @XmlElementWrapper(name = "prepaid_expenses", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "expense", namespace = StringUtils.EMPTY, required = true)
    public List<DisbursementVoucherNonEmployeeExpense> prepaidExpenses;
    
    public DisbursementVoucherNonEmployeeTravel() {
        travelerExpenses = new ArrayList<DisbursementVoucherNonEmployeeExpense>();
        prepaidExpenses = new ArrayList<DisbursementVoucherNonEmployeeExpense>();
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public String getServicePerformed() {
        return servicePerformed;
    }

    public void setServicePerformed(String servicePerformed) {
        this.servicePerformed = servicePerformed;
    }

    public String getServicePerformedLocationName() {
        return servicePerformedLocationName;
    }

    public void setServicePerformedLocationName(String servicePerformedLocationName) {
        this.servicePerformedLocationName = servicePerformedLocationName;
    }

    public String getServicePerformedRegularEmployeeName() {
        return servicePerformedRegularEmployeeName;
    }

    public void setServicePerformedRegularEmployeeName(String servicePerformedRegularEmployeeName) {
        this.servicePerformedRegularEmployeeName = servicePerformedRegularEmployeeName;
    }

    public String getTravelFromCity() {
        return travelFromCity;
    }

    public void setTravelFromCity(String travelFromCity) {
        this.travelFromCity = travelFromCity;
    }

    public String getTravelFromState() {
        return travelFromState;
    }

    public void setTravelFromState(String travelFromState) {
        this.travelFromState = travelFromState;
    }

    public String getTravelFromCountry() {
        return travelFromCountry;
    }

    public void setTravelFromCountry(String travelFromCountry) {
        this.travelFromCountry = travelFromCountry;
    }

    public String getTravelToCity() {
        return travelToCity;
    }

    public void setTravelToCity(String travelToCity) {
        this.travelToCity = travelToCity;
    }

    public String getTravelToState() {
        return travelToState;
    }

    public void setTravelToState(String travelToState) {
        this.travelToState = travelToState;
    }

    public String getTravelToCountry() {
        return travelToCountry;
    }

    public void setTravelToCountry(String travelToCountry) {
        this.travelToCountry = travelToCountry;
    }

    public Date getPerdiemStartDate() {
        return perdiemStartDate;
    }

    public void setPerdiemStartDate(Date perdiemStartDate) {
        this.perdiemStartDate = perdiemStartDate;
    }

    public Date getPerdiemEndDate() {
        return perdiemEndDate;
    }

    public void setPerdiemEndDate(Date perdiemEndDate) {
        this.perdiemEndDate = perdiemEndDate;
    }

    public String getPerdiemCategory() {
        return perdiemCategory;
    }

    public void setPerdiemCategory(String perdiemCategory) {
        this.perdiemCategory = perdiemCategory;
    }

    public String getAutoFromCity() {
        return autoFromCity;
    }

    public void setAutoFromCity(String autoFromCity) {
        this.autoFromCity = autoFromCity;
    }

    public String getAutoFromState() {
        return autoFromState;
    }

    public void setAutoFromState(String autoFromState) {
        this.autoFromState = autoFromState;
    }

    public String getAutoToCity() {
        return autoToCity;
    }

    public void setAutoToCity(String autoToCity) {
        this.autoToCity = autoToCity;
    }

    public String getAutoToState() {
        return autoToState;
    }

    public void setAutoToState(String autoToState) {
        this.autoToState = autoToState;
    }

    public KualiDecimal getPerdiemRate() {
        return perdiemRate;
    }

    public void setPerdiemRate(KualiDecimal perdiemRate) {
        this.perdiemRate = perdiemRate;
    }

    public KualiDecimal getPerdiemActualAmount() {
        return perdiemActualAmount;
    }

    public void setPerdiemActualAmount(KualiDecimal perdiemActualAmount) {
        this.perdiemActualAmount = perdiemActualAmount;
    }

    public String getRoundTripCode() {
        return roundTripCode;
    }

    public void setRoundTripCode(String roundTripCode) {
        this.roundTripCode = roundTripCode;
    }

    public KualiDecimal getPersonalCarMilageAmount() {
        return personalCarMilageAmount;
    }

    public void setPersonalCarMilageAmount(KualiDecimal personalCarMilageAmount) {
        this.personalCarMilageAmount = personalCarMilageAmount;
    }

    public String getPerdiemChangeReasonText() {
        return perdiemChangeReasonText;
    }

    public void setPerdiemChangeReasonText(String perdiemChangeReasonText) {
        this.perdiemChangeReasonText = perdiemChangeReasonText;
    }

    public KualiDecimal getPersonalCarAmount() {
        return personalCarAmount;
    }

    public void setPersonalCarAmount(KualiDecimal personalCarAmount) {
        this.personalCarAmount = personalCarAmount;
    }

    public List<DisbursementVoucherNonEmployeeExpense> getTravelerExpenses() {
        return travelerExpenses;
    }

    public void setTravelerExpenses(List<DisbursementVoucherNonEmployeeExpense> travelerExpenses) {
        this.travelerExpenses = travelerExpenses;
    }

    public List<DisbursementVoucherNonEmployeeExpense> getPrepaidExpenses() {
        return prepaidExpenses;
    }

    public void setPrepaidExpenses(List<DisbursementVoucherNonEmployeeExpense> prepaidExpenses) {
        this.prepaidExpenses = prepaidExpenses;
    }

}
