package org.kuali.kfs.ksr.uif.modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityGroupTab;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.uif.container.SecurityRequestRoleCollectionGroupBuilder;
import org.kuali.kfs.ksr.web.form.SecurityRequestDocumentForm;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.PageGroup;
import org.kuali.rice.krad.uif.modifier.ComponentModifierBase;
import org.kuali.rice.krad.uif.util.ComponentFactory;

/**
 * ====
 * CU Customization:
 * Added a component modifier that creates filtered request role collection groups
 * for each tab on the security group, and inserts them into the KSR document page.
 * ====
 */
public class SecurityRequestDocumentTabGenerationModifier extends ComponentModifierBase {

    private static final long serialVersionUID = 1767349886003356152L;

    protected static final String TAB_HEADER_PREFIX = "Request Access to ";
    protected static final String TAB_ID_START = "SecurityRequestDocument_Tab";

    protected String groupPrototypeId;

    /**
     * Only PageGroup components can be updated by this modifier.
     * 
     * @see org.kuali.rice.krad.uif.modifier.ComponentModifier#getSupportedComponents()
     */
    @Override
    public Set<Class<? extends Component>> getSupportedComponents() {
        return Collections.singleton(PageGroup.class);
    }

    /**
     * Creates new filtered SecurityRequestRole collection groups (effectively Security Request Tabs)
     * and places them in the page's items list.
     * 
     * The logic here is based off of that from the "SecurityRequestDocumentAction.buildTabRoleIndexes" method
     * in the original KNS version of the KSR code.
     * 
     * @see org.kuali.rice.krad.uif.modifier.ComponentModifier#performModification(
     * java.lang.Object, org.kuali.rice.krad.uif.component.Component)
     */
    @Override
    public void performModification(Object model, Component component) {
        SecurityRequestDocumentForm form = (SecurityRequestDocumentForm) model;
        PageGroup page = (PageGroup) component;
        SecurityRequestDocument document = (SecurityRequestDocument) form.getDocument();
        
        // Skip the modification if no principal has been configured yet.
        if (StringUtils.isBlank(document.getPrincipalId())) {
            return;
        }
        
        List<? extends Component> oldItems = page.getItems();
        List<Component> newItems = new ArrayList<>();
        newItems.addAll(oldItems);
        addSecurityGroupTabComponents(newItems, document);
        
        page.setItems(newItems);
        page.sortItems();
    }

    protected void addSecurityGroupTabComponents(List<Component> items, SecurityRequestDocument document) {
        SecurityGroup securityGroup = document.getSecurityGroup();
        Map<String,Integer> requestRoleIndexMap = buildRequestRoleIndexMap(document);
        Stream<SecurityGroupTab> sortedTabs = securityGroup.getSecurityGroupTabs()
                .stream()
                .sorted((tab1, tab2) -> tab1.getTabOrder().compareTo(tab2.getTabOrder()));
        
        sortedTabs.forEach((groupTab) -> {
            List<Integer> roleIndexes = buildSortedRequestRoleIndexesForGroupTab(groupTab, requestRoleIndexMap);
            if (!roleIndexes.isEmpty()) {
                CollectionGroup requestRoleGroup = (CollectionGroup) ComponentFactory.getNewComponentInstance(groupPrototypeId);
                
                /*
                 * The component-copying operation above is not properly deep-copying the CollectionGroupBuilder
                 * for some reason, so explicitly configure a new instance instead.
                 */
                SecurityRequestRoleCollectionGroupBuilder groupBuilder = new SecurityRequestRoleCollectionGroupBuilder();
                groupBuilder.setSortedRequestRoleIndexes(roleIndexes);
                requestRoleGroup.setCollectionGroupBuilder(groupBuilder);
                
                requestRoleGroup.setId(TAB_ID_START + groupTab.getTabOrder());
                requestRoleGroup.setHeaderText(TAB_HEADER_PREFIX + groupTab.getTabName());
                
                items.add(requestRoleGroup);
            }
        });
    }

    protected Map<String,Integer> buildRequestRoleIndexMap(SecurityRequestDocument document) {
        Map<String,Integer> roleIndexMap = new HashMap<>();
        int i = 0;
        
        for (SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
            roleIndexMap.put(requestRole.getRoleId(), Integer.valueOf(i));
            i++;
        }
        
        return roleIndexMap;
    }

    protected List<Integer> buildSortedRequestRoleIndexesForGroupTab(SecurityGroupTab groupTab, Map<String,Integer> requestRoleIndexMap) {
        return groupTab.getSecurityProvisioningGroups()
                .stream()
                .sorted((provGroup1, provGroup2) -> provGroup1.getRoleTabOrder().compareTo(provGroup2.getRoleTabOrder()))
                .map((provGroup) -> requestRoleIndexMap.get(provGroup.getRoleId()))
                .filter((roleIndex) -> roleIndex != null)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void setGroupPrototypeId(String groupPrototypeId) {
        this.groupPrototypeId = groupPrototypeId;
    }

}
