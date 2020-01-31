package com.liveperson.faas.metriccollector;

public class NullMetricCollector implements MetricCollector {

    @Override
    public void onInvokeByEventSuccess(String externalSystem, double requestDurationInSec, String event,
                                       String accountId) {

    }

    @Override
    public void onInvokeByEventFailure(String externalSystem, double requestDurationInSec, String event,
                                       String accountId, int statusCode, Exception ex) {

    }

    @Override
    public void onInvokeByUUIDSuccess(String externalSystem, double requestDurationInSec, String UUID,
                                      String accountId) {

    }

    @Override
    public void onInvokeByUUIDFailure(String externalSystem, double requestDurationInSec, String UUID,
                                      String accountId, int statusCode, Exception ex) {

    }

    @Override
    public void onGetLambdasSuccess(String userId, double requestDurationInSec, String accountId) {

    }

    @Override
    public void onGetLambdasFailure(String userId, double requestDurationInSec, String accountId,
                                    int statusCode, Exception ex) {

    }

    @Override
    public void onIsImplementedSuccess(String externalSystem, double requestDurationInSc, String event,
                                       String accountId) {

    }

    @Override
    public void onIsImplementedFailure(String externalSystem, double requestDurationInSc, String event,
                                       String accountId, int statusCode, Exception ex) {

    }
}
