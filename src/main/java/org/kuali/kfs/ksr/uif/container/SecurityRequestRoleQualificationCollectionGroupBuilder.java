package org.kuali.kfs.ksr.uif.container;

import org.kuali.rice.krad.datadictionary.Copyable;
import org.kuali.rice.krad.uif.container.CollectionGroupBuilder;
import org.kuali.rice.krad.uif.container.CollectionGroupLineBuilder;
import org.kuali.rice.krad.uif.container.collections.LineBuilderContext;

/**
 * ====
 * CU Customization:
 * Added a custom CollectionGroupBuilder whose line builders are
 * SecurityRequestRoleQualificationCollectionGroupLineBuilder objects instead.
 * ====
 */
public class SecurityRequestRoleQualificationCollectionGroupBuilder extends CollectionGroupBuilder
        implements Copyable {

    private static final long serialVersionUID = -1979144644806319232L;

    @Override
    public CollectionGroupLineBuilder getCollectionGroupLineBuilder(LineBuilderContext lineBuilderContext) {
        return new SecurityRequestRoleQualificationCollectionGroupLineBuilder(lineBuilderContext);
    }

    @Override
    public SecurityRequestRoleQualificationCollectionGroupBuilder clone() throws CloneNotSupportedException {
        return (SecurityRequestRoleQualificationCollectionGroupBuilder) super.clone();
    }

}
