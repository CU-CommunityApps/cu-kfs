package edu.cornell.kfs.module.purap.rest.jsonObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.cornell.kfs.module.purap.rest.jsonObjects.fixture.PaymentRequestDtoFixture;
import edu.cornell.kfs.module.purap.rest.jsonObjects.fixture.PaymentRequestResultsDtoFixture;
import edu.cornell.kfs.sys.typeadapters.KualiDecimalTypeAdapter;
import edu.cornell.kfs.sys.typeadapters.LocalDateTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class PaymentRequestDtoTest {
    private static final Logger LOG = LogManager.getLogger();
    private static Gson gson = new GsonBuilder()
            .setDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT)
            .registerTypeAdapter(KualiDecimal.class, new KualiDecimalTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT))
            .create();

    @ParameterizedTest
    @MethodSource("jsonParseFixtures")
    public void testReadJsonToPaymentRequestDto(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException {
        String actualJsonString = readFileToString(paymentRequestDtoFixture.jsonFileName);
        PaymentRequestDto actualDto = gson.fromJson(actualJsonString, PaymentRequestDto.class);
        PaymentRequestDto expectedDto = paymentRequestDtoFixture.toPaymentRequestDto();

        LOG.debug("testReadJsonToPaymentRequestDto, actualDto DTO: {}", actualDto.toString());
        LOG.debug("testReadJsonToPaymentRequestDto, expected DTO: {}", expectedDto.toString());

        Assertions.assertEquals(expectedDto, actualDto);
    }

    private static java.util.stream.Stream<PaymentRequestDtoFixture> jsonParseFixtures() {
        return java.util.Arrays.stream(PaymentRequestDtoFixture.values())
                .filter(dto -> dto.name().startsWith("JSON_PARSE_"));
    }

    @ParameterizedTest
    @MethodSource("jsonParseFixtures")
    public void testWritePaymentRequestDtoToJson(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException, JSONException {
        String expectedJsonString = readFileToString(paymentRequestDtoFixture.jsonFileName);
        String actualJsonString = gson.toJson(paymentRequestDtoFixture.toPaymentRequestDto());

        JSONAssert.assertEquals(expectedJsonString, actualJsonString, JSONCompareMode.STRICT);

    }

    @ParameterizedTest
    @EnumSource
    public void testReadJsonToPaymentRequestResultsDto(PaymentRequestResultsDtoFixture paymentRequestResultsDtoFixture)
            throws IOException {
        String actualJsonString = readFileToString(paymentRequestResultsDtoFixture.jsonFileName);
        PaymentRequestResultsDto actualDto = gson.fromJson(actualJsonString, PaymentRequestResultsDto.class);
        PaymentRequestResultsDto exptectedDto = paymentRequestResultsDtoFixture.toPaymentRequestResultsDto();

        LOG.debug("testReadJsonToPaymentRequestResultsDto, actualDto DTO: {}", actualDto.toString());
        LOG.debug("testReadJsonToPaymentRequestResultsDto, expected DTO: {}", exptectedDto.toString());

        Assertions.assertEquals(exptectedDto, actualDto);
    }

    @ParameterizedTest
    @EnumSource
    public void testWritePaymentRequestResultsDtoToJson(PaymentRequestResultsDtoFixture paymentRequestResultsDtoFixture)
            throws IOException {
        String expectedJsonString = readFileToString(paymentRequestResultsDtoFixture.jsonFileName);
        JsonElement expectedJsonObject = JsonParser.parseString(expectedJsonString);

        String actualJsonString = gson.toJson(paymentRequestResultsDtoFixture.toPaymentRequestResultsDto());
        JsonElement actualJsonObject = JsonParser.parseString(actualJsonString);

        Assertions.assertEquals(expectedJsonObject, actualJsonObject);
    }

    private String readFileToString(String fileName) throws IOException {
        try (java.io.InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            byte[] bytes = is.readAllBytes();
            String results = new String(bytes, StandardCharsets.UTF_8);
            LOG.debug("readFileToString file name {} read to string as {}", fileName, results);
            return results;
        }
    }
}
