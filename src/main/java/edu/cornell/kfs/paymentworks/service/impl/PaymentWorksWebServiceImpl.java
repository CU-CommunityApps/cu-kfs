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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUploadSupplierService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
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
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksWebServiceImpl.class);

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

    protected PaymentWorksUtilityService paymentWorksUtilityService;
    protected PaymentWorksUploadSupplierService paymentWorksUploadSupplierService;

    protected ClientRequest buildClientRequest(String url) {
        LOG.info("buildClientRequest1 URL: " + url);
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.header(PaymentWorksConstants.AUTHORIZATION_HEADER_KEY, buildAuthorizationHeaderString());
        builder.accept(MediaType.APPLICATION_XML);
        return builder.build(buildURI(url), HttpMethod.GET);
    }

    protected ClientRequest buildClientRequest(String url, String jsonString) {
        LOG.info("buildClientRequest2 URL: " + url + "  jsonString: " + jsonString);
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.header(PaymentWorksConstants.AUTHORIZATION_HEADER_KEY, buildAuthorizationHeaderString());
        builder.accept(MediaType.APPLICATION_JSON);
        builder.entity(jsonString, MediaType.APPLICATION_JSON);

        return builder.build(buildURI(url), HttpMethod.PUT);
    }

    protected ClientRequest buildClientRequest(String url, MultiPart MultiPartUploadFile) {
        LOG.info("buildClientRequest3 URL: " + url);
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_JSON);
        builder.header(PaymentWorksConstants.AUTHORIZATION_HEADER_KEY, buildAuthorizationHeaderString());
        builder.entity(MultiPartUploadFile, MediaType.MULTIPART_FORM_DATA);

        return builder.build(buildURI(url), HttpMethod.POST);
    }

    protected URI buildURI(String url) {
        LOG.debug("buildURI The passed in URL is: " + url);
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            LOG.error("buildURI unable to build a URI object for the URL: '" + url + "' with the error: ", e);
            throw new RuntimeException("Unable to build URI for the URL: " + url, e);
        }
        return uri;
    }

    protected String buildAuthorizationHeaderString() {
        return PaymentWorksConstants.TOKEN_HEADER_KEY + paymentworksAuthorizationToken;
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
        ClientResponse response = client.handle(buildClientRequest(buildPendingNewVendorRequestsFromPaymentWorksURL()));

        while (moreRecords) {
            newVendorsRoot = response.getEntity(PaymentWorksNewVendorsRootDTO.class);

            LOG.debug("getPendingNewVendorRequestsFromPaymentWorks, Status: " + response.getStatus());
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                if (ObjectUtils.isNotNull(newVendorsRoot)) {
                    LOG.debug("getPendingNewVendorRequestsFromPaymentWorks, New Vendor Requests retrieved: " + newVendorsRoot.getCount());
                }
                if (newVendorsRoot.getCount() > 0) {
                    newVendors.addAll(newVendorsRoot.getNewVendors().getNewVendorList());
                }

                if (StringUtils.isNotEmpty(newVendorsRoot.getNext())) {
                    response = client.handle(buildClientRequest(newVendorsRoot.getNext()));
                } else {
                    moreRecords = false;
                }
            } else {
                LOG.error("getPendingNewVendorRequestsFromPaymentWorks, Failed to retrieve Pending New Vendor Requests: " + response.getEntity(String.class));
                moreRecords = false;
            }
        }
        return newVendors;
    }

    protected String buildPendingNewVendorRequestsFromPaymentWorksURL() {
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(NEW_VENDOR_REQUEST_PENDING_VENDORS)
                .append(PaymentWorksConstants.QUESTION_MARK)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.STATUS)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksNewVendorStatus.PENDING)).toString();
        return URL;
    }

    @Override
    public List<PaymentWorksVendorUpdatesDTO> getPendingCompanyVendorUpdatesFromPaymentWorks() {
        PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
        List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();

        processVendorUpdates(vendorUpdates, buildPendingCompanyVendorUpdatesFromPaymentWorksURL());

        return vendorUpdates;
    }

    protected void processVendorUpdates(List<PaymentWorksVendorUpdatesDTO> vendorUpdates, String url) {
        PaymentWorksVendorUpdatesRootDTO updateVendorsRoot;
        boolean moreRecords = true;

        Client client = buildClient();
        ClientResponse response = client.handle(buildClientRequest(url));

        while (moreRecords) {
            updateVendorsRoot = response.getEntity(PaymentWorksVendorUpdatesRootDTO.class);

            LOG.debug("processVendorUpdates, Status: " + response.getStatus());

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                if (ObjectUtils.isNotNull(updateVendorsRoot)) {
                    LOG.info("processVendorUpdates Vendor Updates retrieved: " + updateVendorsRoot.getCount());
                }

                if (updateVendorsRoot.getCount() > 0) {
                    vendorUpdates.addAll(updateVendorsRoot.getVendorUpdates().getNewVendorList());
                }

                if (StringUtils.isNotEmpty(updateVendorsRoot.getNext())) {
                    response = client.handle(buildClientRequest(updateVendorsRoot.getNext()));
                } else {
                    moreRecords = false;
                }
            } else {
                LOG.error("processVendorUpdates, Failed to retrieve Pending Vendor Updates: " + response.getEntity(String.class));
                moreRecords = false;
            }
        }
    }

    protected String buildPendingCompanyVendorUpdatesFromPaymentWorksURL() {
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(VENDOR_UPDATES_PENDING_VENDORS)
                .append(PaymentWorksConstants.QUESTION_MARK)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.STATUS)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksUpdateStatus.PENDING).append(PaymentWorksConstants.AMPERSAND)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.GROUP_NAME)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksURLGroupNameOptions.COMPANY)).toString();
        return URL;
    }

    @Override
    public List<PaymentWorksVendorUpdatesDTO> getPendingAddressVendorUpdatesFromPaymentWorks() {
        PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
        List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();

        processVendorUpdates(vendorUpdates, buildPendingAddressVendorUpdatesFromPaymentWorksURL());

        return vendorUpdates;
    }

    protected String buildPendingAddressVendorUpdatesFromPaymentWorksURL() {
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(VENDOR_UPDATES_PENDING_VENDORS)
                .append(PaymentWorksConstants.QUESTION_MARK)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.STATUS)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksUpdateStatus.PENDING).append(PaymentWorksConstants.AMPERSAND)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.GROUP_NAME)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksURLGroupNameOptions.CORPORATE_ADDRESS)
                .append(PaymentWorksConstants.AMPERSAND)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.GROUP_NAME)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksURLGroupNameOptions.REMITTANCE_ADDRESS)).toString();
        return URL;
    }

    @Override
    public List<PaymentWorksVendorUpdatesDTO> getPendingAchUpdatesFromPaymentWorks() {
        PaymentWorksVendorUpdatesRootDTO updateVendorsRoot = null;
        List<PaymentWorksVendorUpdatesDTO> vendorUpdates = new ArrayList<PaymentWorksVendorUpdatesDTO>();

        processVendorUpdates(vendorUpdates, buildPendingAchUpdatesFromPaymentWorksURL());

        return vendorUpdates;
    }

    protected String buildPendingAchUpdatesFromPaymentWorksURL() {
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(VENDOR_UPDATES_PENDING_VENDORS)
                .append(PaymentWorksConstants.QUESTION_MARK)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.STATUS)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksUpdateStatus.PENDING).append(PaymentWorksConstants.AMPERSAND)
                .append(PaymentWorksConstants.PaymentWorksURLParameters.GROUP_NAME)
                .append(PaymentWorksConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksURLGroupNameOptions.BANK_ACCOUNT)).toString();
        return URL;
    }

    @Override
    public PaymentWorksNewVendorDetailDTO getVendorDetailFromPaymentWorks(String newVendorRequestId) {
        Client client = buildClient();
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(NEW_VENDOR_REQUEST_DETAILS_PREFIX)
                .append(PaymentWorksConstants.FORWARD_SLASH).append(newVendorRequestId)
                .append(PaymentWorksConstants.FORWARD_SLASH).append(NEW_VENDOR_REQUEST_DETAILS_SUFFIX)).toString();
        ClientResponse response = client.handle(buildClientRequest(URL));

        PaymentWorksNewVendorDetailDTO newVendorDetail = null;

        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            LOG.debug("getVendorDetailFromPaymentWorks, New Vendor Request Detail retrieved.");
            newVendorDetail = response.getEntity(PaymentWorksNewVendorDetailDTO.class);
        } else {
            LOG.error("Failed to retrieve New Vendor Request Detail for Vendor Request Id(" + newVendorRequestId + "): "
                    + response.getEntity(String.class));
            throw new RuntimeException("Unable to get vendor detail information from PaymentWorks");
        }

        return newVendorDetail;
    }

    @Override
    public void updateNewVendorStatusInPaymentWorks(List<PaymentWorksNewVendorUpdateVendorStatus> paymentWorksUpdateNewVendorStatus) {
        String jsonString = getPaymentWorksUtilityService().pojoToJsonString(paymentWorksUpdateNewVendorStatus);
        Client client = buildClient();
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(NEW_VENDOR_REQUEST_UPDATE_STATUS)).toString();
        LOG.info("updateNewVendorStatusInPaymentWorks. json string: " + jsonString);
        ClientResponse response = client.handle(buildClientRequest(URL, jsonString));

        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            LOG.debug("updateNewVendorStatusInPaymentWorks, New Vendor Request status updated");
        } else {
            LOG.error("updateNewVendorStatusInPaymentWorks, Failed to update New Vendor Request status: " + response.getEntity(String.class));
            throw new RuntimeException("Unable to update new vendor status in PaymentWorks");
        }
    }

    @Override
    public void updateNewVendorUpdatesStatusInPaymentWorks(List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus) {
        updateVendorStatus(paymentWorksUpdateVendorStatus, NEW_VENDOR_REQUEST_UPDATE_STATUS);
    }

    @Override
    public void updateExistingVendorUpdatesStatusInPaymentWorks(List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus) {
        updateVendorStatus(paymentWorksUpdateVendorStatus, VENDOR_UPDATES_UPDATE_STATUS);
    }

    protected void updateVendorStatus(List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus, String updateURLSection) {
        String jsonString = getPaymentWorksUtilityService().pojoToJsonString(paymentWorksUpdateVendorStatus);
        Client client = buildClient();
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(updateURLSection)).toString();
        ClientResponse response = client.handle(buildClientRequest(URL, jsonString));

        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            LOG.debug("updateVendorStatus, Vendor Update status updated");
        } else {
            LOG.error("updateVendorStatus, Failed to update Vendor Update status: " + response.getEntity(String.class));
            throw new RuntimeException("Unable to update vendor status in PaymentWorks");
        }
    }

    @Override
    public boolean uploadSuppliers(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadDTO) {
        boolean isUploaded = false;

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        String supplierUploadFileName = getPaymentWorksUploadSupplierService().createSupplierUploadFile(paymentWorksSupplierUploadDTO, directoryPath);
        LOG.info("uploadSuppliers, The file to be uploaded: " + supplierUploadFileName);
        FormDataBodyPart fileDataBodyPart = new FileDataBodyPart("suppliers", new File(supplierUploadFileName), MediaType.MULTIPART_FORM_DATA_TYPE);
        multiPart.bodyPart(fileDataBodyPart);

        Client client = buildClient();
        String URL = (new StringBuilder(getPaymentworksApiUrl()).append(NEW_VENDOR_REQUEST_SUPPLIER_UPLOAD)).toString();
        LOG.info("URL: " + URL);
        ClientResponse response = client.handle(buildClientRequest(URL, multiPart));

        LOG.info("updateNewVendorStatusInPaymentWorks, Status: " + response.getStatus());

        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            isUploaded = true;
            LOG.debug("Supplier upload succeeded for " + paymentWorksSupplierUploadDTO.size() + " vendor records");
        } else {
            isUploaded = false;
            LOG.error("Failed to upload Supplier File for " + paymentWorksSupplierUploadDTO.size() + " vendor records: "
                    + response.getEntity(String.class));
            throw new RuntimeException("Unable to send supplier file to PaymentWorks");

        }
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

    public PaymentWorksUtilityService getPaymentWorksUtilityService() {
        return paymentWorksUtilityService;
    }

    public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
        this.paymentWorksUtilityService = paymentWorksUtilityService;
    }

    public PaymentWorksUploadSupplierService getPaymentWorksUploadSupplierService() {
        return paymentWorksUploadSupplierService;
    }

    public void setPaymentWorksUploadSupplierService(PaymentWorksUploadSupplierService paymentWorksUploadSupplierService) {
        this.paymentWorksUploadSupplierService = paymentWorksUploadSupplierService;
    }
}
