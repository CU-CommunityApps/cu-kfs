package edu.cornell.kfs.module.cam.businessobject.options;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuAssetLocationTypeValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1543898928013834256L;

    @Override
    public List<KeyValue> getKeyValues() {
        return Stream.of(
                Pair.of(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING),
                Pair.of(CamsConstants.AssetLocationTypeCode.OFF_CAMPUS,
                        CuCamsConstants.AssetLocationTypeLabel.OFF_CAMPUS),
                Pair.of(CamsConstants.AssetLocationTypeCode.BORROWER,
                        CuCamsConstants.AssetLocationTypeLabel.BORROWER),
                Pair.of(CamsConstants.AssetLocationTypeCode.BORROWER_STORAGE,
                        CuCamsConstants.AssetLocationTypeLabel.BORROWER_STORAGE),
                Pair.of(CamsConstants.AssetLocationTypeCode.RETIREMENT,
                        CuCamsConstants.AssetLocationTypeLabel.RETIREMENT))
                .map(this::convertPairIntoKeyValueWithKeyInLabel)
                .collect(Collectors.toList());
    }

    private KeyValue convertPairIntoKeyValueWithKeyInLabel(Pair<String, String> keyValuePair) {
        String label = StringUtils.isNotBlank(keyValuePair.getValue())
                ? keyValuePair.getKey() + CUKFSConstants.PADDED_HYPHEN + keyValuePair.getValue()
                : KFSConstants.EMPTY_STRING;
        return new ConcreteKeyValue(keyValuePair.getKey(), label);
    }

}
