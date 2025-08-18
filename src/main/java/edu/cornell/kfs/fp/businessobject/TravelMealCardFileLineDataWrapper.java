package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class TravelMealCardFileLineDataWrapper extends PersistableBusinessObjectBase {

    protected TravelMealCardVerificationData travelMealCardVerificationData;
    protected TravelMealCardCertificationData travelMealCardCertificationData;

    public TravelMealCardFileLineDataWrapper(TravelMealCardVerificationData travelMealCardVerificationData,
            TravelMealCardCertificationData travelMealCardCertificationData) {
        this.travelMealCardVerificationData = travelMealCardVerificationData;
        this.travelMealCardCertificationData = travelMealCardCertificationData; 
    }

    public TravelMealCardVerificationData getTravelMealCardVerificationData() {
        return travelMealCardVerificationData;
    }

    public void setTravelMealCardVerificationData(TravelMealCardVerificationData travelMealCardVerificationData) {
        this.travelMealCardVerificationData = travelMealCardVerificationData;
    }

    public TravelMealCardCertificationData getTravelMealCardCertificationData() {
        return travelMealCardCertificationData;
    }

    public void setTravelMealCardCertificationData(TravelMealCardCertificationData travelMealCardCertificationData) {
        this.travelMealCardCertificationData = travelMealCardCertificationData;
    }

}
