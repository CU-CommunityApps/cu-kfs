package edu.cornell.kfs.sys.util;

import java.util.function.Supplier;

public interface DerivedSqlParameterHandler {

    boolean moveToNextRow();

    Supplier<?> getSupplierFor(final Object parameterKey);

}
