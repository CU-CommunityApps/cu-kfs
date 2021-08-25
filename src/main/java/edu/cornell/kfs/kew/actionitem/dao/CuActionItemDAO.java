package edu.cornell.kfs.kew.actionitem.dao;

import org.kuali.kfs.kew.actionitem.dao.ActionItemDAO;

import edu.cornell.kfs.kew.actionitem.ActionItemExtension;

public interface CuActionItemDAO extends ActionItemDAO {

    ActionItemExtension findActionItemExtensionByActionItemId(String actionItemId);

    void saveActionItemExtension(ActionItemExtension extension);

}
