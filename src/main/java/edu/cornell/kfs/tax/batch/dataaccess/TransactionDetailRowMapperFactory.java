package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.ResultSet;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

@FunctionalInterface
public interface TransactionDetailRowMapperFactory<U> {

    TransactionDetailRowMapper<U> createMapper(final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet);

}
