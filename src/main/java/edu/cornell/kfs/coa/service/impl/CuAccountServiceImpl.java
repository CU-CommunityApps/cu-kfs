package edu.cornell.kfs.coa.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.impl.AccountServiceImpl;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.service.CuAccountService;

public class CuAccountServiceImpl extends AccountServiceImpl implements CuAccountService{

    private static final Logger LOG = LogManager.getLogger();

    protected NoteService noteService;

    public String getDefaultLaborBenefitRateCategoryCodeForAccountType(final String accountTypeCode) {
        String value = "";
        
        //TODO rewrite to use parameter evaluator service
        
        // make sure the parameter exists
        if (parameterService.parameterExists(Account.class, COAParameterConstants.ACCOUNT_TYPE_BENEFIT_RATE)) {
            // retrieve the value(s) for the parameter
            final String paramValues = parameterService.getParameterValueAsString(Account.class, COAParameterConstants.ACCOUNT_TYPE_BENEFIT_RATE);

            // split the values of the parameter on the semi colon
            final String[] paramValuesArray = paramValues.split(";");

            // load the array into a HashMap
            final HashMap<String, String> paramValuesMap = new HashMap<String, String>();
            for (int i = 0; i < paramValuesArray.length; i++) {
                // create a new array to split on equals sign
                final String[] tempArray = paramValuesArray[i].split("=");
                paramValuesMap.put(tempArray[0], tempArray[1]);
            }

            // check to see if the map has the account type code in it
            if (paramValuesMap.containsKey(accountTypeCode)) {
                value = paramValuesMap.get(accountTypeCode);
            } else {
                // make sure the system parameter exists
                if (parameterService.parameterExists(Account.class, COAParameterConstants.BENEFIT_RATE)) {
                    value = parameterService.getParameterValueAsString(Account.class, COAParameterConstants.BENEFIT_RATE);
                }
            }
        }
        return value;
    }

	@Override
	public List<Note> getAccountNotes(final Account account) {
		List<Note> notes = new ArrayList<Note>();
		if (ObjectUtils.isNotNull(account)&& ObjectUtils.isNotNull(account.getObjectId())) {
			notes = noteService.getByRemoteObjectId(account.getObjectId());
		}
		return notes;
	}

    /**
     * CU Customization: Overridden to backport the FINP-9525 document requeue fix.
     *                   This override should be removed when we upgrade to the 2023-05-17 financials patch.
     */
    @Override
    public void updateRoleAssignmentsForAccountChange(
            final String docIdToIgnore,
            final Set<String> accountNumbers,
            final Set<String> documentTypes
    ) {
        if (accountNumbers.isEmpty() && documentTypes.isEmpty()) {
            LOG.info("updateRoleAssignmentsForAccountChange(String,Set,Set) - no account numbers or document types "
                     + "affected, skipping action request updating");
            return;
        }

        super.updateRoleAssignmentsForAccountChange(docIdToIgnore, accountNumbers, documentTypes);
    }

	public NoteService getNoteService() {
		return noteService;
	}

	public void setNoteService(final NoteService noteService) {
		this.noteService = noteService;
	}

}
