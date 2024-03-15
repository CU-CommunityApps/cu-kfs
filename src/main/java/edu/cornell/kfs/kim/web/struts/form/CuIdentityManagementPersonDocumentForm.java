package edu.cornell.kfs.kim.web.struts.form;

import org.kuali.kfs.kim.web.struts.form.IdentityManagementPersonDocumentForm;

import edu.cornell.kfs.kim.bo.ui.PersonDocumentAffiliation;

public class CuIdentityManagementPersonDocumentForm extends IdentityManagementPersonDocumentForm {

    private static final long serialVersionUID = 1L;

    private PersonDocumentAffiliation newAffiliation;

    public CuIdentityManagementPersonDocumentForm() {
        super();
        newAffiliation = new PersonDocumentAffiliation();
    }

    public PersonDocumentAffiliation getNewAffiliation() {
        return newAffiliation;
    }

    public void setNewAffiliation(PersonDocumentAffiliation newAffiliation) {
        this.newAffiliation = newAffiliation;
    }

}
