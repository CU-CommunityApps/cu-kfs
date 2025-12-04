package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;

public enum PaymentRequestResultsDtoFixture {
    VALID_NO_MESSAGES(
            "/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestResults_valid_no_messages.json",
            true, buildStringList(), buildStringList()),
    VALID_SUCCESS_MESSAGE(
            "/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestResults_valid_success_message.json",
            true, buildStringList("success message 1"), buildStringList()),
    INVALID_SUCCESS_AND_ERROR_MESSAGES(
            "/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestResults_invalid_succes_and_error_messages.json",
            false, buildStringList("success message 1", "success message 2"),
            buildStringList("error message 1", "error message 2")),
    INVALID_ERROR_MESSAGE(
            "/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestResults_invalid_error_message.json",
            false, buildStringList(), buildStringList("error message 1"));

    public final String jsonFileName;
    public final boolean valid;
    public final List<String> successMessages;
    public final List<String> errorMessages;

    private PaymentRequestResultsDtoFixture(String jsonFileName, boolean valid, List<String> successMessages,
            List<String> errorMessages) {
        this.jsonFileName = jsonFileName;
        this.valid = valid;
        this.successMessages = successMessages;
        this.errorMessages = errorMessages;
    }

    private static List<String> buildStringList(String... messages) {
        return Collections.unmodifiableList(Arrays.asList(messages));
    }

    public PaymentRequestResultsDto toPaymentRequestResultsDto() {
        PaymentRequestResultsDto dto = new PaymentRequestResultsDto();
        dto.setValid(valid);
        dto.setSuccessMessages(successMessages);
        dto.setErrorMessages(errorMessages);
        return dto;
    }

}
