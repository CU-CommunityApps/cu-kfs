/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
import org.kuali.rice.kim.api.KimApiConstants;
import org.kuali.rice.kim.api.identity.name.EntityNameContract;
import org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.data.jpa.converters.BooleanYNConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.sql.Timestamp;

/*
 * CU Customization:
 * Fixed a few areas that were not referencing the unmasked values as expected.
 */
@MappedSuperclass
public abstract class EntityNameBase extends PersistableBusinessObjectBase implements EntityNameContract {

    private static final long serialVersionUID = 7102034794623577359L;

    @Column(name = "ENTITY_ID")
    private String entityId;

    @Column(name = "NM_TYP_CD")
    private String nameCode;

    @Column(name = "FIRST_NM")
    private String firstName;

    @Column(name = "MIDDLE_NM")
    private String middleName;

    @Column(name = "LAST_NM")
    private String lastName;

    @Column(name = "PREFIX_NM")
    private String namePrefix;

    @Column(name = "TITLE_NM")
    private String nameTitle;

    @Column(name = "SUFFIX_NM")
    private String nameSuffix;

    @Convert(converter = BooleanYNConverter.class)
    @Column(name = "ACTV_IND")
    private boolean active;

    @Convert(converter = BooleanYNConverter.class)
    @Column(name = "DFLT_IND")
    private boolean defaultValue;

    @Column(name = "NOTE_MSG")
    private String noteMessage;

    @Column(name = "NM_CHNG_DT")
    private Timestamp nameChangedDate;

    @Transient
    private boolean suppressName;

    @Override
    public String getFirstName() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getMiddleName() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Override
    public String getLastName() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getNamePrefix() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return this.namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public String getNameTitle() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return this.nameTitle;
    }

    public void setNameTitle(String nameTitle) {
        this.nameTitle = nameTitle;
    }

    @Override
    public String getFirstNameUnmasked() {
        return this.firstName;
    }

    @Override
    public String getMiddleNameUnmasked() {
        return this.middleName;
    }

    @Override
    public String getLastNameUnmasked() {
        return this.lastName;
    }

    @Override
    public String getNamePrefixUnmasked() {
        return this.namePrefix;
    }

    @Override
    public String getNameTitleUnmasked() {
        return this.nameTitle;
    }

    @Override
    public String getNameSuffixUnmasked() {
        return this.nameSuffix;
    }

    @Override
    public String getCompositeName() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return getCompositeNameUnmasked();
    }

    @Override
    public String getCompositeNameUnmasked() {
        return getLastNameUnmasked() + ", " + getFirstNameUnmasked()
                + (StringUtils.isBlank(getMiddleNameUnmasked()) ? "" : " " + getMiddleNameUnmasked());
    }

    @Override
    public DateTime getNameChangedDate() {
        return nameChangedDate != null ? new DateTime(nameChangedDate.getTime()) : null;
    }

    public void setNameChangedDate(DateTime nameChangedDate) {
        if (nameChangedDate != null) {
            this.nameChangedDate = new Timestamp(nameChangedDate.getMillis());
        } else {
            this.nameChangedDate = null;
        }
    }

    public Timestamp getNameChangedTimestamp() {
        return nameChangedDate;
    }

    public void setNameChangedTimestamp(Timestamp nameChangedDate) {
        this.nameChangedDate = nameChangedDate;
    }

    @Override
    public boolean isSuppressName() {
        try {
            EntityPrivacyPreferences privacy = KimApiServiceLocator.getIdentityService().getEntityPrivacyPreferences(
                    getEntityId());
            if (privacy != null) {
                this.suppressName = privacy.isSuppressName();
            } else {
                this.suppressName = false;
            }
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }

        return this.suppressName;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getNameCode() {
        return nameCode;
    }

    public void setNameCode(String nameCode) {
        this.nameCode = nameCode;
    }

    @Override
    public String getNameSuffix() {
        if (isSuppressName()) {
            return KimApiConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
        }

        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public boolean getActive() {
        return active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getNoteMessage() {
        return noteMessage;
    }

    public void setNoteMessage(String noteMessage) {
        this.noteMessage = noteMessage;
    }

    public boolean getSuppressName() {
        return suppressName;
    }

    public void setSuppressName(boolean suppressName) {
        this.suppressName = suppressName;
    }
}
