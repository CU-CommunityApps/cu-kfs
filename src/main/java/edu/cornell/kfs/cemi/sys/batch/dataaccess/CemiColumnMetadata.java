package edu.cornell.kfs.cemi.sys.batch.dataaccess;

import java.io.Serializable;
import java.sql.Types;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public final class CemiColumnMetadata implements Serializable {

    private static final long serialVersionUID = 3152642457753163796L;

    private final String columnName;
    private final String dtoFieldName;
    private final String staticValue;
    private final int jdbcType;
    private final boolean encrypted;
    private final boolean includedInFileOutput;
    private final boolean representsStaticValue;

    public CemiColumnMetadata(final String columnName, final String dtoFieldName, final int jdbcType,
            final boolean encrypted, final boolean includedInFileOutput) {
        Validate.isTrue(CemiUtils.isNameFormattedForUseAsDbIdentifier(columnName), "columnName is blank or malformed");
        Validate.notBlank(dtoFieldName, "dtoFieldName cannot be blank");
        this.columnName = columnName;
        this.dtoFieldName = dtoFieldName;
        this.staticValue = null;
        this.jdbcType = jdbcType;
        this.encrypted = encrypted;
        this.includedInFileOutput = includedInFileOutput;
        this.representsStaticValue = false;
    }

    public CemiColumnMetadata(final String columnName, final String staticValue) {
        Validate.isTrue(CemiUtils.isNameFormattedForUseAsDbIdentifier(columnName), "columnName is blank or malformed");
        Validate.notNull(staticValue, "staticValue cannot be null");
        this.columnName = columnName;
        this.dtoFieldName = null;
        this.staticValue = staticValue;
        this.jdbcType = Types.VARCHAR;
        this.encrypted = false;
        this.includedInFileOutput = true;
        this.representsStaticValue = true;
    }

    public String getDtoFieldName() {
        return dtoFieldName;
    }

    public String getStaticValue() {
        return staticValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isIncludedInFileOutput() {
        return includedInFileOutput;
    }

    public boolean isRepresentsStaticValue() {
        return representsStaticValue;
    }

}
