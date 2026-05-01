package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableInt;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants.CemiFieldDefinitionType;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class CemiTableMetadata implements Serializable {

    private static final long serialVersionUID = -1712749183973043827L;

    public static final String CACHE_NAME = "CemiTableMetadataCache";

    private final String sheetName;
    private final String tableName;
    private final List<CemiColumnMetadata> columns;

    public CemiTableMetadata(final String sheetName, final String tableName,
            final List<CemiColumnMetadata> columns) {
        Validate.isTrue(CemiUtils.isFormattedAsValidIdentifier(sheetName), "sheetName is blank or malformed");
        Validate.isTrue(CemiUtils.isFormattedAsValidIdentifier(tableName), "tableName is blank or malformed");
        Validate.isTrue(CollectionUtils.isNotEmpty(columns), "columns list cannot be null or empty");
        this.sheetName = sheetName;
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
    }

    public static CemiTableMetadata of(final String sheetNamePrefix, final CemiSheetDefinition sheet) {
        final String tableName = CemiUtils.formatSheetTableName(sheetNamePrefix, sheet.getName());
        final List<CemiColumnMetadata> columns = new ArrayList<>();
        final Map<String, MutableInt> columnNameCounts = new HashMap<>();

        for (final CemiFieldDefinition field : sheet.getFields()) {
            final CemiFieldDefinitionType fieldType = field.getType();
            final String baseColumnName = CemiUtils.formatSheetColumnName(field.getName());
            final MutableInt columnNameCount = columnNameCounts.computeIfAbsent(baseColumnName, name -> new MutableInt());
            columnNameCount.increment();

            final String finalColumnName;
            if (columnNameCount.intValue() > 1) {
                finalColumnName = StringUtils.join(baseColumnName, CUKFSConstants.UNDERSCORE, columnNameCount.toString());
            } else {
                finalColumnName = baseColumnName;
            }

            final CemiColumnMetadata columnMetadata;
            if (fieldType == CemiFieldDefinitionType.STATIC) {
                columnMetadata = new CemiColumnMetadata(finalColumnName, field.getValue());
            } else {
                final String dtoFieldName = field.getKey();
                final boolean encrypted = (fieldType == CemiFieldDefinitionType.STRING_ENCRYPTED);
                columnMetadata = new CemiColumnMetadata(finalColumnName, dtoFieldName, fieldType.jdbcType, encrypted,
                        fieldType.includedInFileOutput);
            }

            columns.add(columnMetadata);
        }

        return new CemiTableMetadata(sheet.getName(), tableName, columns);
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<CemiColumnMetadata> getColumns() {
        return columns;
    }

}
