package edu.cornell.kfs.module.cam.web.struts;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.document.service.GlLineService;
import org.kuali.kfs.module.cam.web.struts.CapitalAssetInformationAction;
import org.kuali.kfs.module.cam.web.struts.CapitalAssetInformationForm;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.Collection;
import java.util.List;

public class CuCapitalAssetInformationAction extends CapitalAssetInformationAction {

    @Override
    protected void prepareRecordsForDisplay(final CapitalAssetInformationForm capitalAssetForm, final GeneralLedgerEntry entry) {
        final GlLineService glLineService = SpringContext.getBean(GlLineService.class);

        entry.setSelected(true);
        capitalAssetForm.setGeneralLedgerEntry(entry);
        capitalAssetForm.setPrimaryGlAccountId(entry.getGeneralLedgerAccountIdentifier());

        List<CapitalAssetInformation> capitalAssetInformations = glLineService.findAllCapitalAssetInformation(entry.getDocumentNumber());
        Collection<GeneralLedgerEntry> glEntries = glLineService.findAllGeneralLedgerEntry(entry.getDocumentNumber());

        if(!capitalAssetInformations.isEmpty() && capitalAssetInformations.size() != glEntries.size()){
            // we need to generate missing capital asset info
            glLineService.setupMissingCapitalAssetInformation(entry.getDocumentNumber());
        }

        List<CapitalAssetInformation> capitalAssetInformation = glLineService.findCapitalAssetInformationForGLLine(entry);

        // KFSMI-9881
        // For GL Entries without capital asset information (ex: loaded by enterprise feed or Vendor Credit Memo),
        // we need to create that information (at least a shell record) for the Capital Asset Information screen
        // to render and subsequent processing to occur successfully.
        if (capitalAssetInformation.isEmpty()) {
            glLineService.setupCapitalAssetInformation(entry);
            capitalAssetInformation = glLineService.findCapitalAssetInformationForGLLine(entry);
        }

        capitalAssetForm.setCapitalAssetInformation(capitalAssetInformation);
    }

}

