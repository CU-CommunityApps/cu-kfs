package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.util.CellReference;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiSimpleSheetBusinessObjectBase;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;

public class CemiSheetOrmMetadata {

    private final String sheetName;
    private final Class<?> dtoClass;
    private final Class<? extends PersistableBusinessObject> boClass;
    private final boolean useDataTransferObjectAsBusinessObject;
    private final List<Pair<String, String>> fieldMappings;

    public CemiSheetOrmMetadata(final CemiSheetDefinition sheetDefinition) {
        Validate.notNull(sheetDefinition, "sheetDefinition cannot be null");
        Validate.notBlank(sheetDefinition.getDtoClassName(), "sheetDefinition does not define a DTO classname");

        this.sheetName = sheetDefinition.getName();
        this.dtoClass = ClassLoaderUtils.getClass(sheetDefinition.getDtoClassName());

        if (StringUtils.isBlank(sheetDefinition.getBusinessObjectClassName())
                || Strings.CS.equals(sheetDefinition.getDtoClassName(), sheetDefinition.getBusinessObjectClassName())) {
            this.boClass = dtoClass.asSubclass(PersistableBusinessObject.class);
            this.useDataTransferObjectAsBusinessObject = true;
        } else {
            try {
                this.boClass = ClassLoaderUtils.getClass(
                        sheetDefinition.getBusinessObjectClassName(), PersistableBusinessObject.class);
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            this.useDataTransferObjectAsBusinessObject = false;
        }

        final List<CemiFieldDefinition> fields = sheetDefinition.getFields();
        this.fieldMappings = IntStream.range(0, fields.size())
                .mapToObj(index -> createFieldMapping(fields.get(index), boClass, index))
                .collect(Collectors.toUnmodifiableList());
    }

    private static Pair<String, String> createFieldMapping(final CemiFieldDefinition fieldDefinition,
            final Class<? extends PersistableBusinessObject> boClass, final int index) {
        final String dtoFieldName = fieldDefinition.getDtoFieldName();
        final String boFieldName;
        if (CemiSimpleSheetBusinessObjectBase.class.isAssignableFrom(boClass)) {
            boFieldName = CemiBasePropertyConstants.COL_PREFIX + CellReference.convertNumToColString(index);
        } else {
            boFieldName = dtoFieldName;
        }
        return Pair.of(dtoFieldName, boFieldName);
    }

    public String getSheetName() {
        return sheetName;
    }

    public Class<?> getDtoClass() {
        return dtoClass;
    }

    public Class<? extends PersistableBusinessObject> getBoClass() {
        return boClass;
    }

    public boolean isUseDataTransferObjectAsBusinessObject() {
        return useDataTransferObjectAsBusinessObject;
    }

    public List<Pair<String, String>> getFieldMappings() {
        return fieldMappings;
    }

}
