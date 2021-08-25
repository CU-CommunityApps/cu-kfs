package edu.cornell.kfs.kew.actionlist.service;

import org.kuali.kfs.kew.actionlist.service.ActionListService;

import edu.cornell.kfs.kew.actionitem.ActionItemExtension;

public interface CuActionListService extends ActionListService {

    ActionItemExtension findActionItemExtensionByActionItemId(String actionItemId);

    String saveActionItemNoteForActionItemId(String note, String actionItemId);

}
