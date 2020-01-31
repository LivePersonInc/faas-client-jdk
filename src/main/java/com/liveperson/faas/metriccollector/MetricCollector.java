package com.liveperson.faas.metriccollector;

/**
 * @author sschwarz
 * Metric Collector Interface that provides methods that can be implemented to collect metrics for your metric tool
 */
public interface MetricCollector {

    /**
     * Is invoked when a lambda is invoked by event successfully
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param event                the event that was used for the invocation i.e. all functions implementing this
     *                             event were called
     * @param accountId            AccountId related to function call
     */
    void onInvokeByEventSuccess(String externalSystem, double requestDurationInSec, String event, String accountId);

    /**
     * Is invoked when a lambda invocation by event fails
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param event                the event that was used for the invocation i.e. all functions implementing this
     *                             event were called
     * @param accountId            AccountId related to function call
     * @param ex                   Exception that caused the program to fail
     */
    void onInvokeByEventFailure(String externalSystem, double requestDurationInSec, String event, String accountId,
                                int statusCode, Exception ex);

    /**
     * is invoked when a lambda Invocation by UUID is successfull
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param UUID                 UUID of function that was invoked
     * @param accountId            AccountId related to function call
     */
    void onInvokeByUUIDSuccess(String externalSystem, double requestDurationInSec, String UUID, String accountId);

    /**
     * is invoked when a lambda Invocation by UUID fails
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param UUID                 UUID of function that was invoked
     * @param accountId            AccountId related to function call
     * @param ex                   Exception that caused the program to fail
     */
    void onInvokeByUUIDFailure(String externalSystem, double requestDurationInSec, String UUID, String accountId,
                               int statusCode, Exception ex);

    /**
     * is invoked when all lambdas are retrieved successfully
     *
     * @param userId               User Id related to account for which lambdas are retrieved
     * @param requestDurationInSec time the request took in seconds
     * @param accountId            AccountId related to function call
     */
    void onGetLambdasSuccess(String userId, double requestDurationInSec, String accountId);

    /**
     * is invoked when retrieval of lambdas failed
     *
     * @param userId               User Id related to account for which lambdas are retrieved
     * @param requestDurationInSec time the request took in seconds
     * @param accountId            AccountId related to function call
     * @param ex                   Exception that caused the program to fail
     */
    void onGetLambdasFailure(String userId, double requestDurationInSec, String accountId,
                             int statusCode, Exception ex);

    /**
     * is invoked when implementation of a specific event is successfully inquired
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param event                event for which it was verified whether any lambdas implementing it exist
     * @param accountId            AccountId related to function call
     */
    void onIsImplementedSuccess(String externalSystem, double requestDurationInSec, String event, String accountId);

    /**
     * is invoked when implementation of a specific event is unsuccessfully inquired
     *
     * @param externalSystem       the name of the external system doing the invocation
     * @param requestDurationInSec time the request took in seconds
     * @param event                event for which it was verified whether any lambdas implementing it exist
     * @param accountId            AccountId related to function call
     * @param ex                   Exception that caused the program to fail
     */
    void onIsImplementedFailure(String externalSystem, double requestDurationInSec, String event, String accountId,
                                int statusCode, Exception ex);

}
