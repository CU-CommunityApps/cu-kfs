package edu.cornell.kfs.tax.batch.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

/**
 * Base implementation class for TaxTableMetadataLookupService that contains most of the relevant
 * metadata derivation logic. Subclasses only have to implement a few abstract methods for retrieving
 * the relevant information from the implementation-specific metadata.
 * 
 * The "T" type represents whatever object type contains the metadata for a particular BO class.
 * 
 * Also, this base implementation will create derived table aliases as follows:
 * 
 * 1. Sort the related enum's mapped business object classes in classname order.
 * 2. Take the first 3 characters from each mapped class's simple name, uppercase them,
 *            and append a unique numeric suffix (starting from zero).
 */
public abstract class TaxTableMetadataLookupServiceBase<T> implements TaxTableMetadataLookupService {

    @Override
    public TaxDtoDbMetadata getDatabaseMappingMetadataForDto(
            final Class<? extends TaxDtoFieldEnum> dtoFieldEnumClass) {
        Validate.notNull(dtoFieldEnumClass, "dtoFieldEnumClass cannot be null");
        Validate.isTrue(dtoFieldEnumClass.isEnum(), "dtoFieldEnumClass must be an enum class");

        final List<Class<? extends BusinessObject>> mappedClasses = getAndSortMappedBusinessObjectClasses(
                dtoFieldEnumClass);
        final Map<Class<? extends BusinessObject>, T> metadataMappings = getMetadataForBusinessObjects(mappedClasses);
        final Map<Class<? extends BusinessObject>, String> tableNameMappings = getTableNameMappings(metadataMappings);
        final Map<Class<? extends BusinessObject>, String> tableAliasMappings = getTableAliasMappings(mappedClasses);
        final Map<TaxDtoFieldEnum, String> columnLabelMappings = getColumnLabelMappings(
                dtoFieldEnumClass, metadataMappings, tableAliasMappings);

        return new TaxDtoDbMetadata(tableNameMappings, tableAliasMappings, dtoFieldEnumClass, columnLabelMappings);
    }

    protected List<Class<? extends BusinessObject>> getAndSortMappedBusinessObjectClasses(
            final Class<? extends TaxDtoFieldEnum> dtoFieldEnumClass) {
        return Arrays.stream(dtoFieldEnumClass.getEnumConstants())
                .map(TaxDtoFieldEnum::getMappedBusinessObjectClass)
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toUnmodifiableList());
    }

    protected Map<Class<? extends BusinessObject>, T> getMetadataForBusinessObjects(
            final List<Class<? extends BusinessObject>> mappedClasses) {
        return mappedClasses.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(), this::getMetadataForBusinessObject));
    }

    protected abstract T getMetadataForBusinessObject(final Class<? extends BusinessObject> businessObjectClass);

    protected Map<Class<? extends BusinessObject>, String> getTableNameMappings(
            final Map<Class<? extends BusinessObject>, T> metadataMappings) {
        return metadataMappings.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, entry -> getQualifiedTableName(entry.getValue())));
    }

    protected String getQualifiedTableName(final T metadata) {
        final String simpleTableName = getTableName(metadata);
        return StringUtils.join(CUTaxBatchConstants.KFS_SCHEMA, KFSConstants.DELIMITER, simpleTableName);
    }

    protected abstract String getTableName(final T metadata);

    protected Map<Class<? extends BusinessObject>, String> getTableAliasMappings(
            final List<Class<? extends BusinessObject>> mappedClasses) {
        int numericSuffix = -1;
        final Stream.Builder<Pair<Class<? extends BusinessObject>, String>> tableAliases = Stream.builder();

        for (final Class<? extends BusinessObject> mappedClass : mappedClasses) {
            numericSuffix++;
            final String aliasPrefix = StringUtils.left(mappedClass.getSimpleName(), 3);
            final String alias = StringUtils.upperCase(aliasPrefix, Locale.US) + numericSuffix;
            tableAliases.add(Pair.of(mappedClass, alias));
        }

        return tableAliases.build()
                .collect(Collectors.toUnmodifiableMap(Pair::getLeft, Pair::getRight));
    }

    protected Map<TaxDtoFieldEnum, String> getColumnLabelMappings(
            final Class<? extends TaxDtoFieldEnum> dtoFieldEnumClass,
            final Map<Class<? extends BusinessObject>, T> metadataMappings,
            final Map<Class<? extends BusinessObject>, String> tableAliasMappings) {
        return Arrays.stream(dtoFieldEnumClass.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(),
                        fieldMapping -> getQualifiedColumnLabel(fieldMapping, metadataMappings, tableAliasMappings)));
    }

    protected String getQualifiedColumnLabel(final TaxDtoFieldEnum fieldMapping,
            final Map<Class<? extends BusinessObject>, T> metadataMappings,
            final Map<Class<? extends BusinessObject>, String> tableAliasMappings) {
        final Class<? extends BusinessObject> mappedClass =
                ((TaxDtoFieldEnum) fieldMapping).getMappedBusinessObjectClass();
        final T metadata = metadataMappings.get(mappedClass);
        final String tableAlias = tableAliasMappings.get(mappedClass);
        final String simpleColumnLabel = getColumnLabel(fieldMapping, metadata);
        return StringUtils.join(tableAlias, KFSConstants.DELIMITER, simpleColumnLabel);
    }

    protected abstract String getColumnLabel(final TaxDtoFieldEnum fieldMapping, final T metadata);

}
