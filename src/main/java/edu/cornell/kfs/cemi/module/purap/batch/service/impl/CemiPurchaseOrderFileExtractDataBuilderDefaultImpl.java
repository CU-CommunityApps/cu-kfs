package edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderGoodsLineBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderLineSplitBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderServiceLineBo;
import edu.cornell.kfs.cemi.module.purap.batch.service.CemiPurchaseOrderFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory.CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory;
import edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory.CemiPurchaseOrderGoodsLineBoFactory;
import edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory.CemiPurchaseOrderHeaderBoFactory;
import edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory.CemiPurchaseOrderServiceLineBoFactory;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

public class CemiPurchaseOrderFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiPurchaseOrderFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private PersonService personService;
    private ParameterService parameterService;
    private CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao;
    private String supplierJobRunDateString;
    @SuppressWarnings("unused")
    private boolean maskSensitiveData;

    private String defaultQuantityItemType;
    private String defaultNonQuantityItemType;

    public CemiPurchaseOrderFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final PersonService personService, final ParameterService parameterService,
            final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao,
            final String supplierJobRunDateString, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo.class);
        Validate.notNull(personService, "personService cannot be null");
        Validate.notNull(parameterService, "parameterService cannot be null");
        Validate.notNull(cemiPurchaseOrderExtractDao, "cemiPurchaseOrderExtractDao cannot be null");
        Validate.notBlank(supplierJobRunDateString, "supplierJobRunDateString cannot be blank");
        this.personService = personService;
        this.parameterService = parameterService;
        this.cemiPurchaseOrderExtractDao = cemiPurchaseOrderExtractDao;
        this.supplierJobRunDateString = supplierJobRunDateString;
        this.maskSensitiveData = maskSensitiveData;

        this.defaultQuantityItemType = this.parameterService.getParameterValueAsString(
                KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.DEFAULT_QUANTITY_ITEM_TYPE);
        this.defaultNonQuantityItemType = this.parameterService.getParameterValueAsString(
                KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.DEFAULT_NON_QUANTITY_ITEM_TYPE);
        Validate.validState(StringUtils.isNotBlank(defaultQuantityItemType),
                "Quantity Item Type parameter should not have been blank");
        Validate.validState(StringUtils.isNotBlank(defaultNonQuantityItemType),
                "Non-Quantity Item Type parameter should not have been blank");
    }

    @Override
    public void writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage(
            final Iterator<PurchaseOrderDocument> legacyPurchaseOrders) {
        int purchaseOrderCount = 0;
        int tabRowCount = 0;
        for (final PurchaseOrderDocument purchaseOrderDocument : IteratorUtils.asIterable(legacyPurchaseOrders)) {
            purchaseOrderCount++;
            if (purchaseOrderCount % 1000 == 0) {
                LOG.info("writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage, Processing {} "
                        + "Purchase Orders and counting...", purchaseOrderCount);
            }
            tabRowCount += writePurchaseOrderDataToIntermediateStorage(purchaseOrderDocument);
        }

        LOG.info("writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage, Finished writing {} "
                + "data lines from {} Purchase Orders ", tabRowCount, purchaseOrderCount);
    }

    private int writePurchaseOrderDataToIntermediateStorage(final PurchaseOrderDocument purchaseOrderDocument) {
        final List<PurchaseOrderItem> openQuantityItemLines = getOpenQuantityItemLines(purchaseOrderDocument);
        final List<PurchaseOrderItem> openNonQuantityItemLines = getOpenNonQuantityItemLines(purchaseOrderDocument);
        if (openQuantityItemLines.isEmpty() && openNonQuantityItemLines.isEmpty()) {
            LOG.warn("writePurchaseOrderDataToIntermediateStorage, Purchase Order Document {} has no open "
                    + "above-the-line items despite the document itself being open; will exclude this document "
                    + "from the output", purchaseOrderDocument.getDocumentNumber());
            return 0;
        }

        int numLinesWritten = 0;

        final CemiPurchaseOrderHeaderBo headerBo = CemiPurchaseOrderHeaderBoFactory.createHeaderBoFrom(
                purchaseOrderDocument, personService, cemiPurchaseOrderExtractDao, supplierJobRunDateString);
        numLinesWritten += writePurchaseOrderQuantityLinesToIntermediateStorage(headerBo, openQuantityItemLines);
        numLinesWritten += writePurchaseOrderNonQuantityLinesToIntermediateStorage(headerBo, openNonQuantityItemLines);
        return numLinesWritten;
    }

    private int writePurchaseOrderQuantityLinesToIntermediateStorage(final CemiPurchaseOrderHeaderBo headerBo,
            final List<PurchaseOrderItem> openQuantityItemLines) {
        if (openQuantityItemLines.isEmpty()) {
            return 0;
        }

        final CemiPurchaseOrderServiceLineBo emptyServiceLine = CemiPurchaseOrderServiceLineBoFactory
                .createServiceLineBoFrom(headerBo, Optional.empty());
        Validate.validState(!emptyServiceLine.getLineSplits().isEmpty(),
                "Empty service line should have had an empty line split BO present");
        final Pair<CemiPurchaseOrderServiceLineBo, CemiPurchaseOrderLineSplitBo> emptyServiceLineData
                = Pair.of(emptyServiceLine, emptyServiceLine.getLineSplits().get(0));
        int numLinesWritten = 0;

        for (final PurchaseOrderItem quantityItemLine : openQuantityItemLines) {
            final CemiPurchaseOrderGoodsLineBo goodsLine = CemiPurchaseOrderGoodsLineBoFactory
                    .createGoodsLineBoFrom(headerBo, Optional.of(quantityItemLine));
            Validate.isTrue(!goodsLine.getLineSplits().isEmpty(),
                    "Goods line should have had either several line splits or an empty line split BO");
            for (final CemiPurchaseOrderLineSplitBo goodsLineSplit : goodsLine.getLineSplits()) {
                final Pair<CemiPurchaseOrderGoodsLineBo, CemiPurchaseOrderLineSplitBo> goodsLineData
                        = Pair.of(goodsLine, goodsLineSplit);
                final CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo tabRowBo
                        = CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory.createTabRowBoFrom(
                                headerBo, goodsLineData, emptyServiceLineData);
                storeSheetRow(tabRowBo);
                numLinesWritten++;
            }
        }

        return numLinesWritten;
    }

    private int writePurchaseOrderNonQuantityLinesToIntermediateStorage(final CemiPurchaseOrderHeaderBo headerBo,
            final List<PurchaseOrderItem> openNonQuantityItemLines) {
        if (openNonQuantityItemLines.isEmpty()) {
            return 0;
        }

        final CemiPurchaseOrderGoodsLineBo emptyGoodsLine = CemiPurchaseOrderGoodsLineBoFactory
                .createGoodsLineBoFrom(headerBo, Optional.empty());
        Validate.validState(!emptyGoodsLine.getLineSplits().isEmpty(),
                "Empty goods line should have had an empty line split BO present");
        final Pair<CemiPurchaseOrderGoodsLineBo, CemiPurchaseOrderLineSplitBo> emptyGoodsLineData
                = Pair.of(emptyGoodsLine, emptyGoodsLine.getLineSplits().get(0));
        int numLinesWritten = 0;

        for (final PurchaseOrderItem nonQuantityItemLine : openNonQuantityItemLines) {
            final CemiPurchaseOrderServiceLineBo serviceLine = CemiPurchaseOrderServiceLineBoFactory
                    .createServiceLineBoFrom(headerBo, Optional.of(nonQuantityItemLine));
            Validate.isTrue(!serviceLine.getLineSplits().isEmpty(),
                    "Service line should have had either several line splits or an empty line split BO");
            for (final CemiPurchaseOrderLineSplitBo serviceLineSplit : serviceLine.getLineSplits()) {
                final Pair<CemiPurchaseOrderServiceLineBo, CemiPurchaseOrderLineSplitBo> serviceLineData
                        = Pair.of(serviceLine, serviceLineSplit);
                final CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo tabRowBo
                        = CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory.createTabRowBoFrom(
                                headerBo, emptyGoodsLineData, serviceLineData);
                storeSheetRow(tabRowBo);
                numLinesWritten++;
            }
        }

        return numLinesWritten;
    }

    private List<PurchaseOrderItem> getOpenQuantityItemLines(final PurchaseOrderDocument purchaseOrderDocument) {
        return getOpenItemLines(purchaseOrderDocument, defaultQuantityItemType);
    }

    private List<PurchaseOrderItem> getOpenNonQuantityItemLines(final PurchaseOrderDocument purchaseOrderDocument) {
        return getOpenItemLines(purchaseOrderDocument, defaultNonQuantityItemType);
    }

    @SuppressWarnings("deprecation")
    private List<PurchaseOrderItem> getOpenItemLines(final PurchaseOrderDocument purchaseOrderDocument,
            final String itemTypeCode) {
        final List<?> items = purchaseOrderDocument.getItems();
        return items.stream()
                .map(PurchaseOrderItem.class::cast)
                .filter(item -> StringUtils.equals(item.getItemTypeCode(), itemTypeCode))
                .filter(PurchaseOrderItem::isItemActiveIndicator)
                .filter(this::isOpenItem)
                .sorted(Comparator.comparing(PurchaseOrderItem::getItemLineNumber))
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean isOpenItem(final PurchaseOrderItem purchaseOrderItem) {
        final KualiDecimal outstandingEncumberedAmount = purchaseOrderItem.getItemOutstandingEncumberedAmount();
        return outstandingEncumberedAmount != null && outstandingEncumberedAmount.isGreaterThan(KualiDecimal.ZERO);
    }

}
