package edu.cornell.kfs.vnd.service;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * <p>Title: KFSVendorWebService</p>
 * <p>Description: Describes the functions that need to be implemented in the vendor web service</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @version 1.0
 */

@WebService 
public interface KFSVendorWebService {

  /**
   * 
   * @return
   * @throws Exception
   */
  public String addVendor(
		  @WebParam(name = "vendorName")String vendorName,
		  @WebParam(name = "vendorTypeCode")String vendorTypeCode,
		  @WebParam(name = "isForeign")boolean isForeign, 
		  @WebParam(name = "taxNumber")String taxNumber, 
		  @WebParam(name = "taxNumberType")String taxNumberType, 
		  @WebParam(name = "ownershipTypeCode")String ownershipTypeCode,
		  @WebParam(name = "isTaxable")boolean isTaxable,
		  @WebParam(name = "isEInvoice")boolean isEInvoice,
		  @WebParam(name = "vendorAddressTypeCode")String vendorAddressTypeCode, 
		  @WebParam(name = "vendorLine1Address")String vendorLine1Address, 
		  @WebParam(name = "vendorCityName")String vendorCityName, 
		  @WebParam(name = "vendorStateCode")String vendorStateCode, 
		  @WebParam(name = "vendorPostalCode")String vendorPostalCode, 
		  @WebParam(name = "vendorCountryCode")String vendorCountryCode,
		  @WebParam(name = "contactName")String contactName,
		  @WebParam(name = "poTransmissionMethodCode")String poTransmissionMethodCode,
		  @WebParam(name = "emailOrFaxNumber")String emailOrFaxNumber
		  ) throws Exception;
  
  /**
   * 
   * @param vendorId
   * @param vendorIdType
   * @return
   * @throws Exception
   */
  public String retrieveKfsVendor(
		  @WebParam(name = "vendorId")String vendorId,
		  @WebParam(name = "vendorIdType")String vendorIdType
		  ) throws Exception;

  /**
   * 
   * @param vendorName
   * @param lastFour
   * @return
   * @throws Exception
   */
  public String retrieveKfsVendorByNamePlusLastFour(
		  @WebParam(name = "vendorName")String vendorName, 
		  @WebParam(name = "lastFour")String lastFour) throws Exception;
  
  /**
   * 
   * @param vendorId
   * @param vendorIdType
   * @return
   * @throws Exception
   */
  public boolean vendorExists(
		  @WebParam(name = "vendorId")String vendorId,
		  @WebParam(name = "vendorIdType")String vendorIdType
		  ) throws Exception;
  
}
