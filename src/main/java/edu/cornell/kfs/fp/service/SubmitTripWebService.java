package edu.cornell.kfs.fp.service;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * <p>Title: SubmitTripWebService</p>
 * <p>Description: Describes the functions that need to be implemented in the trip submission web service</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @author Sandy Eccleston
 * @version 1.0
 */

@WebService 
public interface SubmitTripWebService {

  /**
   * 
   * @return
   * @throws Exception
   */
  public String submitTrip(
		  @WebParam(name = "dvDescription")String dvDescription,
		  @WebParam(name = "dvExplanation")String dvExplanation,
		  @WebParam(name = "tripNumber")String tripNumber,
		  @WebParam(name = "travelerNetId")String travelerNetId,
		  @WebParam(name = "initiatorNetId")String initiatorNetId,
		  @WebParam(name = "totalAmount")double totalAmount,
		  @WebParam(name = "checkStubText")String checkStubText
		  ) throws Exception;
  
  public boolean isValidDVInitiator(
		  @WebParam(name = "initiatorNetId")String initiatorNetId
		  ) throws Exception;
}
