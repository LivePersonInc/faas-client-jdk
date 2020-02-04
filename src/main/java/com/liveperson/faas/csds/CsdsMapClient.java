package com.liveperson.faas.csds;

import com.liveperson.faas.exception.CsdsRetrievalException;

import java.util.Map;

public class CsdsMapClient implements CsdsClient {
    Map<String, String> serviceMap;

    public CsdsMapClient(Map<String, String> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public String getDomain(String service) throws CsdsRetrievalException {
        if (serviceMap.get(service) == null) {
            throw new CsdsRetrievalException(String.format(
                    "Service with name %s could not be found " +
                            "in domains", service));
        }
        return serviceMap.get(service);
    }
}
