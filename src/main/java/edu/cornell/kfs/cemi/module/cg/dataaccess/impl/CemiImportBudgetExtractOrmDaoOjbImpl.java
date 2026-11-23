package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;

public class CemiImportBudgetExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase
        implements CemiImportBudgetExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiImportBudgetExtractAsCloseableStream() {
        // TODO restore scope filter, local env data restriction
        return Stream.empty();
    }

}
