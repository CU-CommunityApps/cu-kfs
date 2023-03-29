package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerResponseMessageTypes;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerXmlConstants;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.DocumentExportResponse;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.Error;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.Header;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.Response;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.Status;
import edu.cornell.kfs.sys.xmladapters.ZonedStringToJavaDateXmlAdapter;

public enum DocumentExportResponseFixture {

    RESPONSE_200_OK("2023-03-16T22:49:33.789Z", HttpStatus.OK),
    RESPONSE_201_CREATED("2023-03-17T22:51:33.789Z", HttpStatus.CREATED,
            warning("The contract's account data failed validation and has been excluded from the REQS document.")),
    RESPONSE_400_BAD_REQUEST("2023-03-18T19:51:33.789Z", HttpStatus.BAD_REQUEST,
            error("Contract Number is a required field."),
            error("Contents for attachment with filename 'test1.txt' were not encoded properly.")),
    RESPONSE_403_FORBIDDEN("2023-03-15T15:51:33.789Z", HttpStatus.FORBIDDEN,
            error("You are not authorized to export Contract XML to this environment.")),
    RESPONSE_500_INTERNAL_SERVER_ERROR("2023-02-15T23:51:33.789Z", HttpStatus.INTERNAL_SERVER_ERROR,
            error("An unexpected error occurred while processing the Contract XML."));

    public final String messageIdAsTimestamp;
    public final HttpStatus httpStatus;
    public final List<Pair<String, String>> messages;

    @SafeVarargs
    private DocumentExportResponseFixture(String messageIdAsTimestamp, HttpStatus httpStatus,
            Pair<String, String>... messages) {
        this.messageIdAsTimestamp = messageIdAsTimestamp;
        this.httpStatus = httpStatus;
        this.messages = List.of(messages);
    }

    public DocumentExportResponse toDocumentExportResponse() {
        DocumentExportResponse exportResponse = new DocumentExportResponse();
        exportResponse.setVersion(JaggaerXmlConstants.DEFAULT_MESSAGE_VERSION);
        exportResponse.setHeader(buildHeaderDTO());
        exportResponse.setResponse(buildResponseDTO());
        return exportResponse;
    }

    private Header buildHeaderDTO() {
        Header header = new Header();
        header.setMessageId(messageIdAsTimestamp);
        header.setTimestamp(ZonedStringToJavaDateXmlAdapter.parseToDate(messageIdAsTimestamp));
        return header;
    }

    private Response buildResponseDTO() {
        Response response = new Response();
        
        Status status = new Status();
        status.setStatusCode(httpStatus.value());
        status.setStatusText(httpStatus.getReasonPhrase());
        response.setStatus(status);
        
        List<Error> errors = messages.stream()
                .map(this::buildErrorDTO)
                .collect(Collectors.toUnmodifiableList());
        response.setErrors(errors);
        
        return response;
    }

    private Error buildErrorDTO(Pair<String, String> message) {
        Error error = new Error();
        error.setType(message.getLeft());
        error.setErrorMessage(message.getRight());
        return error;
    }

    /*
     * The following methods are only meant for simplifying the setup of the enum constants.
     */

    public static Pair<String, String> error(String message) {
        return Pair.of(JaggaerResponseMessageTypes.ERROR, message);
    }

    public static Pair<String, String> warning(String message) {
        return Pair.of(JaggaerResponseMessageTypes.WARNING, message);
    }

}
