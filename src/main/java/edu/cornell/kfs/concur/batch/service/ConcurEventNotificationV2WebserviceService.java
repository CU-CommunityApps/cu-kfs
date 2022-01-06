package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;

public interface ConcurEventNotificationV2WebserviceService {

    ConcurExpenseV3ListingDTO getConcurExpenseListing(String accessToken, String expenseListEndpoint);

    ConcurExpenseV3ListItemDTO getConcurExpenseV3ListItemDTO(String accessToken, String expenseListEndpoint);

}
