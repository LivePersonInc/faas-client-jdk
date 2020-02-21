# Deployment

In order to release a new version of this project to [Maven Central](https://search.maven.org/), you need to have 
access to the "com.liveperson" Domain on Maven Central. See this [thread](https://issues.sonatype.org/browse/OSSRH-54980?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel) for that. 
If your user is added to the domain and you have a public GPG key assigned to it which is published on a public GPG server, 
then you can deploy a new version with this command: 

```
mvn clean package
mvn deploy -Prelease-sign-artifacts
```

That will publish the new artefacto to Maven Central. Usually it takes about 10 minutes until the new package is indexed 
by Sonatype. The project page on Maven Central can be found [here](https://search.maven.org/artifact/com.liveperson.faas/functions-client). 
