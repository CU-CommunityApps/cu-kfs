package org.kuali.kfs.ksr.uif.field;

import java.util.Collections;
import java.util.List;

import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.util.KsrUtil;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.kim.api.type.KimTypeAttribute;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.component.BindingInfo;
import org.kuali.rice.krad.uif.component.MethodInvokerConfig;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.field.RemoteFieldsHolder;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.view.View;

/**
 * Custom subclass of RemoteFieldsHolder that overwrites the translated input fields'
 * BindingInfo instances to point them at actual sub-object fields. Rice's default
 * implementation assumes that the input fields should be bound to a Map, which is
 * not the case with the SecurityRequestRoleQualification object since the fields
 * on its qualification detail sub-objects are bound to those sub-objects in a List instead.
 * 
 * NOTE: There is a known issue where the generated fields will have a property path
 * that starts with "document.document." (instead of just "document."). A matching
 * getter method has been added to the SecurityRequestDocument as a workaround.
 */
public class KsrRemoteFieldsHolder extends RemoteFieldsHolder {

    private static final long serialVersionUID = 3226217974357356805L;

    /**
     * Overridden to associate the input fields with actual non-map-related fields on the document,
     * and to manually configure the method invoker to call this object instead of the view helper service.
     * 
     * @see org.kuali.rice.krad.uif.field.RemoteFieldsHolder#fetchAndTranslateRemoteFields(org.kuali.rice.krad.uif.container.Container)
     */
    @Override
    public List<InputField> fetchAndTranslateRemoteFields(Container parent) {
        View view = ViewLifecycle.getView();
        MethodInvokerConfig fetchingMethodInvoker = getFetchingMethodInvoker();
        if (fetchingMethodInvoker == null) {
            fetchingMethodInvoker = new MethodInvokerConfig();
        }
        fetchingMethodInvoker.setTargetObject(this);
        setFetchingMethodInvoker(fetchingMethodInvoker);
        
        List<InputField> inputFields = super.fetchAndTranslateRemoteFields(parent);
        
        for (InputField inputField : inputFields) {
            inputField.setEnableAutoDirectInquiry(false);
            inputField.setEnableAutoInquiry(false);
            inputField.setEnableAutoQuickfinder(false);
            
            // Copied and tweaked the BindingInfo-setting logic from the superclass.
            BindingInfo fieldBindingInfo = new BindingInfo();
            fieldBindingInfo.setDefaults(view, inputField.getPropertyName());
            inputField.setBindingInfo(fieldBindingInfo);
        }
        
        return inputFields;
    }

    /**
     * This method will be invoked by the superclass's "fetchAndTranslateRemoteFields" method,
     * to retrieve the qualification detail fields as RemotableAttributeField instances.
     * 
     * It is assumed that the enclosing CollectionGroup or its builders have also populated
     * the "line" context variable on this field holder object by now. (The "parentLine"
     * variable is the only one that is populated at this point by default.)
     * 
     * @param view The current View instance.
     * @param model The model object; should be a SecurityRequestDocumentForm in this case.
     * @param parent The parent Container instance.
     * @return A List of RemotableAttributeField instances representing the qualification detail fields.
     */
    public List<RemotableAttributeField> getFieldsForQualificationDetails(View view, Object model, Container parent) {
        SecurityRequestRole requestRole = (SecurityRequestRole) getContext()
                .get(UifConstants.ContextVariableNames.PARENT_LINE);
        SecurityRequestRoleQualification roleQualification = (SecurityRequestRoleQualification) getContext()
                .get(UifConstants.ContextVariableNames.LINE);
        
        if (requestRole != null && roleQualification != null && requestRole.isQualifiedRole()) {
            List<KimTypeAttribute> typeAttributes = KsrUtil.getTypeAttributesForRoleRequest(requestRole);
            KsrRemoteFieldBuilder fieldBuilder = new KsrRemoteFieldBuilder();
            return fieldBuilder.buildQualificationFields(roleQualification, typeAttributes);
        }
        
        return Collections.emptyList();
    }

}
