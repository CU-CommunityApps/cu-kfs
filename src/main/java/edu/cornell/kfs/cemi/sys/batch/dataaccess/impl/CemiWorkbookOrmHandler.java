package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetOrmMetadata;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public class CemiWorkbookOrmHandler {

    private final Map<String, CemiSheetOrmMetadata> sheetsByName;
    private final Map<Class<?>, CemiSheetOrmMetadata> sheetsByDtoClass;
    private final BusinessObjectService businessObjectService;
    private final CemiSheetDao cemiSheetDao;

    public CemiWorkbookOrmHandler(final CemiOutputDefinition outputDefinition,
            final BusinessObjectService businessObjectService, final CemiSheetDao cemiSheetDao) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(businessObjectService, "businessObjectService cannot be null");
        Validate.notNull(cemiSheetDao, "cemiSheetDao cannot be null");

        final CemiSheetOrmMetadata[] sheetMetadata = outputDefinition.getSheets().stream()
                .map(CemiSheetOrmMetadata::new)
                .toArray(CemiSheetOrmMetadata[]::new);

        this.sheetsByName = Arrays.stream(sheetMetadata)
                .collect(Collectors.toUnmodifiableMap(CemiSheetOrmMetadata::getSheetName, Function.identity()));
        this.sheetsByDtoClass = Arrays.stream(sheetMetadata)
                .collect(Collectors.toUnmodifiableMap(CemiSheetOrmMetadata::getDtoClass, Function.identity()));
        this.businessObjectService = businessObjectService;
        this.cemiSheetDao = cemiSheetDao;
    }

    public void storeSheetRow(final Object sheetRowDto, final String jobRunDate, final Long rowIndex) {
        Validate.notNull(sheetRowDto, "sheetRowDto cannot be null");
        Validate.notBlank(jobRunDate, "jobRunDate cannot be blank");
        Validate.notNull(rowIndex, "rowIndex cannot be null");
        final PersistableBusinessObject sheetRowBo = prepareSheetRowBo(sheetRowDto, jobRunDate, rowIndex);
        businessObjectService.save(sheetRowBo);
    }

    private PersistableBusinessObject prepareSheetRowBo(final Object sheetRowDto, final String jobRunDate,
            final Long rowIndex) {
        final Class<?> dtoClass = sheetRowDto.getClass();
        final CemiSheetOrmMetadata sheetMetadata = sheetsByDtoClass.get(dtoClass);
        Validate.validState(sheetMetadata != null, "No mapping found for DTO: %s", dtoClass.getName());

        final PersistableBusinessObject sheetRowBo;
        if (sheetMetadata.isUseDataTransferObjectAsBusinessObject()) {
            sheetRowBo = (PersistableBusinessObject) sheetRowDto;
        } else {
            try {
                final Class<? extends PersistableBusinessObject> boClass = sheetMetadata.getBoClass();
                sheetRowBo = boClass.getDeclaredConstructor().newInstance();
                final BeanWrapper wrappedDto = PropertyAccessorFactory.forBeanPropertyAccess(sheetRowDto); 
                final BeanWrapper wrappedBo = PropertyAccessorFactory.forBeanPropertyAccess(sheetRowBo);
                for (final Pair<String, String> fieldMapping: sheetMetadata.getFieldMappings()) {
                    final Object fieldValue = wrappedDto.getPropertyValue(fieldMapping.getLeft());
                    wrappedBo.setPropertyValue(fieldMapping.getRight(), fieldValue);
                }
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (sheetRowBo instanceof CemiIndexedBusinessObjectBase) {
            final CemiIndexedBusinessObjectBase indexedBo = (CemiIndexedBusinessObjectBase) sheetRowBo;
            indexedBo.setJobRunDate(jobRunDate);
            indexedBo.setRowIndex(rowIndex);
        }

        return sheetRowBo;
    }

    public Stream<String[]> getSheetRowDataForPrinting(final String sheetName, final String jobRunDate) {
        Validate.notBlank(sheetName, "sheetName cannot be blank");
        Validate.notBlank(jobRunDate, "jobRunDate cannot be blank");
        final CemiSheetOrmMetadata sheetMetadata = sheetsByName.get(sheetName);
        Validate.validState(sheetMetadata != null, "No mapping found for sheet name: %s", sheetName);
        Validate.validState(CemiIndexedBusinessObjectBase.class.isAssignableFrom(sheetMetadata.getBoClass()),
                "BO class %s for sheet %s should have been an instance of CemiIndexedBusinessObjectBase. "
                        + "Only sub-types of CemiIndexedBusinessObjectBase can be handled by this method.",
                        sheetMetadata.getBoClass().getName(), sheetName);
        return cemiSheetDao.getSheetRowDataForPrinting(sheetMetadata, jobRunDate);
    }

}
