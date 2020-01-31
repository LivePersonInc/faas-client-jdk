package com.liveperson.faas.csds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.csds.types.BaseURI;
import com.liveperson.faas.csds.types.BaseURIs;
import com.liveperson.faas.exception.CsdsRetrievalException;
import com.liveperson.faas.http.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CsdsWebClient implements CsdsClient {

    private List<BaseURI> cachedDomains;
    private LocalDateTime cacheExpiryDate;
    private RestClient restClient;
    private String accountId;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CsdsWebClient(RestClient restClient, String accountId) {
        this.accountId = accountId;
        this.restClient = restClient;
    }

    @Override
    public String getDomain(String service) throws CsdsRetrievalException {
        return getCachedDomains()
                .stream()
                .filter(domain -> domain.getService().equals(service))
                .findFirst()
                .map(BaseURI::getBaseURI)
                .orElseThrow(() -> new CsdsRetrievalException(String.format(
                        "Service with name %s could not be found " +
                                "in domains", service)));
    }

    private List<BaseURI> getCachedDomains() throws CsdsRetrievalException {
        if (cachedDomainsInitialized() && !cachedDomainsExpired()) {
            return cachedDomains;
        } else {
            BaseURIs baseURIs;
            String csdsRetrivalUrl = String.format("http://%s/api/account/%s/service/baseURI.json?version=1.0",
                    getCsdsDomain(), accountId);
            String response = null;
            try {
                response = restClient.get(csdsRetrivalUrl, Collections.singletonMap("Content-type", "application" +
                        "/json"));
                baseURIs = objectMapper.readValue(response, BaseURIs.class);
            } catch (IOException e) {
                throw new CsdsRetrievalException("A problem occurred in get request or in parsing of Csds domains", e);
            }
            cachedDomains = baseURIs.getBaseURIs();
            cacheExpiryDate = (LocalDateTime.now()).plusHours(2);
            return cachedDomains;
        }

    }

    private String getCsdsDomain() {
        if (accountId.startsWith("le") || accountId.startsWith("qa")) {
            return "hc1n.dev.lprnd.net";
        }
        if (accountId.startsWith("fr")) {
            return "adminlogin-z0-intg.liveperson.net";
        }
        // alpha/production
        return "api.liveperson.net";
    }

    private boolean cachedDomainsInitialized() {
        return cachedDomains != null;
    }

    private boolean cachedDomainsExpired() {
        return cacheExpiryDate.isBefore(LocalDateTime.now());
    }
}

