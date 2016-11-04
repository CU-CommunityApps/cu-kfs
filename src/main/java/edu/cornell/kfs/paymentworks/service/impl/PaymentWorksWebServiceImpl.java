/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.service.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.util.PaymentWorksSupplierConversionUtil;
import edu.cornell.kfs.paymentworks.util.PaymentWorksUtil;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorsRootDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesRootDTO;

@Transactional
public class PaymentWorksWebServiceImpl implements PaymentWorksWebService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksWebServiceImpl.class);

	private static final String NEW_VENDOR_REQUEST_PENDING_VENDORS = "new-vendor-requests/";
	private static final String NEW_VENDOR_REQUEST_UPDATE_STATUS = "new-vendor-requests/bulk/";
	private static final String NEW_VENDOR_REQUEST_DETAILS_PREFIX = "new-vendor-requests";
	private static final String NEW_VENDOR_REQUEST_DETAILS_SUFFIX = "details/";
	private static final String NEW_VENDOR_REQUEST_SUPPLIER_UPLOAD = "suppliers/load/";
	private static final String VENDOR_UPDATES_PENDING_VENDORS = "updates/";
	private static final String VENDOR_UPDATES_UPDATE_STATUS = "updates/bulk/";

	private String paymentworksApiUrl;
	private String paymentworksAuthorizationToken;
	private String directoryPath;
	
	protected ClientRequest buildClientRequest(String url) {
        return buildClientRequest(url, null);
    }
	
	protected ClientRequest buildClientRequest(String url, String jsonString) {
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_XML);
        builder.header("Authorization", "Token " + paymentworksAuthorizationToken);
        if (ObjectUtils.isNotNull(jsonString)) {
        	//.put(Entity.entity(jsonString, MediaType.APPLICATION_JSON));
        	builder.entity(jsonString, MediaType.APPLICATION_JSON);
        }
        
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ClientRequest request = builder.build(uri, CuFPConstants.AmazonWebServiceBillingConstants.HTTP_METHOD_GET_NAME);
        return request;
    }
	
	protected Client buildClient() {
		ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        return client;
	}

	@Override
	public List<PaymentWorksNewVendorDTO> getPendingNewVendorRequestsFromPaymentWorks() {

		PaymentWorksNewVendorsRootDTO newVendorsRoot = null;
		List<PaymentWorksNewVendorDTO> newVendors = new ArrayList<PaymentWorksNewVendorDTO>();
		boolean moreRecords = true;
		
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + NEW_VENDOR_REQUEST_PENDING_VENDORS + "?status=0"));

		while (moreRecords) {
			// read response
			newVendorsRoot = response.getEntity(PaymentWorksNewVendorsRootDTO.class);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Status: " + response.getStatus());
			}

			// if status is ok
			if (response.getStatus() == 200) {
				if (ObjectUtils.isNotNull(newVendorsRoot)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("New Vendor Requests retrieved: " + newVendorsRoot.getCount());
					}
				}

				if (newVendorsRoot.getCount() > 0) {
					// add vendors to list
					newVendors.addAll(newVendorsRoot.getNewVendors().getNewVendorList());
				}

				// check for next
				if (StringUtils.isNotEmpty(newVendorsRoot.getNext())) {
					response = client.handle(buildClientRequest(newVendorsRoot.getNext()));
				} else {
					moreRecords = false;
				}
			} else {
				LOG.error("Failed to retrieve Pending New Vendor Requests: " + response.getEntity(String.class));
				moreRecords = false;
			}
		}
		return newVendors;
	}

	@Override
	public List<PaymentWorksVendorUpdatesDTO> getPendingCompanyVendorUpdatesFromPaymentWorks() {
		PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
		List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();
		boolean moreRecords = true;
        Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + VENDOR_UPDATES_PENDING_VENDORS + "?status=0&group_name=company"));
		
		while (moreRecords) {
			// read response
			updateVendorsRoot = response.getEntity(PaymentWorksVendorUpdatesRootDTO.class);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Status: " + response.getStatus());
			}

			// if status is ok
			if (response.getStatus() == 200) {
				if (ObjectUtils.isNotNull(updateVendorsRoot)) {
					if (LOG.isDebugEnabled()) {
						LOG.info("Vendor Updates retrieved: " + updateVendorsRoot.getCount());
					}
				}

				if (updateVendorsRoot.getCount() > 0) {
					// add vendors to list
					vendorUpdates.addAll(updateVendorsRoot.getVendorUpdates().getNewVendorList());
				}

				// check for next
				if (StringUtils.isNotEmpty(updateVendorsRoot.getNext())) {
					response = client.handle(buildClientRequest(updateVendorsRoot.getNext()));
				} else {
					moreRecords = false;
				}
			} else {
				LOG.error("Failed to retrieve Pending Vendor Updates: " + response.getEntity(String.class));
				moreRecords = false;
			}
		}
		
		return vendorUpdates;
	}

	@Override
	public List<PaymentWorksVendorUpdatesDTO> getPendingAddressVendorUpdatesFromPaymentWorks() {
		PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
		List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();
		boolean moreRecords = true;
		
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + VENDOR_UPDATES_PENDING_VENDORS + "?status=0&group_name=corporate_address&group_name=remittance_address"));
		
		while (moreRecords) {
			// read response
			updateVendorsRoot = response.getEntity(PaymentWorksVendorUpdatesRootDTO.class);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Status: " + response.getStatus());
			}

			// if status is ok
			if (response.getStatus() == 200) {
				if (ObjectUtils.isNotNull(updateVendorsRoot)) {
					if (LOG.isDebugEnabled()) {
						LOG.info("Vendor Updates retrieved: " + updateVendorsRoot.getCount());
					}
				}

				if (updateVendorsRoot.getCount() > 0) {
					// add vendors to list
					vendorUpdates.addAll(updateVendorsRoot.getVendorUpdates().getNewVendorList());
				}

				// check for next
				if (StringUtils.isNotEmpty(updateVendorsRoot.getNext())) {
					response = client.handle(buildClientRequest(updateVendorsRoot.getNext()));
				} else {
					moreRecords = false;
				}
			} else {
				LOG.error("Failed to retrieve Pending Vendor Updates: " + response.getEntity(String.class));
				moreRecords = false;
			}
		}
		
		return vendorUpdates;
	}

	@Override
	public List<PaymentWorksVendorUpdatesDTO> getPendingAchUpdatesFromPaymentWorks() {
		PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
		List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();
		boolean moreRecords = true;
		
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + VENDOR_UPDATES_PENDING_VENDORS + "?status=0&group_name=bank_account"));

		while (moreRecords) {
			// read response
			updateVendorsRoot = response.getEntity(PaymentWorksVendorUpdatesRootDTO.class);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Status: " + response.getStatus());
			}

			// if status is ok
			if (response.getStatus() == 200) {
				if (ObjectUtils.isNotNull(updateVendorsRoot)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("ACH Updates retrieved: " + updateVendorsRoot.getCount());
					}
				}

				if (updateVendorsRoot.getCount() > 0) {
					// add vendors to list
					vendorUpdates.addAll(updateVendorsRoot.getVendorUpdates().getNewVendorList());
				}

				// check for next
				if (StringUtils.isNotEmpty(updateVendorsRoot.getNext())) {
					response = client.handle(buildClientRequest(updateVendorsRoot.getNext()));
				} else {
					moreRecords = false;
				}
			} else {
				LOG.error("Failed to retrieve Pending ACH Updates: " + response.getEntity(String.class));
				moreRecords = false;
			}
		}
		
		return vendorUpdates;
	}

	@Override
	public PaymentWorksNewVendorDetailDTO getVendorDetailFromPaymentWorks(String newVendorRequestId) {
		
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + NEW_VENDOR_REQUEST_DETAILS_PREFIX + "/" + newVendorRequestId + NEW_VENDOR_REQUEST_DETAILS_SUFFIX));


		PaymentWorksNewVendorDetailDTO newVendorDetail = response.getEntity(PaymentWorksNewVendorDetailDTO.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Status: " + response.getStatus());
		}

		if (response.getStatus() == 200) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("New Vendor Request Detail retrieved");
			}
		} else {
			LOG.error("Failed to retrieve New Vendor Request Detail for Vendor Request Id(" + newVendorRequestId + "): " + response.getEntity(String.class));
		}
		
		return newVendorDetail;
	}

	@Override
	public void updateNewVendorStatusInPaymentWorks(List<PaymentWorksNewVendorUpdateVendorStatus> paymentWorksUpdateNewVendorStatus) {
		
		String jsonString = new PaymentWorksUtil().pojoToJsonString(paymentWorksUpdateNewVendorStatus);
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + NEW_VENDOR_REQUEST_UPDATE_STATUS, jsonString ));

		if (LOG.isDebugEnabled()) {
			LOG.debug("Status: " + response.getStatus());
		}

		if (response.getStatus() == 200) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("New Vendor Request status updated");
			}
		} else {
			LOG.error("Failed to update New Vendor Request status: " + response.getEntity(String.class));
		}
	}

	@Override
	public void updateVendorUpdatesStatusInPaymentWorks(
			List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus) {
		
		String jsonString = new PaymentWorksUtil().pojoToJsonString(paymentWorksUpdateVendorStatus);
		Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(getPaymentworksApiUrl() + NEW_VENDOR_REQUEST_UPDATE_STATUS, jsonString ));

		if (LOG.isDebugEnabled()) {
			LOG.debug("Status: " + response.getStatus());
		}

		if (response.getStatus() == 200) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Vendor Update status updated");
			}
		} else {
			LOG.error("Failed to update Vendor Update status: " + response.getEntity(String.class));
		}
	}

	@Override
	public boolean paymentWorksUploadSuppliers(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadDTO) {

		boolean isUploaded = false;
		
		/*
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.register(MultiPartFeature.class);
		clientConfig.register(LoggingFilter.class);
		Client client = ClientBuilder.newClient(clientConfig);

		WebTarget webTarget = client.target(getPaymentworksApiUrl()).path(NEW_VENDOR_REQUEST_SUPPLIER_UPLOAD);

		MultiPart multiPart = new MultiPart();
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

		// create supplier upload csv file
		String supplierUploadFileName = new PaymentworksSupplierConversionUtil()
				.createSupplierUploadFile(paymentworksSupplierUploadDTO, directoryPath);

		FormDataBodyPart fileDataBodyPart = new FileDataBodyPart("suppliers", new File(supplierUploadFileName),
				MediaType.MULTIPART_FORM_DATA_TYPE);
		multiPart.bodyPart(fileDataBodyPart);

		Response response = webTarget.request().header("Authorization", "Token " + paymentworksAuthorizationToken)
				.post(Entity.entity(multiPart, multiPart.getMediaType()));

		// delete supplier upload csv file
		new PaymentworksSupplierConversionUtil().deleteSupplierUploadFile(supplierUploadFileName);

		if (LOG.isDebugEnabled()) {
			LOG.info("Status: " + response.getStatus());
		}

		if (response.getStatus() == 200) {
			isUploaded = true;

			if (LOG.isDebugEnabled()) {
				LOG.debug("Supplier upload succeeded for " + paymentworksSupplierUploadDTO.size() + " vendor records");
			}
		} else {
			isUploaded = false;
			LOG.error("Failed to upload Supplier File for " + paymentworksSupplierUploadDTO.size() + " vendor records: "
					+ response.readEntity(String.class));
		}
		*/
		return isUploaded;
	}

	public String getPaymentworksAuthorizationToken() {
		return paymentworksAuthorizationToken;
	}

	public void setPaymentworksAuthorizationToken(String paymentworksAuthorizationToken) {
		this.paymentworksAuthorizationToken = paymentworksAuthorizationToken;
	}

	public String getPaymentworksApiUrl() {
		return paymentworksApiUrl;
	}

	public void setPaymentworksApiUrl(String paymentworksApiUrl) {
		this.paymentworksApiUrl = paymentworksApiUrl;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

}
