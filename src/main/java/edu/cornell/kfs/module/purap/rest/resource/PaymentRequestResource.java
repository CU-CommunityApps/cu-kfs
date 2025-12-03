package edu.cornell.kfs.module.purap.rest.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.service.PaymentRequestDtoValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ApiAuthenticationService;
import edu.cornell.kfs.sys.typeadapters.KualiDecimalTypeAdapter;

import java.io.InputStream;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentRequestResource {
    private static final Logger LOG = LogManager.getLogger();
    private static Gson gson = new GsonBuilder()
            .setDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT)
            .registerTypeAdapter(KualiDecimal.class, new KualiDecimalTypeAdapter())
            .create();

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    private PaymentRequestDtoValidationService paymentRequestDtoValidationService;
    private ApiAuthenticationService apiAuthenticationService;

    @GET
    public Response describePaymentRequestResource() {
        return Response.ok(CUPurapConstants.PAYMENT_REQUEST_ENDPOINT_DESCRIPTION).build();
    }

    @Path("/createpaymentrequest")
    @POST
    public Response createPaymentRequestDocument(InputStream requestBodyStream) {
        try (Scanner scanner = new Scanner(requestBodyStream, CUKFSConstants.CHAR_SET_UTF_8)
                .useDelimiter(CUKFSConstants.FORM_DELIMITER_PATTERN);) {
            String jsonBody = scanner.hasNext() ? scanner.next() : "";
            PaymentRequestDto paymentRequestDto = gson.fromJson(jsonBody, PaymentRequestDto.class);

            String loggedInUser = getApiAuthenticationService().getAuthenticateUser(servletRequest);
            LOG.info("createPaymentRequestDocument, logged in user: {} and the paymentRequestDto: {}", loggedInUser,
                    paymentRequestDto);

            PaymentRequestResultsDto results = getPaymentRequestDtoValidationService()
                    .validatePaymentRequestDto(paymentRequestDto);
            if (results.isValid()) {
                results.getSuccessMessages().add("In follow up user stories, this endpoint will be updated" +
                        " to create a preq document using logged in user " + loggedInUser);
                return Response.ok(gson.toJson(results)).build();
            } else {
                return Response.status(Status.BAD_REQUEST).entity(gson.toJson(results)).build();
            }

        } catch (Exception e) {
            LOG.error("createPaymentRequestDocument, an error occurred", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(CUKFSConstants.INTERNAL_SERVER_ERROR).build();
        }
    }

    private PaymentRequestDtoValidationService getPaymentRequestDtoValidationService() {
        if (paymentRequestDtoValidationService == null) {
            paymentRequestDtoValidationService = SpringContext.getBean(PaymentRequestDtoValidationService.class);
        }
        return paymentRequestDtoValidationService;
    }

    private ApiAuthenticationService getApiAuthenticationService() {
        if (apiAuthenticationService == null) {
            apiAuthenticationService = SpringContext.getBean(ApiAuthenticationService.class);
        }
        return apiAuthenticationService;
    }
}
