package com.liveperson.faas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.exception.CsdsRetrievalException;
import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.security.types.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtBearerGenerator implements BearerGenerator {

    static Logger logger = LogManager.getLogger();
    String accountId;
    String clientId;
    String clientSecret;
    private RestClient restClient;
    private CsdsClient csdsClient;
    private String currentJwt;
    private AuthExpiryTester authExpiryTester;

    public JwtBearerGenerator(RestClient restClient, CsdsClient csdsClient, String accountId,
                              String clientId,
                              String clientSecret) {
        this.restClient = restClient;
        this.csdsClient = csdsClient;
        this.accountId = accountId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authExpiryTester = new JwtExpiryTester();

    }

    @Override
    public String retrieveBearerToken() throws TokenGenerationException {
        if (jwtNotInitializedOrAboutToExpire()) {
            try {
                currentJwt = generateJwt(accountId, clientId, clientSecret);
            } catch (CsdsRetrievalException | IOException e) {
                logger.warn("There was an error retrieving JTW from Server. " + e);
                if (jwtFailedToInitializeOrExpired()) {
                    throw new TokenGenerationException("No valid JWT could be retrieved and current one is expired", e);
                }
            }
        }

        return "Bearer " + this.currentJwt;
    }

    private String generateJwt(String accountId, String clientId, String clientSecret) throws CsdsRetrievalException,
            IOException {
        String sentinelBaseUrl = csdsClient.getDomain("sentinel");
        String jwtUrl = String.format("https://%s/sentinel/api/account/%s/app" +
                        "/token" +
                        "?v=2.0&grant_type=client_credentials&client_id=%s&client_secret=%s",
                sentinelBaseUrl, accountId, clientId,
                clientSecret);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/x-www-form-urlencoded");
        String response = restClient.post(jwtUrl, headers, "");
        ObjectMapper mapper = new ObjectMapper();
        Token accessToken = mapper.readValue(response, Token.class);
        return accessToken.getAccess_token();
    }

    private boolean jwtNotInitializedOrAboutToExpire() {
        return currentJwt == null || authExpiryTester.isAboutToExpire(currentJwt);
    }

    private boolean jwtFailedToInitializeOrExpired() {
        return currentJwt == null || authExpiryTester.isExpired(currentJwt);
    }

}
