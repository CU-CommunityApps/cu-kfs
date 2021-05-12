package edu.cornell.kfs.module.cam.web.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class CuCapAssetInventoryServerAuthFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger(CuCapAssetInventoryServerAuthFilter.class);

    private WebServiceCredentialService webServiceCredentialService;
    private ConfigurationService configurationService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "DELETE, PUT, POST, GET, OPTIONS");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "cognito_id_token, Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers");
        if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
            chain.doFilter(request, response);
        } else {
            this.checkAuthorization(httpServletRequest, httpServletResponse, chain);
        }
    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        try {
            if (isAuthorized(request)) {
                chain.doFilter(request, response);
            } else {
                LOG.warn("checkAuthorization unauthorized {} {}", request.getMethod(), request.getPathInfo());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
            }
        } catch (Exception ex) {
            LOG.error("checkAuthorization", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String cognitoIdToken = request.getHeader(CuCamsConstants.CapAssetApi.COGNITO_ID_TOKEN);
        PublicKey cognitoUserPoolPublicKey = getCognitoUserPoolPublicKey();
        if (ObjectUtils.isNull(cognitoUserPoolPublicKey)) {
            return false;
        }

        String cognitoUserPoolIssuerUrl = getConfigurationService().getPropertyValueAsString(CuCamsConstants.CapAssetApi.ConfigurationProperties.COGNITO_USER_POOL_ISSUER_URL);
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) cognitoUserPoolPublicKey, null);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(cognitoUserPoolIssuerUrl)
                .withClaim(CuCamsConstants.CapAssetApi.TOKEN_USE, CuCamsConstants.CapAssetApi.ID)
                .build();

        DecodedJWT jwt = verifier.verify(cognitoIdToken);
        String email = jwt.getClaim(CuCamsConstants.CapAssetApi.EMAIL).asString();
        LOG.info("CapAssetInventory Authorized {}", email);
        return true;
    }

    private PublicKey getCognitoUserPoolPublicKey() {
        try {
            String cognitoPublicKey = getConfigurationService().getPropertyValueAsString(CuCamsConstants.CapAssetApi.ConfigurationProperties.COGNITO_PUBLIC_KEY_JSON);
            JsonObject publicKeyJson = parsePublicKeyJson(cognitoPublicKey);
            return decodePublicKey(publicKeyJson);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            LOG.error("getCognitoUserPoolPublicKey", ex);
        }
        return null;
    }

    private JsonObject parsePublicKeyJson(String rawPublicKey) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(rawPublicKey, JsonElement.class);
        return jsonElement.getAsJsonObject();
    }

    private PublicKey decodePublicKey(JsonObject publicKeyJson) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String keyModuloN = publicKeyJson.get(CuCamsConstants.CapAssetApi.COGNITO_PUBLIC_KEY_MODULO).getAsString();
        String keyExponentE = publicKeyJson.get(CuCamsConstants.CapAssetApi.COGNITO_PUBLIC_KEY_EXPONENT).getAsString();

        Base64.Decoder decoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, decoder.decode(keyModuloN));
        BigInteger publicExponent = new BigInteger(1, decoder.decode(keyExponentE));

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        return KeyFactory.getInstance(CuCamsConstants.CapAssetApi.RSA).generatePublic(publicKeySpec);
    }

    @Override
    public void destroy() {
    }

    protected WebServiceCredentialService getWebServiceCredentialService() {
        if (ObjectUtils.isNull(this.webServiceCredentialService)) {
            this.webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        }

        return this.webServiceCredentialService;
    }

    protected ConfigurationService getConfigurationService() {
        if (ObjectUtils.isNull(this.configurationService)) {
            this.configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return this.configurationService;
    }

}
