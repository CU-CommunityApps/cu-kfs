package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.ProcurementCardHolder;

public class CorporateBilledCorporatePaidCardHolder extends ProcurementCardHolder {
    private static final long serialVersionUID = -9136190795527646788L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCardHolder.class);
    
    @Override
    public void setCardHolderLine1Address(String cardHolderLine1Address) {
        LOG.error("should not call setCardHolderLine1Address");
        throw new RuntimeException("WTF is going om?");
    }
    
    @Override
    public String getCardHolderLine1Address() {
        LOG.error("should not call getCardHolderLine1Address");
        return super.getCardHolderLine1Address();
    }
    
    @Override
    public void setCardHolderLine2Address(String cardHolderLine2Address) {
        LOG.error("should not call setCardHolderLine2Address");
        super.setCardHolderLine2Address(cardHolderLine2Address);
    }
    
    @Override
    public String getCardHolderLine2Address() {
        LOG.error("should not call getCardHolderLine2Address");
        return super.getCardHolderLine2Address();
    }
    
    @Override
    public void setCardHolderCityName(String cardHolderCityName) {
        LOG.error("should not call setCardHolderCityName");
        super.setCardHolderCityName(cardHolderCityName);
    }
    
    @Override
    public String getCardHolderCityName() {
        LOG.error("should not call getCardHolderCityName");
        return super.getCardHolderCityName();
    }
    
    @Override
    public void setCardHolderStateCode(String cardHolderStateCode) {
        LOG.error("should not call setCardHolderStateCode");
        super.setCardHolderStateCode(cardHolderStateCode);
    }
    
    @Override
    public String getCardHolderStateCode() {
        LOG.error("should not call getCardHolderStateCode");
        return super.getCardHolderStateCode();
    }
    
    @Override
    public void setCardHolderZipCode(String cardHolderZipCode) {
        LOG.error("should not call setCardHolderZipCode");
        super.setCardHolderZipCode(cardHolderZipCode);
    }
    
    @Override
    public String getCardHolderZipCode() {
        LOG.error("should not call getCardHolderZipCode");
        return super.getCardHolderZipCode();
    }
    
    @Override
    public void setCardHolderWorkPhoneNumber(String cardHolderWorkPhoneNumber) {
        LOG.error("should not call setCardHolderWorkPhoneNumber");
        super.setCardHolderWorkPhoneNumber(cardHolderWorkPhoneNumber);
    }
    
    @Override
    public String getCardHolderWorkPhoneNumber() {
        LOG.error("should not call getCardHolderWorkPhoneNumber");
        return super.getCardHolderWorkPhoneNumber();
    }

}
