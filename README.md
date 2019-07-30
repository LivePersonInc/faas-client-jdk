# FaaS-Client (Java)
In order to invoke a lambda that has been deployed via LivePerson Functions, this Java based FaaS-Client can be used.
The client is able to read all available lambdas & trigger an invocation based on lambda UUID or eventId.

For details about LivePerson Functions & its API have a look at:
* [LivePerson Functions Overview](https://developers.liveperson.com/liveperson-functions-overview.html)
* [Swagger API doc of Function-Management](https://va-a.faasui.liveperson.net/fm/api-docs/)
* [Swagger API doc of Function-Deployment](https://va-a.faasui.liveperson.net/dm/api-docs/)
* [Swagger API doc of Function-Invocation](https://va-a.faasgw.liveperson.net/evg/api-docs/)

## Preparing data for RESTful API calls
```java
String accountId = "YOUR_ACCOUNT_ID";
String lambdaUUID = "545fc8a2-b9d9-4e76-9b55-17b38e6181c9";
String externalSystem = "botStudio";
String apiKey = "YOUR_API_KEY";
String apiSecret = "YOUR_API_SECRET";
```

## Fetching lambdas of account
```java
try {
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain, apiKey, apiSecret);
    
    HashMap<String,String> filterMap = new HashMap<String, String>();
    filterMap.put("state", "Draft") // Filter lambdas by state ("Draft", "Productive", "Modified", "Marked Undeployed")
    filterMap.put("eventId", FaaSEvent.ControllerBotMessagingNewConversation.toString()); // Filter lambdas by event name (also substring)
    filterMap.put("name", "lambda_substring") // Filter lambdas by name substring

    List<LambdaResponse> lambdas = client.getLambdasOfAccount(accountId, externalSystem, filterMap);
    ...

} catch (FaaSException e) {...}
```


## Invoking a lambda by UUID via the RESTful API
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
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain, apiKey, apiSecret);

    User result = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, User.class);
    ...

} catch (FaaSException e) {...}
```

## Invoking a lambda by event via the RESTful API
When multiple lambdas are invoked by event, the result will always be an array of the following structure:
```java
public class Response {
    //The invoked lambda UUID
    public String uuid;
    
    //The invocation timestamp
    public Date timestamp;
    
    //The result of the specific lambda, i.e. an instance of the User class
    public User result;
}
```

The invocation is then done in the following way:
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
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain, apiKey, apiSecret);
    
    //Check if lambdas are implemented for event
    boolean isImplemented = client.isImplemented(externalSystem, accountId, FaaSEvent.DenverPostSurveyEmailTranscript);

    if(isImplemented){
        //Invoke lambdas for event
        Response[] result = client.invoke(externalSystem, accountId, FaaSEvent.DenverPostSurveyEmailTranscript, invocationData, Response[].class);
    }
    ...

} catch (FaaSException e) {...}
```
