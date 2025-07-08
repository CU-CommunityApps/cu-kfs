package edu.cornell.kfs.concur.rest.resource;

import java.text.ParseException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import jakarta.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurEventNotificationDTO;
import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurStandaloneEventNotificationDTO;
import edu.cornell.kfs.concur.service.ConcurEventNotificationConversionService;
import edu.cornell.kfs.concur.service.ConcurEventNotificationService;
import edu.cornell.kfs.sys.service.CUMarshalService;

@Path("/validate")
public class ConcurAccountValidationResource {
	private static final Logger LOG = LogManager.getLogger(ConcurAccountValidationResource.class);
    protected static volatile ConcurEventNotificationConversionService concurEventNotificationConversionService;
    protected static volatile ConcurEventNotificationService concurEventNotificationService;
    protected static volatile CUMarshalService cuMarshalService;
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response validate(String concurEventNotificationXml) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validate(): Validating the following event notification:\n" + concurEventNotificationXml);
        }
        ConcurEventNotification concurEventNotification = null;
        try {
            ConcurEventNotificationDTO concurEventNotificationDTO = getCuMarshalService().unmarshalString(
                    concurEventNotificationXml, ConcurStandaloneEventNotificationDTO.class);
            concurEventNotification = getConcurEventNotificationConversionService().convertConcurEventNotification(concurEventNotificationDTO);
        } catch (ParseException | JAXBException e) {
            LOG.error("validate():" + e.getMessage(), e);
            throw new BadRequestException();
        }
        getConcurEventNotificationService().saveConcurEventNotification(concurEventNotification);
        if (LOG.isDebugEnabled()) {
            LOG.debug("validate(): Completed validation of event notification for object: "
                    + concurEventNotification.getObjectURI());
        }
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

    public static CUMarshalService getCuMarshalService() {
        if (cuMarshalService == null) {
            cuMarshalService = SpringContext.getBean(CUMarshalService.class);
        }
        return cuMarshalService;
    }

}
