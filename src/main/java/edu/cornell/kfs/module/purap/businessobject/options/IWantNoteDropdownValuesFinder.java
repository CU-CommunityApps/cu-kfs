package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import edu.cornell.kfs.module.purap.businessobject.IWantNoteDropdownValue;

public class IWantNoteDropdownValuesFinder extends KeyValuesBase {

    private KeyValuesService keyValuesService;

    @Override
    public List<KeyValue> getKeyValues() {
        List<IWantNoteDropdownValue> noteTexts = (List<IWantNoteDropdownValue>) keyValuesService.findAll(IWantNoteDropdownValue.class);
        if (noteTexts == null) {
            noteTexts = new ArrayList<>(0);
        } else {
            noteTexts = new ArrayList<>(noteTexts);
        }

        Collections.sort(noteTexts);

        final List<KeyValue> labels = new ArrayList<>();
        labels.add(new ConcreteKeyValue("", ""));

        for (final IWantNoteDropdownValue iWantNote : noteTexts) {
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
