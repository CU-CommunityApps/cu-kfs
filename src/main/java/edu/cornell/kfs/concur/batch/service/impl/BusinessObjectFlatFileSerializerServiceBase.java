package edu.cornell.kfs.concur.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.concur.batch.businessobject.BusinessObjectFlatFileSerializerFieldUtils;
import edu.cornell.kfs.concur.batch.service.BusinessObjectFlatFileSerializerService;

/**
 * Basic implementation of BusinessObjectFlatFileSerializerService that allows for creating a file
 * from line item objects, an optional header object, and an optional footer object.
 * Subclasses must implement the appropriate methods so that the header, line items and footer
 * can be retrieved from the main encapsulating object accordingly.
 * 
 * This implementation only supports using a single field utility for all of the main line items.
 * Separate field utilities can be used for the header and footer lines.
 */
@SuppressWarnings("deprecation")
public abstract class BusinessObjectFlatFileSerializerServiceBase implements BusinessObjectFlatFileSerializerService {

	private static final Logger LOG = LogManager.getLogger(BusinessObjectFlatFileSerializerServiceBase.class);

    protected BusinessObjectFlatFileSerializerFieldUtils headerSerializerUtils;
    protected BusinessObjectFlatFileSerializerFieldUtils lineItemSerializerUtils;
    protected BusinessObjectFlatFileSerializerFieldUtils footerSerializerUtils;

    @Override
    public boolean serializeToFlatFile(String fullyQualifiedFileName, BusinessObject objectToSerialize) {
        if (StringUtils.isBlank(fullyQualifiedFileName)) {
            throw new IllegalArgumentException("fullyQualifiedFileName cannot be blank");
        } else if (ObjectUtils.isNull(objectToSerialize)) {
            throw new IllegalArgumentException("objectToSerialize cannot be null");
        }
        
        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        
        try {
            fileWriter = new FileWriter(fullyQualifiedFileName);
            writer = new BufferedWriter(fileWriter);
            writeToFile(writer, objectToSerialize);
        } catch (Exception e) {
            LOG.error("Could not completely write business object to file: " + fullyQualifiedFileName, e);
            return false;
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(fileWriter);
        }
        
        return true;
    }

    protected void writeToFile(BufferedWriter writer, BusinessObject objectToSerialize) throws IOException {
        BusinessObject header = getHeader(objectToSerialize);
        List<? extends BusinessObject> lineItems = getLineItems(objectToSerialize);
        BusinessObject footer = getFooter(objectToSerialize);
        
        if (ObjectUtils.isNotNull(header)) {
            writeLines(writer, headerSerializerUtils, Collections.singletonList(header));
        }
        
        writeLines(writer, lineItemSerializerUtils, lineItems);
        
        if (ObjectUtils.isNotNull(footer)) {
            writeLines(writer, footerSerializerUtils, Collections.singletonList(footer));
        }
        
        writer.flush();
    }

    protected void writeLines(BufferedWriter writer, BusinessObjectFlatFileSerializerFieldUtils serializerUtils,
            List<? extends BusinessObject> lineObjects) throws IOException {
        for (BusinessObject lineObject : lineObjects) {
            String line = serializerUtils.serializeBusinessObject(lineObject);
            writer.write(line);
            writer.write(KFSConstants.NEWLINE);
        }
    }

    /**
     * Returns the object that should be serialized to create the file's header line.
     * 
     * @param objectToSerialize The object that was passed to the serializeToFlatFile() method.
     * @return The object representing the header, or null if no header line should be written.
     */
    protected abstract BusinessObject getHeader(BusinessObject objectToSerialize);

    /**
     * Returns the objects that should be serialized to create the file's line items.
     * 
     * @param objectToSerialize The object that was passed to the serializeToFlatFile() method.
     * @return A List of all the line item objects to be serialized.
     */
    protected abstract List<? extends BusinessObject> getLineItems(BusinessObject objectToSerialize);

    /**
     * Returns the object that should be serialized to create the file's footer/trailer line.
     * 
     * @param objectToSerialize The object that was passed to the serializeToFlatFile() method.
     * @return The object representing the footer, or null if no footer line should be written.
     */
    protected abstract BusinessObject getFooter(BusinessObject objectToSerialize);

    public void setHeaderSerializerUtils(BusinessObjectFlatFileSerializerFieldUtils headerSerializerUtils) {
        this.headerSerializerUtils = headerSerializerUtils;
    }

    public void setLineItemSerializerUtils(BusinessObjectFlatFileSerializerFieldUtils lineItemSerializerUtils) {
        this.lineItemSerializerUtils = lineItemSerializerUtils;
    }

    public void setFooterSerializerUtils(BusinessObjectFlatFileSerializerFieldUtils footerSerializerUtils) {
        this.footerSerializerUtils = footerSerializerUtils;
    }

}
