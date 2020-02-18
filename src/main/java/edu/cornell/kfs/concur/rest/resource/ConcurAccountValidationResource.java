package edu.cornell.kfs.concur.rest.resource;

import java.text.ParseException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.eventnotification.rest.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.service.ConcurEventNotificationConversionService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;

@Path("/validate")
public class ConcurAccountValidationResource {
	private static final Logger LOG = LogManager.getLogger(ConcurAccountValidationResource.class);
    protected static volatile ConcurEventNotificationConversionService concurEventNotificationConversionService;
    protected static volatile ConcurEventNotificationService concurEventNotificationService;
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response validate(ConcurEventNotificationDTO concurEventNotificationDTO) {
        ConcurEventNotification concurEventNotification = null;
        try {
            concurEventNotification = getConcurEventNotificationConversionService().convertConcurEventNotification(concurEventNotificationDTO);
        } catch (ParseException e) {
            LOG.error("validate():" + e.getMessage(), e);
            throw new BadRequestException();
        }
        getConcurEventNotificationService().saveConcurEventNotification(concurEventNotification);
        return Response.ok().build();
    }    

    public ConcurEventNotificationService getConcurEventNotificationService() {
        if (concurEventNotificationService == null) {
            concurEventNotificationService = SpringContext.getBean(ConcurEventNotificationService.class);
        }
        return concurEventNotificationService;
    }

    public static ConcurEventNotificationConversionService getConcurEventNotificationConversionService() {
        if (concurEventNotificationConversionService == null) {
            concurEventNotificationConversionService = SpringContext.getBean(ConcurEventNotificationConversionService.class);
        }
        return concurEventNotificationConversionService;
    }

}
