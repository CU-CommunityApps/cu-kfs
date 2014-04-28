package edu.cornell.kfs.fp.document.interfaces;

/**
 * This Interface describes all the functions needed to integrate with the CU Legacy Travel system.
 * Note, you'll need to define member variables for tripAssociationStatusCode and tripId, and they will need to be persisted.
 * @author jdh34
 *
 */
public interface CULegacyTravelIntegrationInterface {
	
	public String getTripAssociationStatusCode();
	public void setTripAssociationStatusCode(String tripAssociationStatusCode);
	
	public String getTripId();
	public void setTripId(String tripId);
	
	public String getDocumentNumber();
}
