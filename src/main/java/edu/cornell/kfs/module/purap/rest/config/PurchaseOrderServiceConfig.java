package edu.cornell.kfs.module.purap.rest.config;

import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.document.service.VendorService;
import edu.cornell.kfs.module.purap.web.filter.PurchaseOrderAuthFilter;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseOrderServiceConfig {

    @Bean
    public DataDictionaryService dataDictionaryService() {
        return SpringContext.getBean(DataDictionaryService.class);
    }

    @Bean
    public PurchaseOrderService purchaseOrderService() {
        return SpringContext.getBean(PurchaseOrderService.class);
    }

    @Bean
    public VendorService vendorService() {
        return SpringContext.getBean(VendorService.class);
    }

    @Bean
    public WebServiceCredentialService webServiceCredentialService() {
        return SpringContext.getBean(WebServiceCredentialService.class);
    }

    @Bean
    public PurchaseOrderAuthFilter purchaseOrderAuthFilter() {
        return new PurchaseOrderAuthFilter();
    }
}
