package edu.cornell.kfs.module.purap.rest.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
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
import edu.cornell.kfs.sys.typeadapters.LocalDateTypeAdapter;

import java.io.InputStream;
import java.time.LocalDate;
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
    private static final String CANCEL_PREQ_ANNOTATION_MESSAGE = "Canceled with the payment request endpoint";
    private static Gson gson = new GsonBuilder()
            .setDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT)
            .registerTypeAdapter(KualiDecimal.class, new KualiDecimalTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT))
            .create();

    /*
    This can be remvoed after payflow is fully implemented.  
    This should be false when pushed to develop / master.
    Note: when we implement routing of payment request documents, if there is an error on submission, we will need to cancel the preq
     */
    private static final boolean AUTO_CANCEL_PO_FOR_TESTING = false;

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    private PaymentRequestDtoValidationService paymentRequestDtoValidationService;
    private ApiAuthenticationService apiAuthenticationService;
    private CuPaymentRequestService cuPaymentRequestService;
    private DocumentService documentService;

    @GET
    public Response describePaymentRequestResource() {
        return Response.ok(CUPurapConstants.PAYMENT_REQUEST_ENDPOINT_DESCRIPTION).build();
    }

    @Path("/createpaymentrequest")
    @POST
    public Response createPaymentRequestDocument(InputStream requestBodyStream) {
        LOG.info("createPaymentRequestDocument, entering");
        try (Scanner scanner = new Scanner(requestBodyStream, CUKFSConstants.CHAR_SET_UTF_8)
                .useDelimiter(CUKFSConstants.FORM_DELIMITER_PATTERN);) {
            String jsonBody = scanner.hasNext() ? scanner.next() : "";
            PaymentRequestDto paymentRequestDto = gson.fromJson(jsonBody, PaymentRequestDto.class);

            String loggedInPrincipalName = getApiAuthenticationService().getAuthenticateUser(servletRequest);
            LOG.info("createPaymentRequestDocument, logged in user: {} and the paymentRequestDto: {}", loggedInPrincipalName,
                    paymentRequestDto);

            PaymentRequestResultsDto results = getPaymentRequestDtoValidationService()
                    .validatePaymentRequestDto(paymentRequestDto);
            if (results.isValid()) {
                try {
                    UserSession userSession = createUserSessionForAiPaymentRequestUser(loggedInPrincipalName);

                    PaymentRequestDocument preqDoc = GlobalVariables.doInNewGlobalVariables(userSession,
                            () -> getCuPaymentRequestService().createPaymentRequestDocumentFromDto(paymentRequestDto, results));

                    LOG.info("createPaymentRequestDocument, PREQ Document #{} Created", preqDoc.getDocumentNumber());
                    results.getSuccessMessages().add(String.format("Created PREQ Document %s", preqDoc.getDocumentNumber()));

                    results.setDocumentNumber(preqDoc.getDocumentNumber());

                    if (AUTO_CANCEL_PO_FOR_TESTING) {
                        /* Note we will want to cancel the saved preq if we can't submit the preq due to further business rule errors
                         * Adding this code now to help during the testing process
                         */
                        cancelPreq(userSession, preqDoc);
                    }

                    return Response.ok(gson.toJson(results)).build();

                } catch (Exception e) {
                    LOG.error("createPaymentRequestDocument, Unexpected error occurred while creating PREQ Document", e);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(results)).build();
                }

            } else {
                LOG.info("createPaymentRequestDocument, there were validation errors, return false {}", results);
                return Response.status(Status.BAD_REQUEST).entity(gson.toJson(results)).build();
            }

        } catch (Exception e) {
            LOG.error("createPaymentRequestDocument, an error occurred", e);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(CUKFSConstants.INTERNAL_SERVER_ERROR).build();
        }
    }

    private UserSession createUserSessionForAiPaymentRequestUser(String loggedInPrincipalName) throws Exception {
        try {

            UserSession userSession = new UserSession(loggedInPrincipalName);
            LOG.info("createPaymentRequestDocument userSession created for user {}, name {}",
                    loggedInPrincipalName, userSession.getPrincipalName());
            return userSession;

        } catch (Exception e) {
            LOG.error("createPaymentRequestDocument, Unexpected error occurred while creating PREQ Document Session for user {}",
                    loggedInPrincipalName, e);
            throw e;
        }
    }

    private void cancelPreq(final UserSession userSession, PaymentRequestDocument preqDoc) throws Exception {
        GlobalVariables.doInNewGlobalVariables(userSession,
                            () -> getDocumentService().cancelDocument(preqDoc, CANCEL_PREQ_ANNOTATION_MESSAGE));
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

    private CuPaymentRequestService getCuPaymentRequestService() {
        if (cuPaymentRequestService == null) {
            cuPaymentRequestService = SpringContext.getBean(CuPaymentRequestService.class);
        }
        return cuPaymentRequestService;
    }

    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

}
