package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.cg.businessobject.Award;

public interface CemiImportBudgetFileExtractDataBuilder {

    void writeImportBudgetFileImportBudgetHighVolumeTabExtractDataToIntermediateStorage(
            final Iterator<Award> legacyObjects);

    void writeImportBudgetFileBudgetLinesDataTabExtractDataToIntermediateStorage(
            final Iterator<Award> legacyObjects);

}
