/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.rest.application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.rest.marshaller.BusinessObjectCSVMessageBodyWriter;
import org.kuali.kfs.sys.rest.marshaller.BusinessObjectJSONMessageBodyWriter;
import org.kuali.kfs.sys.rest.marshaller.BusinessObjectLookupJSONMessageBodyWriter;
import org.kuali.kfs.sys.rest.marshaller.FileMessageBodyWriter;
import org.kuali.kfs.sys.rest.provider.BusinessObjectClassProvider;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class BaseApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> clazzes = new HashSet<>();

    public BaseApiApplication() {
        singletons.add(SpringContext.getBean(BusinessObjectClassProvider.class));
        singletons.add(SpringContext.getBean(BusinessObjectLookupJSONMessageBodyWriter.class));
        singletons.add(SpringContext.getBean(BusinessObjectJSONMessageBodyWriter.class));
        singletons.add(SpringContext.getBean(BusinessObjectCSVMessageBodyWriter.class));
        singletons.add(new FileMessageBodyWriter());
        clazzes.add(MultiPartFeature.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return clazzes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    protected void addClass(Class clazz) {
        clazzes.add(clazz);
    }

    protected void addSingleton(Object singleton) {
        singletons.add(singleton);
    }
}
