package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

public class TransactionDetailDaoJdbcBase extends CuSqlQueryPlatformAwareDaoBaseJdbc {

    protected TaxTableMetadataLookupService taxTableMetadataLookupService;
    protected EncryptionService encryptionService;

    protected <T> List<T> readFullResults(final ResultSet resultSet, final TaxDtoDbMetadata metadata,
            final Supplier<T> dtoConstructor) throws SQLException {
        final TaxDtoRowMapper<T> dtoMapper = new TaxDtoRowMapperImpl<>(
                dtoConstructor, encryptionService, metadata, resultSet);
        Stream.Builder<T> dtos = Stream.builder();
        while (dtoMapper.moveToNextRow()) {
            dtos.add(dtoMapper.readCurrentRow());
        }
        return dtos.build().collect(Collectors.toUnmodifiableList());
    }

    public void setTaxTableMetadataLookupService(final TaxTableMetadataLookupService taxTableMetadataLookupService) {
        this.taxTableMetadataLookupService = taxTableMetadataLookupService;
    }

    public void setEncryptionService(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

}
