package edu.cornell.kfs.coa.batch.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.hc.core5.net.URIBuilder;
import org.kuali.kfs.sys.web.WebClientFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.service.DownloadLegacyAccountAttachmentsService;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class DownloadLegacyAccountAttachmentsServiceImpl implements DownloadLegacyAccountAttachmentsService {

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^(/\\w+)+$");

    private WebServiceCredentialService webServiceCredentialService;

    @Override
    public void downloadAndProcessLegacyAccountAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final FailableBiConsumer<LegacyAccountAttachment, DataBuffer, IOException> attachmentProcessor)
            throws IOException, URISyntaxException {
        Objects.requireNonNull(legacyAccountAttachment, "legacyAccountAttachment cannot be null");
        Objects.requireNonNull(attachmentProcessor, "attachmentProcessor cannot be null");
        DataBuffer fileDataBuffer = null;
        try {
            final URI fileDownloadUrl = buildAttachmentDownloadUrl(legacyAccountAttachment.getFilePath());
            final String apiKey = getApiKey();
            fileDataBuffer = getClient()
                    .get()
                    .uri(fileDownloadUrl)
                    .header(CuCoaBatchConstants.DFA_ATTACHMENTS_API_KEY, apiKey)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, ClientResponse::createException)
                    .toEntityFlux(BodyExtractors.toDataBuffers())
                    .flatMap(httpEntity -> DataBufferUtils.join(httpEntity.getBody()))
                    .block();
            attachmentProcessor.accept(legacyAccountAttachment, fileDataBuffer);
        } finally {
            if (fileDataBuffer != null) {
                DataBufferUtils.release(fileDataBuffer);
            }
        }
    }

    private URI buildAttachmentDownloadUrl(final String filePath)
            throws URISyntaxException {
        final String baseUrl = getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalStateException("Base DFA attachment URL endpoint cannot be blank");
        } else if (!FILE_PATH_PATTERN.matcher(filePath).matches()) {
            throw new IllegalArgumentException("Account attachment has invalid or malformed file path: " + filePath);
        }
        return new URIBuilder(baseUrl)
                .appendPath(filePath)
                .build();
    }

    private WebClient getClient() {
        return WebClientFactory.create();
    }

    private String getBaseUrl() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE, CuCoaBatchConstants.DFA_ATTACHMENTS_URL_KEY);
    }

    private String getApiKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE, CuCoaBatchConstants.DFA_ATTACHMENTS_API_KEY);
    }

    public void setWebServiceCredentialService(final WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
