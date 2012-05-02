package edu.cornell.kfs.module.bc.document.service;

import java.util.List;

import edu.cornell.kfs.module.bc.businessobject.SipImportData;

public interface SipDistributionService {

    /**
     * Calculates and applies SIP to BC.
     * 
     * Summary of what the process does:
     * 
     * 1. calculate new comp rate and annual rate based on SIP awards and previous comp
     * rate and annual rate - build report entries with the new amounts - save the SIP
     * data inlcuding the new comp rate and annual rate in the SIP table (data is saved
     * both in REPORT and UPDATE mode)
     * 
     * 2. retrieve the appointment funding entries for the emplid. position nbr pairs in
     * the sip import and calculate the new requested amount by multiplying the new annual
     * rate with the requested amount - build report entries with the updated appointment
     * funding entries - if in UPDATE mode save the updated appointment funding entries
     * 
     * 3. retrieve the Pending BC GL entries for the affected appointment funding entries
     * and update the requested amount to reflect the SIP changes - build report entries
     * for the updated Pending BC GL entries - if in UPDATE mode save the updated Pending
     * BC GL entries
     * 
     * 4. for all the affected Pending BC GL entries retrieve the annual benefits lines
     * and update the requested amount - build report entries with the updated annual
     * benefits - if in UPDATE mode save the updated annual benefits
     * 
     * 5. for all the affected benefits lines check if there are monthly benefits, if
     * there are then update the first month amount to reflect the SIP changes - build
     * report entries with the updated monthly benefits - if in UPDATE mode save the
     * updates monthly benefits
     * 
     * 6. retrieve the 2PLG entries for the affected edocs in order to remove them - build
     * report entries with the 2PLG entries to be deleted - if in UPDATE mode delete the
     * 2PLG entries
     * 
     * 7. retrieve the SIP pools for the affected edocs in order to remove them - build
     * report entries for the SIP pools to be removed - if in UPDATE mode remove the SIP
     * pools
     * 
     * 8. create new PLUG entries that have as requested amount = total of the revenues -
     * total of the expenditures - build report entries with the new PLUG entries - if in
     * UPDATE mode save the new PLUG entries
     * 
     * 
     * 
     * @param updateMode
     * @param sipImportDataCollection
     * @return
     */
    public StringBuilder distributeSip(boolean updateMode, List<SipImportData> sipImportDataCollection);

}
