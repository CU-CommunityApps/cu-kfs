/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.kim.impl.responsibility;

import org.kuali.kfs.kim.api.common.template.TemplateContract;
import org.kuali.kfs.kim.impl.common.template.Template;

/* Cornell Customization: backport redis*/
public class ResponsibilityTemplate extends Template {

    private static final long serialVersionUID = 1L;
    
    public static final String CACHE_NAME = "ResponsibilityTemplate";

    private String id;

    /**
     * Converts a immutable object to its mutable counterpart
     *
     * @param im immutable object
     * @return the mutable bo
     */
    public static ResponsibilityTemplate from(TemplateContract im) {
        if (im == null) {
            return null;
        }
        ResponsibilityTemplate bo = new ResponsibilityTemplate();
        bo.id = im.getId();
        bo.setNamespaceCode(im.getNamespaceCode());
        bo.setName(im.getName());
        bo.setDescription(im.getDescription());
        bo.setActive(im.isActive());
        bo.setKimTypeId(im.getKimTypeId());
        bo.setVersionNumber(im.getVersionNumber());
        bo.setObjectId(im.getObjectId());
        return bo;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
