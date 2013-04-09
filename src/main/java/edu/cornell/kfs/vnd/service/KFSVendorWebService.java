package edu.cornell.kfs.vnd.service;

import java.util.Map;

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
		  @WebParam(name = "vendorType")String vendorType
		  ) throws Exception;
  
  /**
   * 
   * @param initiatorNetId
   * @param documentName
   * @return
   * @throws Exception
   */
  public boolean updateVendor(
		  @WebParam(name = "documentName")String documentName
		  ) throws Exception;

  /**
   * 
   * @param viewerNetId
   * @param docID
   * @return
   * @throws Exception
   */
  public String retrieveKfsVendor(
		  @WebParam(name = "vendorId")String vendorId,
		  @WebParam(name = "vendorIdType")String vendorIdType
		  ) throws Exception;

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
