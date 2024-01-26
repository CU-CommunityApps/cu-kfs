package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import edu.cornell.kfs.module.purap.businessobject.IWantNote;

public class IWantNoteValuesFinder extends KeyValuesBase {

    private KeyValuesService keyValuesService;

    @Override
    public List<KeyValue> getKeyValues() {
        List<IWantNote> noteTexts = (List<IWantNote>) keyValuesService.findAll(IWantNote.class);
        if (noteTexts == null) {
            noteTexts = new ArrayList<>(0);
        } else {
            noteTexts = new ArrayList<>(noteTexts);
        }

        noteTexts.sort(new IWantNoteComparator());

        final List<KeyValue> labels = new ArrayList<>();
        labels.add(new ConcreteKeyValue("", ""));

        for (final IWantNote iWantNote : noteTexts) {
            if (iWantNote.isActive()) {
                labels.add(new ConcreteKeyValue(iWantNote.getNoteText(), iWantNote.getNoteText()));
            }
        }

        return labels;
    }

    public void setKeyValuesService(final KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }

}
