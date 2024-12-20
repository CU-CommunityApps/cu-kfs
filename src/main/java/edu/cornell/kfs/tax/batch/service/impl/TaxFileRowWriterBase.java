package edu.cornell.kfs.tax.batch.service.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.service.TaxFileRowWriter;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.xml.TaxOutputSection;

public abstract class TaxFileRowWriterBase<T> implements TaxFileRowWriter<T> {

    protected final TaxOutputDefinition taxOutputDefinition;
    protected final Map<String, TaxOutputSection> taxOutputSections;

    protected TaxFileRowWriterBase(final TaxOutputDefinition taxOutputDefinition) {
        Validate.notNull(taxOutputDefinition, "taxOutputDefinition cannot be null");
        this.taxOutputDefinition = taxOutputDefinition;
        this.taxOutputSections = taxOutputDefinition.getSections().stream()
                .collect(Collectors.toUnmodifiableMap(section -> section.getName(), section -> section));
    }

    protected TaxOutputSection getSection(final String sectionName) {
        final TaxOutputSection section = taxOutputSections.get(sectionName);
        Validate.isTrue(section != null, "Could not find tax output section: %s", sectionName);
        return section;
    }

}
