package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.batch.TaxOutputField;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

abstract class SprintaxFieldDefinition {
    String name;

    public SprintaxFieldDefinition(String name) {
        this.name = name;
    }

    public static SprintaxFieldDefinition buildTransactionDetailFieldDefinition(TaxTableField detailField, String name) {
        SprintaxFieldDefinition recordPiece;

        switch (detailField.jdbcType) {
            case java.sql.Types.DECIMAL :
                recordPiece = new TransactionDetailBigDecimalFieldDefinition(name, detailField.index);
                break;

            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
                recordPiece = new TransactionDetailIntFieldDefinition(name, detailField.index);
                break;

            case java.sql.Types.VARCHAR :
                recordPiece = new TransactionDetailStringFieldDefinition(name, detailField.index);
                break;

            case java.sql.Types.DATE :
                recordPiece = new TransactionDetailDateFieldDefinition(name, detailField.index);
                break;

            default :
                throw new IllegalStateException("Unrecognized field datatype");
        }
        return recordPiece;
    }

    public static SprintaxFieldDefinition buildTransactionDetailFieldDefinition(TaxTableField detailField, TaxOutputField field) {
        String name = field.getName();
        if (name.equals("ssn")) {
            return new DerivedFieldDefinitionString("ssn");
        }

        return buildTransactionDetailFieldDefinition(detailField, name);
    }

    abstract String getValue(ResultSet rs) throws SQLException;
}

abstract class IndexedColumnFieldDefinition extends SprintaxFieldDefinition {
    int columnIndex;

    IndexedColumnFieldDefinition(String name, int columnIndex) {
        super(name);
        this.columnIndex = columnIndex;
    }
}

class StaticStringFieldDefinition extends SprintaxFieldDefinition {
    String value;

    StaticStringFieldDefinition(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    String getValue(ResultSet rs) throws SQLException {
        return value;
    }
}

class DerivedFieldDefinitionString extends SprintaxFieldDefinition {
    public String value;

    public DerivedFieldDefinitionString(String name) {
        super(name);
    }

    @Override
    String getValue(ResultSet rs) {
        return value;
    }
}

final class TransactionDetailStringFieldDefinition extends IndexedColumnFieldDefinition {

    public TransactionDetailStringFieldDefinition(String name, int columnIndex) {
        super(name, columnIndex);
    }

    @Override
    String getValue(ResultSet rs) throws SQLException {
        String stringValue = rs.getString(columnIndex);
        return StringUtils.replace(stringValue, ",",  " ");
    }
}

class TransactionDetailIntFieldDefinition extends IndexedColumnFieldDefinition {

    public TransactionDetailIntFieldDefinition(String name, int columnIndex) {
        super(name, columnIndex);
    }

    @Override
    String getValue(ResultSet rs) throws SQLException {
        return Integer.toString(rs.getInt(columnIndex));
    }
}

final class TransactionDetailBigDecimalFieldDefinition extends IndexedColumnFieldDefinition {

    public TransactionDetailBigDecimalFieldDefinition(String name, int columnIndex) {
        super(name, columnIndex);
    }

    @Override
    String getValue(ResultSet rs) throws SQLException {
        BigDecimal val = rs.getBigDecimal(columnIndex);
        return (val != null) ? val.toPlainString() : null;
    }
}

class TransactionDetailDateFieldDefinition extends IndexedColumnFieldDefinition {

    public TransactionDetailDateFieldDefinition(String name, int columnIndex) {
        super(name, columnIndex);
    }

    @Override
    String getValue(ResultSet rs) throws SQLException {
        java.sql.Date val = rs.getDate(columnIndex);
        // Return the date in yyyy-mm-dd format.
        return val != null ? val.toString() : null;
    }
}