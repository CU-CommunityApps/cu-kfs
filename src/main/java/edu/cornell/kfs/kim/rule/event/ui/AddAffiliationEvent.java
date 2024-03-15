package edu.cornell.kfs.kim.rule.event.ui;

import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.rules.rule.BusinessRule;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEventBase;

import edu.cornell.kfs.kim.bo.ui.PersonDocumentAffiliation;
import edu.cornell.kfs.kim.rule.ui.AddAffiliationRule;

public class AddAffiliationEvent extends KualiDocumentEventBase {

    private PersonDocumentAffiliation affiliation;

    public AddAffiliationEvent(final String errorPathPrefix, final IdentityManagementPersonDocument document) {
        super("adding affiliation document " + getDocumentId(document), errorPathPrefix, document);
    }

    public AddAffiliationEvent(final String errorPathPrefix, final Document document,
            final PersonDocumentAffiliation affiliation) {
        this(errorPathPrefix, (IdentityManagementPersonDocument) document);
        this.affiliation = affiliation;
    }

    @Override
    public Class<? extends BusinessRule> getRuleInterfaceClass() {
        return AddAffiliationRule.class;
    }

    @Override
    public boolean invokeRuleMethod(final BusinessRule rule) {
        return ((AddAffiliationRule) rule).processAddAffiliation(this);
    }

    public PersonDocumentAffiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(PersonDocumentAffiliation affiliation) {
        this.affiliation = affiliation;
    }

}
