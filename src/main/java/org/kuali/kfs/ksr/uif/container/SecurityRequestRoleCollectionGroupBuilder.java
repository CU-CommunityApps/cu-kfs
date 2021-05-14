package org.kuali.kfs.ksr.uif.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.rice.krad.datadictionary.Copyable;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.CollectionGroupBuilder;
import org.kuali.rice.krad.uif.view.View;

/**
 * Custom CollectionGroupBuilder that contains a pre-filtered and pre-sorted list of request role indexes,
 * and whose filtering only returns the intersection of that list and the superclass's filtering.
 * 
 * The filtering is being done in this manner to ensure that the order of the pre-sorted indexes
 * is preserved. The existing KRAD collection filter functionality is not being used here,
 * because such processing just intersects a numerically-ordered index list with the ones
 * returned by the collection filters.
 */
public class SecurityRequestRoleCollectionGroupBuilder extends CollectionGroupBuilder implements Copyable {

    private static final long serialVersionUID = 4395545306785706058L;

    protected List<Integer> sortedRequestRoleIndexes;

    public void setSortedRequestRoleIndexes(List<Integer> sortedRequestRoleIndexes) {
        this.sortedRequestRoleIndexes = sortedRequestRoleIndexes;
    }

    /**
     * Overridden to return a pre-sorted role index list that only contains
     * the indexes returned by the superclass's version of this method.
     * The request role indexes are sorted according to the role tab ordering
     * from the associated KSR provisioning groups.
     * 
     * @see org.kuali.rice.krad.uif.container.CollectionGroupBuilder#performCollectionFiltering(
     * org.kuali.rice.krad.uif.view.View, java.lang.Object,
     * org.kuali.rice.krad.uif.container.CollectionGroup, java.util.Collection)
     */
    @Override
    protected List<Integer> performCollectionFiltering(View view, Object model,
            CollectionGroup collectionGroup, Collection<?> collection) {
        if (CollectionUtils.isEmpty(sortedRequestRoleIndexes)) {
            throw new IllegalStateException("No pre-sorted request role indexes have been specified");
        }
        
        List<Integer> filteredIndexes = super.performCollectionFiltering(view, model, collectionGroup, collection);
        Set<Integer> filteredIndexesSet = new HashSet<>(filteredIndexes);
        return sortedRequestRoleIndexes.stream()
                .filter((index) -> filteredIndexesSet.contains(index))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public SecurityRequestRoleCollectionGroupBuilder clone() throws CloneNotSupportedException {
        return (SecurityRequestRoleCollectionGroupBuilder) super.clone();
    }

}
