package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.module.purap.businessobject.RequisitionItem;

public class CuRequisitionItem extends RequisitionItem {

    // KFSPTS-985 : this is for setdistribution
    private Integer favoriteAccountLineIdentifier;
    
    // KFSPTS-2257 : eshop flag enhancement
    // (Product Flags - Controlled Substance, Energy Star, Green, Hazardous Material, Rad Minor, Radioactive, Recycled, Select Agent, Toxin)

    private boolean controlled;
    private boolean green;
    private boolean hazardous;
    private boolean radioactive;
    private boolean radioactiveMinor;
    private boolean selectAgent;
    private boolean toxin;
    private boolean recycled; 
    private boolean energyStar;
     
    public Integer getFavoriteAccountLineIdentifier() {
        return favoriteAccountLineIdentifier;
    }
    
    public void setFavoriteAccountLineIdentifier(
             Integer favoriteAccountLineIdentifier) {
        this.favoriteAccountLineIdentifier = favoriteAccountLineIdentifier;
    }
    
    public boolean isControlled() {
        return controlled;
    }
    
    public void setControlled(boolean controlled) {
        this.controlled = controlled;
    }
    
    public boolean isGreen() {
        return green;
    }
    
     public void setGreen(boolean green) {
        this.green = green;
    }
    
    public boolean isHazardous() {
        return hazardous;
    }
    
    public void setHazardous(boolean hazardous) {
        this.hazardous = hazardous;
    }
    
    public boolean isRadioactive() {
        return radioactive;
    }
    
    public void setRadioactive(boolean radioactive) {
        this.radioactive = radioactive;
    }
    
    public boolean isRadioactiveMinor() {
        return radioactiveMinor;
    }
    
    public void setRadioactiveMinor(boolean radioactiveMinor) {
        this.radioactiveMinor = radioactiveMinor;
    }
    
    public boolean isSelectAgent() {
        return selectAgent;
    }
    
    public void setSelectAgent(boolean selectAgent) {
        this.selectAgent = selectAgent;
    }
    
    public boolean isToxin() {
        return toxin;
    }
    
    public void setToxin(boolean toxin) {
        this.toxin = toxin;
    }
    
    public boolean isRecycled() {
        return recycled;
    }
    
    public void setRecycled(boolean recycled) {
        this.recycled = recycled;
    }
    
    public boolean isEnergyStar() {
        return energyStar;
    }
    
    public void setEnergyStar(boolean energyStar) {
        this.energyStar = energyStar;
    }
     
     // KFSPTS-2257
    public String getEshopFlags() {
        StringBuilder sb = new StringBuilder();
        if (isControlled()) {
            sb.append("Controlled,");
        }
        if (isEnergyStar()) {
            sb.append("Energy Star,");
        }
        if (isSelectAgent()) {
            sb.append("Select Agent,");
        }
        if (isRadioactive()) {
            sb.append("Radioactive,");
        }
        if (isRadioactiveMinor()) {
            sb.append("Radioactive Minor,");
        }
        if (isRecycled()) {
            sb.append("Recycled,");
        }
        if (isGreen()) {
            sb.append("Green,");
        }
        if (isToxin()) {
            sb.append("Toxin,");
        }
        if (isHazardous()) {
            sb.append("Hazardous,");
        }
        if (sb.length() == 0) {
            return "No e-Shop flag is set to true";
        }
        return sb.substring(0, sb.length() - 1);
    }
    
}
