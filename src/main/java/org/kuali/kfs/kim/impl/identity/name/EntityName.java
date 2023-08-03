/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.impl.identity.name;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.mo.common.Defaultable;
import org.kuali.kfs.core.api.mo.common.Identifiable;
import org.kuali.kfs.core.api.mo.common.active.Inactivatable;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.privacy.EntityPrivacyPreferences;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import java.sql.Timestamp;

/*
 * CU Customization:
 * Fixed a few areas that were not referencing the unmasked values as expected.
 */
public class EntityName extends PersistableBusinessObjectBase implements Defaultable, Inactivatable, Identifiable {

    public static final String CACHE_NAME = "EntityNameType";

    private static final long serialVersionUID = -1449221117942310530L;

    private String id;

    private EntityNameType nameType;
    private String entityId;
    private String nameCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String namePrefix;
    private String nameTitle;
    private String nameSuffix;
    private boolean active;
    private boolean defaultValue;
    private String noteMessage;
    private Timestamp nameChangedDate;
    private boolean suppressName;

    public EntityNameType getNameType() {
        return nameType;
    }

    public void setNameType(final EntityNameType nameType) {
        this.nameType = nameType;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getFirstName() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getNamePrefix() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return namePrefix;
    }

    public void setNamePrefix(final String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameTitle() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return nameTitle;
    }

    public void setNameTitle(final String nameTitle) {
        this.nameTitle = nameTitle;
    }

    public String getFirstNameUnmasked() {
        return firstName;
    }

    public String getMiddleNameUnmasked() {
        return middleName;
    }

    public String getLastNameUnmasked() {
        return lastName;
    }

    public String getNamePrefixUnmasked() {
        return namePrefix;
    }

    public String getNameTitleUnmasked() {
        return nameTitle;
    }

    public String getNameSuffixUnmasked() {
        return nameSuffix;
    }

    public String getCompositeName() {
        if (isSuppressName()) {
            return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return getCompositeNameUnmasked();
    }

    public String getCompositeNameUnmasked() {
        return getLastNameUnmasked() + ", " + getFirstNameUnmasked() + (StringUtils.isBlank(getMiddleNameUnmasked()) ? "" : " " +
                getMiddleNameUnmasked());
    }

    public DateTime getNameChangedDate() {
        return nameChangedDate != null ? new DateTime(nameChangedDate.getTime()) : null;
    }

    public void setNameChangedDate(final DateTime nameChangedDate) {
        if (nameChangedDate != null) {
            this.nameChangedDate = new Timestamp(nameChangedDate.getMillis());
        } else {
            this.nameChangedDate = null;
        }
    }

    public Timestamp getNameChangedTimestamp() {
        return nameChangedDate;
    }

    public void setNameChangedTimestamp(final Timestamp nameChangedDate) {
        this.nameChangedDate = nameChangedDate;
    }

    public boolean isSuppressName() {
        try {
            final EntityPrivacyPreferences privacy = KimApiServiceLocator.getIdentityService().getEntityPrivacyPreferences(
                    getEntityId());
            if (privacy != null) {
                suppressName = privacy.isSuppressName();
            } else {
                suppressName = false;
            }
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }

        return suppressName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public String getNameCode() {
        return nameCode;
    }

    public void setNameCode(final String nameCode) {
        this.nameCode = nameCode;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(final String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public boolean getActive() {
        return active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getNoteMessage() {
        return noteMessage;
    }

    public void setNoteMessage(final String noteMessage) {
        this.noteMessage = noteMessage;
    }

    public boolean getSuppressName() {
        return suppressName;
    }

    public void setSuppressName(final boolean suppressName) {
        this.suppressName = suppressName;
    }
}
