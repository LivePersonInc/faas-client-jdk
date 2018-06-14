# faas-client
FaaS client for invoking lambdas via the eventsource gateway (a.k.a Asgard)

## Preparing data for invocation
```java
String accountId = "le49829325";
String lambdaUUID = "545fc8a2-b9d9-4e76-9b55-17b38e6181c9";
String externalSystem = "botStudio";

//oAuth1.0 authorization header
String authHeader = "OAuth oauth_consumer_key=3c5ffb9e07...";

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation();

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};
invocationData.setHeaders(headers);

//Set payload
User user = new User();
user.name = "John Doe";
invocationData.setPayload(user);
```

## Invoking a lambda via the RESTful API
```java
try {
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain);

    User result = client.invoke(externalSystem, authHeader, accountId, lambdaUUID, invocationData, User.class);
    ...

} catch (FaaSException e) {...}
```