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
package org.kuali.kfs.kim.api.identity.name;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.CodedAttribute;
import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.mo.AbstractDataTransferObject;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.kuali.rice.core.api.util.jaxb.DateTimeAdapter;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Collection;

/*
 * CU Customization:
 * Fixed a few areas that were not referencing the unmasked values as expected.
 */
@XmlRootElement(name = EntityName.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = EntityName.Constants.TYPE_NAME, propOrder = {
        EntityName.Elements.ID,
        EntityName.Elements.ENTITY_ID,
        EntityName.Elements.NAME_TYPE,
        EntityName.Elements.NAME_PREFIX,
        EntityName.Elements.NAME_TITLE,
        EntityName.Elements.FIRST_NAME,
        EntityName.Elements.MIDDLE_NAME,
        EntityName.Elements.LAST_NAME,
        EntityName.Elements.NAME_SUFFIX,
        EntityName.Elements.COMPOSITE_NAME,
        EntityName.Elements.NAME_PREFIX_UNMASKED,
        EntityName.Elements.NAME_TITLE_UNMASKED,
        EntityName.Elements.FIRST_NAME_UNMASKED,
        EntityName.Elements.MIDDLE_NAME_UNMASKED,
        EntityName.Elements.LAST_NAME_UNMASKED,
        EntityName.Elements.NAME_SUFFIX_UNMASKED,
        EntityName.Elements.COMPOSITE_NAME_UNMASKED,
        EntityName.Elements.NOTE_MESSAGE,
        EntityName.Elements.NAME_CHANGED_DATE,
        EntityName.Elements.SUPPRESS_NAME,
        EntityName.Elements.DEFAULT_VALUE,
        EntityName.Elements.ACTIVE,
        CoreConstants.CommonElements.VERSION_NUMBER,
        CoreConstants.CommonElements.OBJECT_ID,
        CoreConstants.CommonElements.FUTURE_ELEMENTS
})
public final class EntityName extends AbstractDataTransferObject implements EntityNameContract {

    @XmlElement(name = Elements.NAME_SUFFIX, required = false)
    private final String nameSuffix;
    @XmlElement(name = Elements.ENTITY_ID, required = false)
    private final String entityId;
    @XmlElement(name = Elements.NAME_TYPE, required = false)
    private final CodedAttribute nameType;
    @XmlElement(name = Elements.FIRST_NAME, required = false)
    private final String firstName;
    @XmlElement(name = Elements.FIRST_NAME_UNMASKED, required = false)
    private final String firstNameUnmasked;
    @XmlElement(name = Elements.MIDDLE_NAME, required = false)
    private final String middleName;
    @XmlElement(name = Elements.MIDDLE_NAME_UNMASKED, required = false)
    private final String middleNameUnmasked;
    @XmlElement(name = Elements.LAST_NAME, required = false)
    private final String lastName;
    @XmlElement(name = Elements.LAST_NAME_UNMASKED, required = false)
    private final String lastNameUnmasked;
    @XmlElement(name = Elements.NAME_PREFIX, required = false)
    private final String namePrefix;
    @XmlElement(name = Elements.NAME_PREFIX_UNMASKED, required = false)
    private final String namePrefixUnmasked;
    @XmlElement(name = Elements.NAME_TITLE, required = false)
    private final String nameTitle;
    @XmlElement(name = Elements.NAME_TITLE_UNMASKED, required = false)
    private final String nameTitleUnmasked;
    @XmlElement(name = Elements.NAME_SUFFIX_UNMASKED, required = false)
    private final String nameSuffixUnmasked;
    @XmlElement(name = Elements.COMPOSITE_NAME, required = false)
    private final String compositeName;
    @XmlElement(name = Elements.COMPOSITE_NAME_UNMASKED, required = false)
    private final String compositeNameUnmasked;
    @XmlElement(name = Elements.NOTE_MESSAGE, required = false)
    private final String noteMessage;
    @XmlElement(name = Elements.NAME_CHANGED_DATE, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime nameChangedDate;
    @XmlElement(name = Elements.SUPPRESS_NAME, required = false)
    private final boolean suppressName;
    @XmlElement(name = CoreConstants.CommonElements.VERSION_NUMBER, required = false)
    private final Long versionNumber;
    @XmlElement(name = CoreConstants.CommonElements.OBJECT_ID, required = false)
    private final String objectId;
    @XmlElement(name = Elements.DEFAULT_VALUE, required = false)
    private final boolean defaultValue;
    @XmlElement(name = Elements.ACTIVE, required = false)
    private final boolean active;
    @XmlElement(name = Elements.ID, required = false)
    private final String id;
    @SuppressWarnings("unused")
    @XmlAnyElement
    private final Collection<Element> _futureElements = null;

    /**
     * Private constructor used only by JAXB.
     */
    private EntityName() {
        this.nameSuffix = null;
        this.entityId = null;
        this.nameType = null;
        this.firstName = null;
        this.firstNameUnmasked = null;
        this.middleName = null;
        this.middleNameUnmasked = null;
        this.lastName = null;
        this.lastNameUnmasked = null;
        this.namePrefix = null;
        this.namePrefixUnmasked = null;
        this.nameTitle = null;
        this.nameTitleUnmasked = null;
        this.nameSuffixUnmasked = null;
        this.compositeName = null;
        this.compositeNameUnmasked = null;
        this.noteMessage = null;
        this.nameChangedDate = null;
        this.suppressName = false;
        this.versionNumber = null;
        this.objectId = null;
        this.defaultValue = false;
        this.active = false;
        this.id = null;
    }

    private EntityName(Builder builder) {
        this.nameSuffix = builder.getNameSuffix();
        this.entityId = builder.getEntityId();
        this.nameType = builder.getNameType() != null ? builder.getNameType().build() : null;
        this.firstName = builder.getFirstName();
        this.firstNameUnmasked = builder.getFirstNameUnmasked();
        this.middleName = builder.getMiddleName();
        this.middleNameUnmasked = builder.getMiddleNameUnmasked();
        this.lastName = builder.getLastName();
        this.lastNameUnmasked = builder.getLastNameUnmasked();
        this.namePrefix = builder.getNamePrefix();
        this.namePrefixUnmasked = builder.getNamePrefixUnmasked();
        this.nameTitle = builder.getNameTitle();
        this.nameTitleUnmasked = builder.getNameTitleUnmasked();
        this.nameSuffixUnmasked = builder.getNameSuffixUnmasked();
        this.compositeName = builder.getCompositeName();
        this.compositeNameUnmasked = builder.getCompositeNameUnmasked();
        this.noteMessage = builder.getNoteMessage();
        this.nameChangedDate = builder.getNameChangedDate();
        this.suppressName = builder.isSuppressName();
        this.versionNumber = builder.getVersionNumber();
        this.objectId = builder.getObjectId();
        this.defaultValue = builder.isDefaultValue();
        this.active = builder.isActive();
        this.id = builder.getId();
    }

    @Override
    public String getNameSuffix() {
        return this.nameSuffix;
    }

    @Override
    public String getEntityId() {
        return this.entityId;
    }

    @Override
    public CodedAttribute getNameType() {
        return this.nameType;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getFirstNameUnmasked() {
        return this.firstNameUnmasked;
    }

    @Override
    public String getMiddleName() {
        return this.middleName;
    }

    @Override
    public String getMiddleNameUnmasked() {
        return this.middleNameUnmasked;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public String getLastNameUnmasked() {
        return this.lastNameUnmasked;
    }

    @Override
    public String getNamePrefix() {
        return this.namePrefix;
    }

    @Override
    public String getNamePrefixUnmasked() {
        return this.namePrefixUnmasked;
    }

    @Override
    public String getNameTitle() {
        return this.nameTitle;
    }

    @Override
    public String getNameTitleUnmasked() {
        return this.nameTitleUnmasked;
    }

    @Override
    public String getNameSuffixUnmasked() {
        return this.nameSuffixUnmasked;
    }

    @Override
    public String getCompositeName() {
        return this.compositeName;
    }

    @Override
    public String getCompositeNameUnmasked() {
        return this.compositeNameUnmasked;
    }

    @Override
    public String getNoteMessage() {
        return this.noteMessage;
    }

    @Override
    public DateTime getNameChangedDate() {
        return this.nameChangedDate;
    }

    @Override
    public boolean isSuppressName() {
        return this.suppressName;
    }

    @Override
    public Long getVersionNumber() {
        return this.versionNumber;
    }

    @Override
    public String getObjectId() {
        return this.objectId;
    }

    @Override
    public boolean isDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * A builder which can be used to construct {@link EntityName} instances.  Enforces the constraints of the
     * {@link EntityNameContract}.
     */
    public static final class Builder implements Serializable, ModelBuilder, EntityNameContract {

        private String nameSuffix;
        private String entityId;
        private CodedAttribute.Builder nameType;
        private String firstName;
        private String middleName;
        private String lastName;
        private String namePrefix;
        private String nameTitle;
        private String compositeName;
        private String noteMessage;
        private DateTime nameChangedDate;
        private boolean suppressName;
        private Long versionNumber;
        private String objectId;
        private boolean defaultValue;
        private boolean active;
        private String id;

        private Builder() {
        }

        private Builder(String id, String entityId, String firstName, String lastName, boolean suppressName) {
            setId(id);
            setEntityId(entityId);
            setFirstName(firstName);
            setLastName(lastName);
            setSuppressName(suppressName);
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(String id, String entityId, String firstName, String lastName,
                boolean suppressName) {
            return new Builder(id, entityId, firstName, lastName, suppressName);
        }

        public static Builder create(EntityNameContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            Builder builder = create();
            builder.setNameSuffix(contract.getNameSuffix());
            builder.setEntityId(contract.getEntityId());
            if (contract.getNameType() != null) {
                builder.setNameType(CodedAttribute.Builder.create(contract.getNameType()));
            }
            builder.setFirstName(contract.getFirstNameUnmasked());
            builder.setMiddleName(contract.getMiddleNameUnmasked());
            builder.setLastName(contract.getLastNameUnmasked());
            builder.setNamePrefix(contract.getNamePrefixUnmasked());
            builder.setNameTitle(contract.getNameTitleUnmasked());
            builder.setNoteMessage(contract.getNoteMessage());
            builder.setNameChangedDate(contract.getNameChangedDate());
            builder.setSuppressName(contract.isSuppressName());
            builder.setVersionNumber(contract.getVersionNumber());
            builder.setObjectId(contract.getObjectId());
            builder.setDefaultValue(contract.isDefaultValue());
            builder.setActive(contract.isActive());
            builder.setId(contract.getId());
            builder.setCompositeName(contract.getCompositeNameUnmasked());
            return builder;
        }

        public EntityName build() {
            return new EntityName(this);
        }

        @Override
        public String getNameSuffix() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.nameSuffix;
        }

        public void setNameSuffix(String nameSuffix) {
            this.nameSuffix = nameSuffix;
        }

        @Override
        public String getEntityId() {
            return this.entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        @Override
        public CodedAttribute.Builder getNameType() {
            return this.nameType;
        }

        public void setNameType(CodedAttribute.Builder nameType) {
            this.nameType = nameType;
        }

        @Override
        public String getFirstName() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        @Override
        public String getFirstNameUnmasked() {
            return this.firstName;
        }

        @Override
        public String getMiddleName() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.middleName;
        }

        public void setMiddleName(String middleName) {

            this.middleName = middleName;
        }

        @Override
        public String getMiddleNameUnmasked() {
            return this.middleName;
        }

        @Override
        public String getLastName() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public String getLastNameUnmasked() {
            return this.lastName;
        }

        @Override
        public String getNamePrefix() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.namePrefix;
        }

        public void setNamePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public String getNamePrefixUnmasked() {
            return this.namePrefix;
        }

        @Override
        public String getNameTitle() {
            if (isSuppressName()) {
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return this.nameTitle;
        }

        public void setNameTitle(String nameTitle) {
            this.nameTitle = nameTitle;
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
                return KimConstants.RESTRICTED_DATA_MASK;
            }
            return getCompositeNameUnmasked();
        }

        public void setCompositeName(String compositeName) {
            this.compositeName = compositeName;
        }

        @Override
        public String getCompositeNameUnmasked() {
            if (this.compositeName == null) {
                //KULRICE-12360: Display name formatting issue when middle or first name missing
                String lastNameTemp = "";
                String firstNameTemp = "";

                if (StringUtils.isNotBlank(getLastNameUnmasked())) {
                    lastNameTemp = getLastNameUnmasked();
                }
                if (StringUtils.isNotBlank(getFirstNameUnmasked())) {
                    firstNameTemp = getFirstNameUnmasked();
                }
                if (StringUtils.isNotBlank(lastNameTemp) && StringUtils.isNotBlank(firstNameTemp)) {
                    lastNameTemp = lastNameTemp + ", ";
                }

                setCompositeName(lastNameTemp + firstNameTemp
                        + (StringUtils.isBlank(getMiddleNameUnmasked()) ? "" : " " + getMiddleNameUnmasked()));
            }
            return this.compositeName;
        }

        @Override
        public String getNoteMessage() {
            return this.noteMessage;
        }

        public void setNoteMessage(String noteMessage) {
            this.noteMessage = noteMessage;
        }

        @Override
        public DateTime getNameChangedDate() {
            return this.nameChangedDate;
        }

        public void setNameChangedDate(DateTime nameChangedDate) {
            this.nameChangedDate = nameChangedDate;
        }

        @Override
        public boolean isSuppressName() {
            return this.suppressName;
        }

        private void setSuppressName(boolean suppressName) {
            this.suppressName = suppressName;
        }

        @Override
        public Long getVersionNumber() {
            return this.versionNumber;
        }

        public void setVersionNumber(Long versionNumber) {
            this.versionNumber = versionNumber;
        }

        @Override
        public String getObjectId() {
            return this.objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        @Override
        public boolean isDefaultValue() {
            return this.defaultValue;
        }

        public void setDefaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            if (StringUtils.isWhitespace(id)) {
                throw new IllegalArgumentException("id is blank");
            }
            this.id = id;
        }
    }

    static class Constants {
        static final String ROOT_ELEMENT_NAME = "entityName";
        static final String TYPE_NAME = "EntityNameType";
    }

    /**
     * A private class which exposes constants which define the XML element names to use when this object is
     * marshalled to XML.
     */
    static class Elements {
        static final String NAME_SUFFIX = "nameSuffix";
        static final String ENTITY_ID = "entityId";
        static final String NAME_TYPE = "nameType";
        static final String FIRST_NAME = "firstName";
        static final String FIRST_NAME_UNMASKED = "firstNameUnmasked";
        static final String MIDDLE_NAME = "middleName";
        static final String MIDDLE_NAME_UNMASKED = "middleNameUnmasked";
        static final String LAST_NAME = "lastName";
        static final String LAST_NAME_UNMASKED = "lastNameUnmasked";
        static final String NAME_PREFIX = "namePrefix";
        static final String NAME_PREFIX_UNMASKED = "namePrefixUnmasked";
        static final String NAME_TITLE = "nameTitle";
        static final String NAME_TITLE_UNMASKED = "nameTitleUnmasked";
        static final String NAME_SUFFIX_UNMASKED = "nameSuffixUnmasked";
        static final String COMPOSITE_NAME = "compositeName";
        static final String COMPOSITE_NAME_UNMASKED = "compositeNameUnmasked";
        static final String NOTE_MESSAGE = "noteMessage";
        static final String NAME_CHANGED_DATE = "nameChangedDate";
        static final String SUPPRESS_NAME = "suppressName";
        static final String DEFAULT_VALUE = "defaultValue";
        static final String ACTIVE = "active";
        static final String ID = "id";
    }
}
