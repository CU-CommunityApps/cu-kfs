package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.cornell.kfs.module.purap.rest.jsonOnjects.fixture.PaymentRequestDtoFixture;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.fixture.PaymentRequestResultsDtoFixture;
import edu.cornell.kfs.sys.typeadapters.KualiDecimalTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PaymentRequestDtoTest {

    private static final Logger LOG = LogManager.getLogger();

    private static Gson buildGson() {
        return new GsonBuilder()
                .setDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT)
                .registerTypeAdapter(KualiDecimal.class, new KualiDecimalTypeAdapter())
                .create();
    }

    @ParameterizedTest
    @EnumSource
    public void testReadJsonToPaymentRequestDto(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException {
        Gson gson = buildGson();

        String actualJsonString = readFileToString(paymentRequestDtoFixture.jsonFileName);
        PaymentRequestDto actualDto = gson.fromJson(actualJsonString, PaymentRequestDto.class);
        PaymentRequestDto exptectedDto = paymentRequestDtoFixture.toPaymentRequestDto();

        LOG.debug("testReadJsonToPaymentRequestDto, actualDto DTO: {}", actualDto.toString());
        LOG.debug("testReadJsonToPaymentRequestDto, expected DTO: {}", exptectedDto.toString());

        assertEquals(exptectedDto, actualDto);
    }

    @ParameterizedTest
    @EnumSource
    public void testWritePaymentRequestDtoToJson(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException {
        Gson gson = buildGson();

        String expectedJsonString = readFileToString(paymentRequestDtoFixture.jsonFileName);
        JsonElement expectedJsonObject = JsonParser.parseString(expectedJsonString);

        String actualJsonString = gson.toJson(paymentRequestDtoFixture.toPaymentRequestDto());
        JsonElement actualJsonObject = JsonParser.parseString(actualJsonString);

        assertEquals(expectedJsonObject, actualJsonObject);
    }

    @ParameterizedTest
    @EnumSource
    public void testReadJsonToPaymentRequestResultsDto(PaymentRequestResultsDtoFixture paymentRequestResultsDtoFixture) throws IOException {
        Gson gson = buildGson();

        String actualJsonString = readFileToString(paymentRequestResultsDtoFixture.jsonFileName);
        PaymentRequestResultsDto actualDto = gson.fromJson(actualJsonString, PaymentRequestResultsDto.class);
        PaymentRequestResultsDto exptectedDto = paymentRequestResultsDtoFixture.toPaymentRequestResultsDto();

        LOG.debug("testReadJsonToPaymentRequestResultsDto, actualDto DTO: {}", actualDto.toString());
        LOG.debug("testReadJsonToPaymentRequestResultsDto, expected DTO: {}", exptectedDto.toString());

        assertEquals(exptectedDto, actualDto);
    }

    @ParameterizedTest
    @EnumSource
    public void testWritePaymentRequestResultsDtoToJson(PaymentRequestResultsDtoFixture paymentRequestResultsDtoFixture) throws IOException {
        Gson gson = buildGson();

        String expectedJsonString = readFileToString(paymentRequestResultsDtoFixture.jsonFileName);
        JsonElement expectedJsonObject = JsonParser.parseString(expectedJsonString);

        String actualJsonString = gson.toJson(paymentRequestResultsDtoFixture.toPaymentRequestResultsDto());
        JsonElement actualJsonObject = JsonParser.parseString(actualJsonString);

        assertEquals(expectedJsonObject, actualJsonObject);
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
