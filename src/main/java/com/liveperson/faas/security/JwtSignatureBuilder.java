package com.liveperson.faas.security;

import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.http.RestClient;

public class JwtSignatureBuilder implements AuthSignatureBuilder {

    BearerGenerator bearerGenerator;

    public JwtSignatureBuilder(RestClient client, CsdsClient csdsClient, String accountId,
                               String clientId, String clientSecret) {
        bearerGenerator = new JwtBearerGenerator(client, csdsClient, accountId, clientId,
                clientSecret);
    }

    @Override
    public String getAuthHeader() throws TokenGenerationException {
        return bearerGenerator.retrieveBearerToken();
    }


}
