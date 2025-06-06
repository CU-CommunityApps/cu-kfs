package edu.cornell.kfs.module.cam;

public class CuCamsConstants {
    public static class Parameters {
        public static final String RE_USE_RETIRED_ASSET_TAG_NUMBER = "RE_USE_RETIRED_ASSET_TAG_NUMBER";
    }

    public static final class CapAssetApi {
        public static final String ASSET_TAG_NOT_FOUND = "Asset Tag not found";
        public static final String ERROR = "error";
        public static final String COGNITO_PUBLIC_KEY_MODULO = "n";
        public static final String COGNITO_PUBLIC_KEY_EXPONENT = "e";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String BAD_REQUEST = "Invalid Request";
        public static final String COGNITO_ID_TOKEN = "cognito_id_token";
        public static final String CAPITAL_ASSET_KFS_API_DESCRIPTION = "The Cornell Capital Asset website uses this resource to scan capital assets data.";
        public static final String BUILDING_NAME = "building_name";
        public static final String BUILDING_CODE = "building_code";
        public static final String CONDITION_CODE = "condition_code";
        public static final String CONDITION_NAME = "condition_name";
        public static final String ROOM_NUMBER = "room_number";
        public static final String NETID = "netid";
        public static final String AMOUNT = "amount";
        public static final String NULL = "null";
        public static final String CAMPUS_TAG_NUMBER = "campusTagNumber";
        public static final String CAMPUS_TAG_NUMBER_ATTRIBUTE = "campus_tag_number";
        public static final String ACTIVE = "active";
        public static final String YES = "Y";
        public static final String RSA = "RSA";
        public static final String TOKEN_USE = "token_use";
        public static final String ID = "id";
        public static final String EMAIL = "email";
        public static final String CAMPUS_CODE = "campus_code";
        public static final String CAMPUS_CODE_PARAMETER = "campusCode";
        public static final String BUILDING_CODE_PARAMETER = "buildingCode";
        public static final String ASSET_TAG_PARAMETER = "assetTag";
        public static final String SERIAL_NUMBER = "serial_number";
        public static final String CAPITAL_ASSET_DESCRIPTION = "capital_asset_description";
        public static final String LAST_UPDATED = "last_updated";
        public static final String LAST_INVENTORY_DATE = "last_inventory_date";
        public static final String ORGANIZATION_INVENTORY_NAME = "organization_inventory_name";
        public static final String CAPITAL_ASSET_NUMBER = "capital_asset_number";
        public static final String ASSET_NOT_FOUND_ERROR = "Error Scanning Asset Tag #";
        public static final long UPLOAD_ROW_NUMBER = 1L;
        public static final String KFS_SYSTEM_USER = "KFS";
        public static final String COLON_SPACE = ": ";

        public static final class ConfigurationProperties {
            public static final String COGNITO_USER_POOL_ISSUER_URL = "cu.kfs.cams.api.cognito.user.pool.url";
            public static final String COGNITO_PUBLIC_KEY_JSON = "cu.kfs.cams.api.public.key.json";
        }
    }

    public static final class AssetLocationTypeLabel {
        public static final String OFF_CAMPUS = "Off-Campus";
        public static final String BORROWER = "Borrower";
        public static final String BORROWER_STORAGE = "Borrower Storage";
        public static final String RETIREMENT = "Retirement";
    }
}
