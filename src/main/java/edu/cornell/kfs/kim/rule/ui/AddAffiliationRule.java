package edu.cornell.kfs.kim.rule.ui;

import org.kuali.kfs.krad.rules.rule.BusinessRule;

import edu.cornell.kfs.kim.rule.event.ui.AddAffiliationEvent;

public interface AddAffiliationRule extends BusinessRule {

    boolean processAddAffiliation(AddAffiliationEvent addAffiliationEvent);
}
