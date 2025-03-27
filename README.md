
[![Build Status](https://travis-ci.com/LivePersonInc/faas-client-jdk.svg?branch=develop)](https://travis-ci.com/LivePersonInc/faas-client-jdk)

# Functions-Client (Java)   ![Alt text](logo.png "Logo")

The client can be used for invoking lambdas that have been deployed on LivePerson Functions site.
It offers functionality to retrieve all lambdas and to invoke them via lambda UUID or event IDs.

For more details on LivePerson Functions & its API have a look at:

* [LivePerson Functions Overview](https://developers.liveperson.com/liveperson-functions-overview.html)

## 🚨 Important Project Update: Functions V2 compatibility released! 🚨

We are excited to announce a **major update** to our client: **Functions V2** is now available! 🎉

### What's New?

* **Naming Conventions:** For Functions V1, "Lambda" is used instead of "Function." For V2, "Function" is used, and any reference to "Lambda" refers to the Functions Platform V1.
* **Client Compatibility:** The client is fully compatible with both Functions V1 and V2. It has been designed to minimize the need for changes. The client will automatically call the appropriate platform (V1 or V2) based on your account's CSDS domain for the "Invoke" and "isImplemented" methods.
* **Getting Functions/Lambdas:** To retrieve functions/lambdas, you need to call the appropriate method based on your account version (V1 or V2). A method, *isV2Domain*, is provided to help with this. 
* **Error Response Changes:** The error response body returned by the V2 platform has changed. For more details, refer to the [Exception Handling](#exception-handling) section.
* **Updated/Added Methods:**
  * *getFunctions* – Now required for retrieving V2 functions as a *FunctionResponse* object.
  * *isV2Domain* – Determines whether the accounts associated with the client instance have a V2 domain from the CSDS.
  * *ExternalSystem* has been renamed to *LpEventSource*.
  * *FaaSError* contains now the V2 error while the *FaaSErrorV1* used for V1.

### How to Use the New Features

The client should continue to function as it did before, automatically calling either the V1 or V2 platform based on the account configuration for the "Invoke" and "isImplemented" methods. If you're handling function errors from the error response body within the client response payload, be aware that the V2 error format has changed. You'll need to adjust your handling logic accordingly. For more details, refer to the [Exception Handling](#exception-handling) section.

**IMPORTANT**: If you're using *getFunctions*/*getLambdas*, be aware that the function data type has changed in V2. For V2, a list of *FunctionResponse* objects will be returned, while for V1, a list of *LambdaResponse* objects will be returned. Use the *isV2Domain* method to implement logic that calls the appropriate method based on the account version.

### Action Required

* Update the client to Version 2.x.x.
* Adjust your error handling for V2 accounts. For more details, refer to the [Exception Handling](#exception-handling) section.
* Modify your code to handle the response from *getFunctions*/*getLambdas* correctly, in case you're using these methods.

## Adding the client as maven dependency

Go to your project's pom.xml file and add as dependency.

```xml
<dependency>
  <groupId>com.liveperson.faas</groupId>
  <artifactId>functions-client</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Initializing the client

Prerequisite is having an account id as every instance of this client is bound to a single account id.

Initialize the client using its builder.

```java
String accountId = "YOUR_ACCOUNT_ID";
FaasClient.Builder builder = new FaasWebClient.Builder(accountId);
```

Furthermore you have to choose a method of authorization.
Either you provide a client secret and client Id, as we use OAuth 2.0 with client credentials by default, 
or you alternatively pass your own implementation of the `AuthSignatureBuilder` or `AuthDpopSignatureBuilder`.

* [More information on Client Credentials](https://developers.liveperson.com/liveperson-functions-foundations-external-invocation.html#authentication)

```java
String clientId = "clientId";
String clientSecret = "secret";
builder.withClientId(clientId);
builder.withClientSecret(clientSecret);
or
AuthSignatureBuilder authSignatureBuilder = new YourAuthSignatureBuilder();
builder.withAuthSignatureBuilder(authSignatureBuider);
or // Oauth2 + DPoP (only for internal usage)
AuthDPoPSignatureBuilder  authDPoPSignatureBuilder = new YourAuthDPoPSignatureBuilder();
builder.withAuthDPoPSignatureBuilder(authDPoPSignatureBuilder);
```

This is the bare minimum that has to be provided to build the client.

```java
FaasClient faasClient = builder.build();
```

### DPoP authorization

The client supports Oauth2+DPoP authorization ONLY FOR INTERNAL USE in service-to-service. You must provide your implementation of the `AuthDPoPSignatureBuilder` during the initialization.

### Optional fields for builder

Additionally you can pass your own implementations of specific fields to the builder before building the client.
Examples are given below.

<details><summary>RestClient</summary>
<p>

```java
RestClient restClient = new YourRestClient();
builder.withRestClient(restClient);
```

</p>
</details>

<details><summary>CsdsClient</summary>
<p>

There are two ways to set the `CsdsClient` which resolves necessary LP domains for our application.
Either you use a map whose keys represent the service names and whose values represent the domains or you implement the `CsdsClient` interface. 
Using the map reduces the calls to the CSDS endpoint. If no `CsdsClient`is provided, the library provides a default implementation, that caches the domains for two hours.

```java
CsdsClient csdsClient = new YourCsdsClient();
builder.withCsdsClient(CsdsClient csdsClient);
or
Map<String, String> csdsMap = new HashMap<String, String>;
builder.withCsdsMap(csdsMap);
```

</p>
</details>

<details><summary>isImplementedCache</summary>
<p>

You can set the IsImplementedCache for the `isImplemented` method which determines whether there are deployed functions that implement a given event.
By instantiating the client yourself you can set a custom caching time. Otherwise we will default back to 60 seconds.

```java
int cachingTimeInSeconds = 30;
builder.withIsImplementedCache(new IsImplementedCache(cachingTimeInSeconds));
```

</p>
</details>

<details><summary>MetricCollector</summary>
<p>

```java
MetricCollector metricCollector = new YourMetricCollector();
builder.withMetricCollector(metricCollector);
```

</p>
</details>

## Method calls

### Preparing data for RESTful API calls

```java
String functionUUID = "UUID";
String lpEventSource = "botStudio";
```

Optional params allow you to set a timeout for the function calls and to set a requestId. 
The default value for the timeout is 15 seconds.
If you do not provide any requestId it will be autogenerated.
The maximum for the timeout should be 40 seconds, it will be internally split up in 10 seconds of connection timeout (40 * 0.25) and 30 seconds of request timeout (40 * 0.75) as FaaS functions time out after 30 seconds.

```java
OptionalParams optionalParams = new OptionalParams();
optionalParams.setTimeOutInMs(40000)
optionalParams.setRequestId("requestId");
```

### Fetching functions

**You have to use your own authentication method when fetching functions as it relies on OAuth 1.0. / Oauth 2.0 + DPoP**

<details><summary>Fetching functions of account</summary>
<p>

For V1 functions use *getLambdas* method.

```java
try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build();

    HashMap<String,String> filterMap = new HashMap<String, String>();
    filterMap.put("state", "Draft") // Filter lambdas by state ("Draft", "Productive", "Modified", "Marked Undeployed")
    filterMap.put("eventId", FaaSEvent.ControllerBotMessagingNewConversation.toString()); // Filter lambdas by event name (also substring)
    filterMap.put("name", "lambda_substring") // Filter lambdas by name substring

    List<LambdaResponse> lambdas = client.getLambdas(userId, filterMap, optionalParams);
    ...

} catch (FaaSException e) {...}
```

For V2 functions use *getFunctions* method.

```java
try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build();

    HashMap<String,String> filterMap = new HashMap<String, String>();
    filterMap.put("state", "Draft") // Filter lambdas by state ("Draft", "Productive", "Modified", "Marked Undeployed")
    filterMap.put("eventId", FaaSEvent.ControllerBotMessagingNewConversation.toString()); // Filter lambdas by event name (also substring)
    filterMap.put("functionName", "lambda_substring") // Filter lambdas by name substring

    List<FunctionResponse> lambdas = client.getFunctions(userId, filterMap, optionalParams);
    ...

} catch (FaaSException e) {...}
```

</p>
</details>

### Invoking function by UUID

<details><summary>Invoking a function by UUID with response</summary>
<p>

```java
//Set request payload
User payload = new User();
payload.name = "John Doe";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);

try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build()
    User result = client.invokeByUUID(lpEventSource, functionUUID, invocationData, User.class, optionalParams);
    //Or call it with a requestID:
    String requestId = "requestId";
    User result = client.invokeByUUID(lpEventSource, functionUUID, invocationData, User.class, requestId, optionalParams);
    ...

} catch (FaaSException e) {...}
```

</p>
</details>

<details><summary>Invoking a lambda by UUID without response</summary>
<p>

```java
//Set request payload
User payload = new User();
payload.name = "John Doe";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);
try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build()
    client.invokeByUUID(lpEventSource, functionUUID, invocationData, optionalParams);

} catch (FaaSException e) {...}
```

</p>
</details>

### Invoking lambda by Event

Calling by event invokes all deployed functions that implement the given event.
The result will therefore always be an array of objects the following structure:

```java
public class Response {
    //The invoked lambda UUID
    public String uuid;

    //The invocation timestamp
    public Date timestamp;

    //The result of the specific lambda, i.e. an instance of the User class
    //This has to changed according to your wanted return type
    public User result;
}
```

<details><summary>Invoking a function by event with response</summary>
<p>

```java
//Set request payload
User payload = new User();
payload.name = "John Doe";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);

try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build()

    //Check if lambdas are implemented for event
    boolean isImplemented = client.isImplemented(lpEventSource, FaaSEvent.DenverPostSurveyEmailTranscript);

    if(isImplemented){
        //Invoke lambdas for event
        Response[] result = client.invokeByEvent(lpEventSource, FaaSEvent.DenverPostSurveyEmailTranscript, invocationData, Response[].class, optionalParams);

        // cast to list for convenience
        List<Response> = Arrays.asList(result);
    }
    ...

} catch (FaaSException e) {...}
```

</p>
</details>

<details><summary>Invoking a function by event without response</summary>
<p>

```java
//Set request payload
User payload = new User();
payload.name = "John Doe";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);
try {
    // After setting the builder up, instantiate the client
    FaasClient faasClient = builder.build()

    //Check if lambdas are implemented for event
    boolean isImplemented = client.isImplemented(lpEventSource, FaaSEvent.DenverPostSurveyEmailTranscript);

    if(isImplemented){
        //Invoke lambdas for event
        client.invokeByEvent(lpEventSource, FaaSEvent.DenverPostSurveyEmailTranscript, invocationData, optionalParams);
    }
    ...

} catch (FaaSException e) {...}
```

</p>
</details>

<details><summary>Invoking a lambda by eventId using custom event</summary>
<p>

Custom events are events that are not yet fully acknowledged and thus not part of the FaasEvent enum. Instead you have to
provide a string.`

```java
     //Check if lambdas are implemented for event
    boolean isImplemented = client.isImplemented(lpEventSource, event);

    //With return type: 
    if(isImplemented) {
        Response[] result = client.invokeByEvent(lpEventSource, event, invocationData, Response[].class, optionalParams);
    }
    //Without return type:
    if(isImplemented) {
        client.invokeByEvent(lpEventSource, event, invocationData, optionalParams);
    }
```

## Logging

Our Application offers logging with Log4j2. For it to work properly you will have to create a log4j2.xml file or a similar configuration file.
It should be placed in src/main/resources. For instance you could use following configuration to log to the console.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="MyApp" packages="">
  <Appenders>
    <Console name="STDOUT" target="SYSTEM_OUT">
      <PatternLayout pattern="%m%n"/>
    </Console>
  </Appenders>
  <Loggers>
    <Root level="error">
      <AppenderRef ref="STDOUT"/>
    </Root>
  </Loggers>
</Configuration>
```

More detailed information about Log4j2 can be found [here](https://logging.apache.org/log4j/2.x/).

</p>
</details>

## Exception Handling

LivePerson Functions can raise different types of exceptions. These exceptions are generally wrapped in a `FaaSException`.

* **`FaaSDetailedException`**: This occurs when the request is rejected by the service, such as when the UUID of the lambda in the invocation request does not exist. To get more details, we recommend using the `getFaaSError` method. For a list of all error codes, see [here](https://developers.liveperson.com/liveperson-functions-external-invocations-error-codes.html).

### For V1 Platform

* **`FaaSLambdaException`** inherits from `FaaSDetailedExceptionV1`. It is thrown during invocations if the error is caused by the implementation of the lambda itself (e.g., the lambda returns an error on purpose or has a timeout).
* We recommend monitoring all exceptions using `e.printStackTrace()` since it provides much more detail than the error message alone.
* Alerting should only be set up for `FaaSException` or `FaaSDetailedExceptionV1`.

### For V2 Platform

* **`FaaSFunctionException`** inherits from `FaaSDetailedException`. It is thrown during invocations if the error is caused by the function implementation itself (e.g., the function returns an error on purpose or has a timeout).
* Similarly, monitor all exceptions using `e.printStackTrace()` to capture more details.
* Alerting should only be set up for `FaaSException` or `FaaSDetailedException`.

### Key Changes in V2

* The error response object structure has changed.
  * **ErrorCode** is now represented as **code**.
  * **errorMessage** is now **message**.

```java
try {
    ...
    //Invoke lambdas for event
    Response[] result = client.invoke(lpEventSource, FaaSEvent.DenverPostSurveyEmailTranscript, invocationData, Response[].class, optionalParams);
    ...

} catch (FaaSLambdaException e){ // V1 Functions
  /**
   * Lambda exceptions occur when the lambda fails due to the implementation.
   * These exceptions are not relevant for alerting, because there are no issues with the service itself.
   */
  e.printStackTrace();

  //Get Details of exception
  FaaSErrorV1 faaSError = e.getFaaSError();

  faaSError.getErrorCode();
  faaSError.getErrorMsg();

} catch (FaaSFunctionException e){ // V2 Functions
  /**
   * Function exceptions occur when the function fails due to the implementation.
   * These exceptions are not relevant for alerting, because there are no issues with the service itself.
   */
  e.printStackTrace();

  //Get Details of exception
  FaaSError faaSError = e.getFaaSError();
 
  faaSError.getCode(); 
  faaSError.getMessage();
}
catch (FaaSDetailedExceptionV1 e) { // V1 functions
  /**
   * Detailed Exceptions contain custom error codes that give more information.
   * These errors are relevant for alerting
   */
  e.printStackTrace();

  //Get Details of exception
  FaaSErrorV1 faaSError = e.getFaaSError();

  faaSError.getErrorCode(); 
  faaSError.getErrorMsg();
}
catch (FaaSDetailedException e) { // Now V2 function
  /**
   * Detailed Exceptions contain custom error codes that give more information.
   * These errors are relevant for alerting
   */
  e.printStackTrace();

  //Get Details of exception
  FaaSError faaSError = e.getFaaSError();

  faaSError.getCode(); 
  faaSError.getMessage();
} catch (FaaSException e){
  /**
   * All general errors are wrapped in a FaaSException (i.e. parsing the JSON response, or 401 on authentication).             *
   * These errors are relevant for alerting.
   */
  e.printStackTrace();
}
```

### Thread-Safety

Client is programmed to be thread-safe so that multiple threads can use one client without causing any problems due to shared data access.

### Collecting metrics

All you need to do to collect metrics is to provide an implementation of our interface metric collector and pass the values passed in the associated methods to your metric monitor.
The methods themselves are called in the appropriate place.

### Test setup

For the system tests to run you will have to create a .env with following values.

```java
FUNCTION_UUID=
ACCOUNT_ID=
CLIENT_ID=
CLIENT_SECRET=
CLIENT_SECRET=
ACCOUNT_ID_V1=
LAMBDA_UUID_V1=
CLIENT_ID_V1=
CLIENT_SECRET_V1=
USER_NAME=
PASSWORD=
```
