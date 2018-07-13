# faas-client
FaaS client for invoking lambdas via the eventsource gateway (a.k.a Asgard)

## Preparing data for invocation
```java
String accountId = "le49829325";
String lambdaUUID = "545fc8a2-b9d9-4e76-9b55-17b38e6181c9";
String externalSystem = "botStudio";
String apiKey = "d7e8ddd8995344cb8a8373a060958e7f";
String apiSecret = "b16d4dee1f7ab6e4";

//Set header
Map<String, String> headers = new HashMap<String, String>() {{
    put("Accept-Language", "en-US");
}};
```

## Invoking a lambda by UUID via the RESTful API
```java
//Set request payload
User payload = new User();
payload.name = "John Doe";

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

//Create invocation data => Send via body during invocation
FaaSInvocation<User> invocationData = new FaaSInvocation(headers, payload);

try {
    //Initialize FaaS client with the CSDSDomain
    FaaSClient client = FaaSWebClient.getInstance(csdsDomain, apiKey, apiSecret);

    Response[] result = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, Response[].class);
    ...

} catch (FaaSException e) {...}
```