package edu.cornell.kfs.module.cam.web.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

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
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "DELETE, PUT, POST, GET, OPTIONS");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "cognito_id_token, capital_asset_scanning_api_key, Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers");
        if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
            chain.doFilter(request, response);
        } else {
            this.checkAuthorization(httpServletRequest, httpServletResponse, chain);
        }
    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (isAuthorized(request)) {
                chain.doFilter(request, response);
            } else {
                LOG.warn("CapAssetInventoryApi checkAuthorization unauthorized " + request.getMethod() + " " + request.getPathInfo());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String cognitoIdToken = request.getHeader(CuCamsConstants.CapAssetApi.COGNITO_ID_TOKEN);
        LOG.info(cognitoIdToken);
        String cognitoPublicKeyPath = CuCamsConstants.CapAssetApi.COGNITO_PUBLIC_KEY_FILE_RELATIVE_PATH;
        String fileContents = new String(Files.readAllBytes(Paths.get(cognitoPublicKeyPath)));
        LOG.info(fileContents);

        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(fileContents, JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        LOG.info(jsonObject);

        String keyId = jsonObject.get("kid").getAsString();
        String keyAlg = jsonObject.get("alg").getAsString();
        String keyType = jsonObject.get("kty").getAsString();
        String keyN = jsonObject.get("n").getAsString();
        String keyE = jsonObject.get("e").getAsString();
        LOG.info(keyId);
        LOG.info(keyAlg);
        LOG.info(keyType);

        //decode keyN
        Base64.Decoder decoder = keyN.endsWith("=") || keyN.contains("+") || keyN.contains("/") ? Base64.getDecoder() : Base64.getUrlDecoder();
        byte[] keyNDecoded = decoder.decode(keyN);

        BigInteger modulus = new BigInteger(1, keyNDecoded);

        //decode keyE
        Base64.Decoder decoder2 = keyE.endsWith("=") || keyE.contains("+") || keyE.contains("/") ? Base64.getDecoder() : Base64.getUrlDecoder();
        byte[] keyEDecoded = decoder2.decode(keyE);

        BigInteger publicExponent = new BigInteger(1, keyEDecoded);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        LOG.info(publicKey.toString());

        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
        String iss = String.format("https://cognito-idp.%s.amazonaws.com/%s", "us-east-1", "us-east-1_PF3qpyCGu");

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(iss)
                .withClaim("token_use", "id")
                .build();

        DecodedJWT jwt = verifier.verify(cognitoIdToken);
        String email = jwt.getClaim("email").asString();
        LOG.info("CapAssetInventory Authorized %s", email);

        String correctApiKey = getWebServiceCredentialService().getWebServiceCredentialValue(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_CREDENTIAL_GROUP_CODE,
                CuCamsConstants.CapAssetApi.CAPITAL_ASSET_API_KEY_CREDENTIAL_NAME);
        String submittedApiKey = request.getHeader(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_API_KEY_CREDENTIAL_NAME);
        return !StringUtils.isEmpty(correctApiKey) && !StringUtils.isEmpty(submittedApiKey) && submittedApiKey.equals(correctApiKey);
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

}
