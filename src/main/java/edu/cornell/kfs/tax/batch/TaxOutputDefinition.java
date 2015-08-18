package edu.cornell.kfs.tax.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper object defining how each tax data row should be written
 * to the output file.
 */
public class TaxOutputDefinition {
    // The output section definitions.
    private List<TaxOutputSection> sections;

    public TaxOutputDefinition() {
        sections = new ArrayList<TaxOutputSection>();
    }



    /**
     * Returns the output section definitions.
     */
    public List<TaxOutputSection> getSections() {
        return sections;
    }

    public void setSections(List<TaxOutputSection> sections) {
        this.sections = sections;
    }

    public void addSection(TaxOutputSection section) {
        sections.add(section);
    }

}
