package edu.cornell.kfs.vnd.service;

import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

import edu.cornell.kfs.vnd.service.params.VendorAddressParam;
import edu.cornell.kfs.vnd.service.params.VendorContactParam;
import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;
import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;

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
		  @WebParam(name = "addresses")List<VendorAddressParam> addresses,
		  @WebParam(name = "contacts")List<VendorContactParam> contacts,
		  @WebParam(name = "phoneNumbers")List<VendorPhoneNumberParam> phoneNumbers,
		  @WebParam(name = "supplierDiversitys")List<VendorSupplierDiversityParam> supplierDiversitys
		  ) throws Exception;
  
  /**
   * 
   * @return
   * @throws Exception
   */
  public String updateVendor(
		  @WebParam(name = "vendorName")String vendorName,
		  @WebParam(name = "vendorTypeCode")String vendorTypeCode,
		  @WebParam(name = "isForeign")boolean isForeign, 
		  @WebParam(name = "vendorNumber")String vendorNumber, 
		  @WebParam(name = "ownershipTypeCode")String ownershipTypeCode,
		  @WebParam(name = "isTaxable")boolean isTaxable,
		  @WebParam(name = "isEInvoice")boolean isEInvoice,
		  @WebParam(name = "addresses")List<VendorAddressParam> addresses,
		  @WebParam(name = "contacts")List<VendorContactParam> contacts,
		  @WebParam(name = "phoneNumbers")List<VendorPhoneNumberParam> phoneNumbers,
		  @WebParam(name = "supplierDiversitys")List<VendorSupplierDiversityParam> supplierDiversitys
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
  

public String retrieveKfsVendorByEin(
		  @WebParam(name = "vendorEin")String vendorEin
		  ) throws Exception;

public String uploadAttachment(
		  @WebParam(name = "vendorId")String vendorId,
		  @WebParam(name = "fileData")String fileData,
		  @WebParam(name = "fileName")String fileName,
		  @WebParam(name = "noteText")String noteText
		  ) throws Exception;

public String uploadAtt(
		  @WebParam(name = "vendorId")String vendorId,
		  @WebParam(name = "fileData")
		  @XmlMimeType("application/octet-stream")DataHandler fileData,
		  @WebParam(name = "fileName")String fileName,
		  @WebParam(name = "noteText")String noteText
		  ) throws Exception;


}
