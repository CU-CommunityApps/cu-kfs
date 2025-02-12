package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.ResultSet;

import org.apache.commons.lang3.function.TriFunction;
import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

@FunctionalInterface
public interface TransactionDetailRowMapperFactory<U>
        extends TriFunction<EncryptionService, TaxDtoDbMetadata, ResultSet, TransactionDetailRowMapper<U>> {

}
