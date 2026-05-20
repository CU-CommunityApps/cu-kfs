package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.dto.CemiRemitToSupplier;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierDataBuilder;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierDao;

public abstract class CemiRemitToSupplierDataBuilderBase implements CemiRemitToSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;
    protected final boolean maskSensitiveData;
   
    protected CemiRemitToSupplierDao cemiRemitTosupplierDao;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    
    protected CemiRemitToSupplierDataBuilderBase(final CemiOutputDefinition outputDefinition,
            CemiRemitToSupplierDao cemiRemitTosupplierDao, DateTimeService dateTimeService, BusinessObjectService businessObjectService, 
            final LocalDateTime jobRunDate, boolean maskSensitiveData) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(cemiRemitTosupplierDao, "CemiRemitToSupplierDao cannot be null");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(businessObjectService, "businessObjectService cannot be null");
        Validate.notNull(jobRunDate, "jobRunDate cannot be null");
        this.outputDefinition = outputDefinition;
        this.cemiRemitTosupplierDao = cemiRemitTosupplierDao;
        this.dateTimeService = dateTimeService;
        this.businessObjectService = businessObjectService;
        this.jobRunDate = jobRunDate;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeRemitToSupplierDataToIntermediateStorage(Iterator<CemiSupplierBo> suppliers,
            LocalDateTime jobRunDate) throws IOException {
        // TODO Auto-generated method stub
    }
    
    
    protected void writeRemitToSupplierRowToFiles(final CemiRemitToSupplier remitToSupplier) throws IOException {
        writeDataToIntermediateStorage(CemiRemitToSupplierConstants.RemitToSupplierExtractSheets.REMIT_TO_SUPPLIER, remitToSupplier);
    }

    
    /*
     * The subclass that writes the remit to supplier data to the temp tables needs to implement this method.
     * If desired, the implementation can keep connections/files/etc. open until close() is called.
     * See the CSV implementation for an example.
     */
    protected abstract void writeDataToIntermediateStorage(
            final String sheetName, final Object rowObject) throws IOException;
    
    // The temp table implementation can use (or override) this method to retrieve the column value to be inserted.
    protected String getFieldValue(final CemiFieldDefinition field, final Object rowObject) {
        switch (field.getType()) {
            case STATIC:
                return field.getValue();

            case STRING:
                return (String) ObjectUtils.getPropertyValue(rowObject, field.getKey());

            default:
                throw new IllegalStateException("Unknown field type: " + field.getType());
        }
    }

}
