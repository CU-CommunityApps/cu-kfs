package edu.cornell.kfs.coa.rest.resource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentErrorResponseDto;
import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;
import edu.cornell.kfs.coa.service.AccountAttachmentService;
import edu.cornell.kfs.sys.web.CuResponseStatusException;
import edu.cornell.kfs.sys.web.LazyInputStreamResource;

@RestController
@RequestMapping(path = "coa/account-attachments/api")
public class AccountAttachmentController {

    private static final Logger LOG = LogManager.getLogger();

    private final AccountAttachmentService accountAttachmentService;
    private final AttachmentService attachmentService;

    @Autowired
    public AccountAttachmentController(
            final AccountAttachmentService accountAttachmentService,
            final AttachmentService attachmentService) {
        Objects.requireNonNull(accountAttachmentService,
                "accountAttachmentService cannot be null");
        Objects.requireNonNull(attachmentService, "attachmentService cannot be null");
        this.accountAttachmentService = accountAttachmentService;
        this.attachmentService = attachmentService;
    }

    @GetMapping(path = "get-attachment-list")
    public ResponseEntity<AccountAttachmentListingDto> getAttachmentMetadataForAccount(
            @RequestParam("chartCode") final String chartCode,
            @RequestParam("accountNumber") final String accountNumber
    ) {
        LOG.debug("getAttachmentMetadataForAccount, Chart: <{}>, Account: <{}>", chartCode, accountNumber);
        final AccountAttachmentListingDto attachmentListing = accountAttachmentService
                .getAccountAttachmentListing(chartCode, accountNumber);
        return ResponseEntity.ok(attachmentListing);
    }

    @GetMapping(path = "get-attachment-contents")
    public ResponseEntity<Resource> getAccountAttachment(
            @RequestParam("chartCode") final String chartCode,
            @RequestParam("accountNumber") final String accountNumber,
            @RequestParam("attachmentId") final String attachmentId
    ) {
        LOG.debug("getAccountAttachment, Chart: <{}>, Account: <{}>, Attachment ID: <{}>",
                chartCode, accountNumber, attachmentId);
        final Attachment attachment = accountAttachmentService.getAccountAttachment(
                chartCode, accountNumber, attachmentId);
        final Resource attachmentResource = new LazyInputStreamResource(
                () -> attachmentService.retrieveAttachmentContents(attachment),
                Optional.of(attachment.getAttachmentFileSize()),
                attachment.getAttachmentFileName());

        // Header setup derived from base financials BatchFileController.getBusinessObjectBatchFile() method.
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setExpires(0L);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setContentDisposition(createAttachmentContentDisposition(attachment));

        return ResponseEntity.ok()
                .headers(headers)
                .body(attachmentResource);
    }

    private ContentDisposition createAttachmentContentDisposition(final Attachment attachment) {
        return ContentDisposition.attachment()
                .filename(attachment.getAttachmentFileName(), StandardCharsets.UTF_8)
                .build();
    }

    @ExceptionHandler
    public ResponseEntity<AccountAttachmentErrorResponseDto> handleException(final CuResponseStatusException e) {
        LOG.error("handleException, Encountered CuResponseStatusException during account attachment operation", e);
        final AccountAttachmentErrorResponseDto errorResponse = (AccountAttachmentErrorResponseDto) e.getStatusInfo();
        return ResponseEntity.status(e.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<AccountAttachmentErrorResponseDto> handleException(final ResponseStatusException e) {
        if (e.getStatus() == HttpStatus.NOT_FOUND && StringUtils.isNotBlank(e.getReason())) {
            LOG.warn("handleException, Operation failed due to nonexistent data: {}", e.getReason());
        } else {
            LOG.error("handleException, Encountered ResponseStatusException during account attachment operation", e);
        }
        final List<String> errorMessages = StringUtils.isNotBlank(e.getReason()) ? List.of(e.getReason()) : List.of();
        return createErrorResponse(e.getStatus(), errorMessages);
    }

    @ExceptionHandler
    public ResponseEntity<AccountAttachmentErrorResponseDto> handleException(
            final MissingServletRequestParameterException e) {
        LOG.error("handleException, Operation failed due to one or more missing request parameters", e);
        final List<String> errors = List.of("Required parameter is missing: " + e.getParameterName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler
    public ResponseEntity<AccountAttachmentErrorResponseDto> handleException(final Exception e) {
        LOG.error("handleException, Encountered unexpected exception during account attachment operation", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Internal Server Error"));
    }

    private ResponseEntity<AccountAttachmentErrorResponseDto> createErrorResponse(
            final HttpStatus status, final List<String> errorMessages) {
        final AccountAttachmentErrorResponseDto errorResponse = new AccountAttachmentErrorResponseDto();
        errorResponse.setErrors(errorMessages);
        return ResponseEntity.status(status)
                .body(errorResponse);
    }

}
