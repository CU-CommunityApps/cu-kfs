package edu.cornell.kfs.concur.batch.service.impl;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;

public class ConcurStandardAccountingExtractPdpFeedFileHelper {
    private PdpFeedFileBaseEntry pdpFeedFileBaseEntry;
    private PdpFeedGroupEntry currentGroup;
    private PdpFeedDetailEntry currentDetailEntry;
    PdpFeedAccountingEntry currentAccountingEntry;
    
    public ConcurStandardAccountingExtractPdpFeedFileHelper() {
        pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeId(new PdpFeedPayeeIdEntry());
        currentDetailEntry = new  PdpFeedDetailEntry();
        currentAccountingEntry = new PdpFeedAccountingEntry();
    }
    
    public void addCurrentGroupToPdpFeedFileAndReset() {
        pdpFeedFileBaseEntry.getGroup().add(currentGroup);
        currentGroup = new PdpFeedGroupEntry();
    }
    
    public void addCurrentAccountingToDetailAndReset(){
        currentDetailEntry.getAccounting().add(currentAccountingEntry);
        currentAccountingEntry = new PdpFeedAccountingEntry();
    }
    
    public void addCurrentDetailToGroupAndReset() {
        currentGroup.getDetail().add(currentDetailEntry);
        currentDetailEntry = new  PdpFeedDetailEntry();
    }

    public PdpFeedFileBaseEntry getPdpFeedFileBaseEntry() {
        return pdpFeedFileBaseEntry;
    }

    public void setPdpFeedFileBaseEntry(PdpFeedFileBaseEntry pdpFeedFileBaseEntry) {
        this.pdpFeedFileBaseEntry = pdpFeedFileBaseEntry;
    }

    public PdpFeedGroupEntry getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(PdpFeedGroupEntry currentGroup) {
        this.currentGroup = currentGroup;
    }

    public PdpFeedDetailEntry getCurrentDetailEntry() {
        return currentDetailEntry;
    }

    public void setCurrentDetailEntry(PdpFeedDetailEntry currentDetailEntry) {
        this.currentDetailEntry = currentDetailEntry;
    }

    public PdpFeedAccountingEntry getCurrentAccountingEntry() {
        return currentAccountingEntry;
    }

    public void setCurrentAccountingEntry(PdpFeedAccountingEntry currentAccountingEntry) {
        this.currentAccountingEntry = currentAccountingEntry;
    }
}
