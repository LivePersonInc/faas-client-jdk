package com.liveperson.faas.security;

import com.liveperson.faas.client.FaaSClient;
import com.liveperson.faas.client.FaaSEvent;
import com.liveperson.faas.client.FaaSWebClient;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.csds.CsdsWebClient;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.http.RestClient;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        String accountId = "18311561";
        RestClient restClient = new DefaultRestClient();
        CsdsClient csdsClient = new CsdsWebClient(restClient, accountId);
        AuthSignatureBuilder authSignatureBuilder = new JwtSignatureBuilder(restClient, csdsClient, "18311561",
                "e6b51bb9-026a-4e5d-ad93-445d0eade9b3", "bu3cmlff0ldvnaiutoaegqjegt");
        String accessToken = null;
        OptionalParams optionalParams = new OptionalParams();
        try {
            accessToken = authSignatureBuilder.getAuthHeader();
        } catch (TokenGenerationException e) {
        }
        if (accessToken != null) {
            System.out.println(accessToken);
        }

        FaaSClient client = new FaaSWebClient.Builder(accountId)
                .withClientId("e6b51bb9-026a-4e5d-ad93-445d0eade9b3")
                .withClientSecret("bu3cmlff0ldvnaiutoaegqjegt")
                .build();
        String externalSystem = "ExternalInvocation";

        boolean result = false;
        try {
            result = client.isImplemented(externalSystem, FaaSEvent.ConversationalCommand, optionalParams);
        } catch (FaaSException e) {
            e.printStackTrace();
        }
        System.out.println(result);

        try {
            result = client.isImplemented(externalSystem, FaaSEvent.ConversationalCommand, optionalParams);
        } catch (FaaSException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        // Thread.sleep(65000);

        try {
            result = client.isImplemented(externalSystem, FaaSEvent.ConversationalCommand, optionalParams);
        } catch (FaaSException e) {
            e.printStackTrace();
        }
        System.out.println(result);

        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("state", "Productive"); // Filter lambdas by state ("Draft", "Productive", "Modified", "Marked
        // Undeployed")
        filterMap.put("eventId", "eventId");
        filterMap.put("name", "myFilter");

        // List<LambdaResponse> lambdas = client.getLambdasOfAccount("9000002671", filterMap);

    }
}
