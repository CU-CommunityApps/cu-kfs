package edu.cornell.kfs.sys.util;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.rice.kim.impl.identity.PersonImpl;

public class MockDocumentUtils {

    public static <T extends Document> T buildMockDocument(Class<T> documentClass) {
        T document = EasyMock.partialMockBuilder(documentClass)
                .createNiceMock();
        EasyMock.replay(document);
        
        document.setDocumentHeader(new FinancialSystemDocumentHeader());
        document.setAdHocRoutePersons(new ArrayList<>());
        document.setAdHocRouteWorkgroups(new ArrayList<>());
        document.setNotes(new ArrayList<>());
        
        return document;
    }

    public static Note buildMockNote(String noteText) {
        Note note = buildMockNote();
        note.setNoteText(noteText);
        return note;
    }

    public static Note buildMockNote() {
        Note note = EasyMock.partialMockBuilder(Note.class)
                .addMockedMethod("setNotePostedTimestampToCurrent")
                .createNiceMock();
        
        note.setNotePostedTimestampToCurrent();
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.replay(note);
        return note;
    }

    public static AdHocRoutePerson buildMockAdHocRoutePerson() {
        TestAdHocRoutePerson adHocPerson = EasyMock.partialMockBuilder(TestAdHocRoutePerson.class)
                .createNiceMock();
        EasyMock.replay(adHocPerson);
        adHocPerson.setType(AdHocRouteRecipient.PERSON_TYPE);
        adHocPerson.setPerson(new PersonImpl());
        return adHocPerson;
    }

    /**
     * Helper AdHocRoutePerson subclass that allows for setting the recipient's ID
     * without invoking any service locators.
     */
    public static class TestAdHocRoutePerson extends AdHocRoutePerson {
        private static final long serialVersionUID = 1L;

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }
}
