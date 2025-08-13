// Source code is decompiled from a .class file using FernFlower decompiler.
package org.kuali.kfs.sys.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.DisplayMessageException;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ErrorHandlerAction extends Action {
   private static final Logger LOG = LogManager.getLogger();

   public ErrorHandlerAction() {
   }

   public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
      LOG.debug("execute() started");
      Exception exception = (Exception)request.getAttribute("org.apache.struts.action.EXCEPTION");
      if (exception instanceof DisplayMessageException) {
         request.setAttribute("message", exception.getMessage());
         return mapping.findForward("display");
      } else {
         Environment environment = (Environment)SpringContext.getBean(Environment.class);
         
         /*
          * CU Customization KFSPTS-25264 display the "Our Apologies" screen when in production mode or when app spider testing is enabled
          */
         return environment.isProductionEnvironment() || isAppSpiderTestingEnabled() ? mapping.findForward("prd") : mapping.findForward("tst");
      }
   }
   
   private boolean isAppSpiderTestingEnabled() {
      boolean appSpiderEnabled =  KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(CUKFSConstants.CU_APPSPIDER_ENABLED_KEY);
      LOG.debug("isAppSpiderTestingEnabled, returning {}", appSpiderEnabled);
      return appSpiderEnabled;
   }
}
