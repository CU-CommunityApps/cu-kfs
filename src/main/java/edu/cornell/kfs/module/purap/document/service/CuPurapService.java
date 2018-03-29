package edu.cornell.kfs.module.purap.document.service;

import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public interface CuPurapService extends PurapService {

    KualiDecimal getApoLimit(PurchasingDocument purchasingDocument);

}
