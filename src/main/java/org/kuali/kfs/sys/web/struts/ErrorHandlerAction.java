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
package org.kuali.kfs.sys.web.struts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.DisplayMessageException;

import edu.cornell.kfs.sys.CUKFSConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorHandlerAction extends Action {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.debug("execute() started");

        final Exception exception = (Exception) request.getAttribute(Globals.EXCEPTION_KEY);

        if (exception instanceof DisplayMessageException) {
            request.setAttribute("message", exception.getMessage());
            return mapping.findForward("display");
        }

        final Environment environment = SpringContext.getBean(Environment.class);
        /*
         * CU Customization KFSPTS-25264 display the "Our Apologies" screen when in production mode or when app spider testing is enabled
         */
        if (environment.isProductionEnvironment() || isAppSpiderTestingEnabled()) {
            return mapping.findForward("prd");
        } else {
            return mapping.findForward("tst");
        }
    }

    private boolean isAppSpiderTestingEnabled() {
        boolean appSpiderEnabled =  KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(CUKFSConstants.CU_APPSPIDER_ENABLED_KEY);
        LOG.debug("isAppSpiderTestingEnabled, returning {}", appSpiderEnabled);
        return appSpiderEnabled;
    }
}
