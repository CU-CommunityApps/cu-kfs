package edu.cornell.kfs.module.purap.rest.resource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.service.PaymentRequestDtoValidationService;
import edu.cornell.kfs.module.purap.service.impl.PaymentRequestDtoValidationServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentRequestResource {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson gson = new Gson();

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    private PaymentRequestDtoValidationService paymentRequestDtoValidationService;

    @GET
    public Response describePaymentRequestResource() {
        return Response.ok(CUPurapConstants.PAYMENT_REQUEST_ENDPOINT_DESCRIPTION).build();
    }

    // New POST endpoint for creating PaymentRequestDocument
    @Path("payment-request")
    @javax.ws.rs.POST
    @javax.ws.rs.Consumes(MediaType.APPLICATION_JSON)
    public Response createPaymentRequestDocument(PaymentRequestDto paymentRequestDto) {
        LOG.info("Received PaymentRequestDto: {}", paymentRequestDto);

        // TODO: You may need to inject/set necessary services on validationService, if not autowired.
        PaymentRequestResultsDto results = getPaymentRequestDtoValidationService().validatePaymentRequestDto(paymentRequestDto);

        return Response.ok(results).build();
    }

    private PaymentRequestDtoValidationService getPaymentRequestDtoValidationService() {
        if (paymentRequestDtoValidationService == null) {
            paymentRequestDtoValidationService = SpringContext.getBean(PaymentRequestDtoValidationService.class);
        }
        return paymentRequestDtoValidationService;
    }
}
