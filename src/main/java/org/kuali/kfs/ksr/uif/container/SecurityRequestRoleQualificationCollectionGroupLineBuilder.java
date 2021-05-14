package org.kuali.kfs.ksr.uif.container;

import java.util.List;

import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.CollectionGroupLineBuilder;
import org.kuali.rice.krad.uif.container.collections.LineBuilderContext;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.RemoteFieldsHolder;

/**
 * ====
 * CU Customization:
 * Added a CollectionGroupLineBuilder implementation that will auto-populate
 * the "line" context variable on RemoteFieldsHolder components, to simplify
 * performing custom processing with such components. (RemoteFieldsHolder
 * components normally only get the "parentLine" context variable populated
 * at the time when they are generating the fields.)
 * ====
 */
public class SecurityRequestRoleQualificationCollectionGroupLineBuilder extends CollectionGroupLineBuilder {

    private static final long serialVersionUID = 500656468947213993L;

    // This variable is private in the superclass and has no getter, so declare it in this class as well.
    protected LineBuilderContext lineBuilderContext;

    public SecurityRequestRoleQualificationCollectionGroupLineBuilder(LineBuilderContext lineBuilderContext) {
        super(lineBuilderContext);
        this.lineBuilderContext = lineBuilderContext;
    }

    /**
     * Overridden to also populate the "line" context variable
     * on any RemoteFieldsHolder instances. (Such components
     * normally only have the "parentLine" variable populated
     * at this point.)
     * 
     * @see org.kuali.rice.krad.uif.container.CollectionGroupLineBuilder#processAnyRemoteFieldsHolder(
     * org.kuali.rice.krad.uif.container.CollectionGroup, java.util.List)
     */
    @Override
    public List<Field> processAnyRemoteFieldsHolder(CollectionGroup group, List<? extends Component> items) {
        for (Component item : items) {
            if (item instanceof RemoteFieldsHolder) {
                item.getContext().put(UifConstants.ContextVariableNames.LINE, lineBuilderContext.getCurrentLine());
            }
        }
        
        return super.processAnyRemoteFieldsHolder(group, items);
    }

}
