package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.Comparator;

import edu.cornell.kfs.module.purap.businessobject.IWantNote;

public class IWantNoteComparator implements Comparator {

    @Override
    public int compare(final Object o1, final Object o2) {
        final IWantNote iWantNote1 = (IWantNote) o1;
        final IWantNote iWantNote2 = (IWantNote) o2;
        return iWantNote1.getNoteText().compareTo(iWantNote2.getNoteText());
    }

}
