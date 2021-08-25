package edu.cornell.kfs.kew.actionitem;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

/**
 * Cornell Customization: Moved this class into the cu-kfs code, and updated it
 * for compatibility as needed.
 * 
 * CONTRIB-73 by MSU - Add a Note to Your Action List Item.
 * 
 * The Class holds note information associating with each action item. The note
 * will be removed as long as the action has been taken.
 */
public class ActionItemExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension, Serializable {

    private static final long serialVersionUID = -1079562205125660151L;

    private String actionItemId;
    private String actionNote;

    public String getActionItemId() {
        return actionItemId;
    }

    public void setActionItemId(String actionItemId) {
        this.actionItemId = actionItemId;
    }

    public String getActionNote() {
        return actionNote;
    }

    public void setActionNote(String actionNote) {
        this.actionNote = actionNote;
    }

    public String getActionNoteForSorting() {
        return StringUtils.lowerCase(actionNote);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("actionItemId", actionItemId)
                .append("actionNote", actionNote)
                .append("versionNumber", versionNumber)
                .append("lastUpdatedTimeStamp", getLastUpdatedTimestamp())
                .toString();
    }

}
