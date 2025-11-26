# Extending Wanaku

This guide covers how to extend Wanaku by creating custom capabilities, including resource providers, tool services, and MCP servers.

## Overview

Wanaku leverages [Quarkus](https://quarkus.io/) and [Apache Camel](https://camel.apache.org) to provide connectivity to a vast range of services and platforms.

Although we aim to provide a few of them out-of-the box, not all of them will fit all the use cases. For most cases, users should rely on the [Camel Integration Capability for Wanaku](https://wanaku.ai/docs/camel-integration-capability/). That capability service leverages Apache Camel which offers more than 300 components capable of talking to any type of system. Users can design their integrations using tools such as [Kaoto](https://kaoto.io/) or Karavan and expose the routes as tools or resources using that capability service.

## Adding a New Resource Provider Capability

For cases where the [Camel Integration Capability for Wanaku](https://wanaku.ai/docs/camel-integration-capability/) is not sufficient, users can create their own capability services.

We try to make it simple for users to create custom services that solve their particular need.

### Creating a New Resource Provider

To create a custom resource provider, you can run:

```shell
wanaku capabilities create resource --name y4
```

To run the newly created service enter the directory that was created (i.e., `cd wanaku-provider-y4`), then build the project using Maven (`mvn clean package`).

> [!NOTE]
> Capabilities services are created, by default, using [Apache Camel](http://camel.apache.org). However, it is possible to create purely Quarkus-based capabilities using the option `--type=quarkus`.

Then, launch it using:

```shell
java -Dwanaku.service.registration.uri=http://localhost:8080 -Dquarkus.grpc.server.port=9901 ... -jar target/quarkus-app/quarkus-run.jar
```

You can check if the service was registered correctly using `wanaku capabilities list`.

> [!IMPORTANT]
> Remember to set the parameters in the `application.properties` file and also adjust the authentication settings.

### Adjusting Your Resource Capability

After created, then most of the work is to adjust the auto-generated `Delegate` class to provide the Camel-based URI and, if necessary, coerce (convert) the response from its specific type to String.

## Adding a New Tool Invoker Capability

### Creating a New Tool Service

To create a custom tool service, you can run:

```shell
wanaku capabilities create tool --name jms
```

> [!NOTE]
> Capabilities services are created, by default, using [Apache Camel](http://camel.apache.org). However, it is possible to create purely Quarkus-based capabilities using the option `--type=quarkus`.

To run the newly created service enter the directory that was created (i.e., `cd wanaku-tool-service-jms`), then build the project using Maven (`mvn clean package`).

Then, launch it using:

```shell
java -Dwanaku.service.registration.uri=http://localhost:8080 -Dquarkus.grpc.server.port=9900 ... -jar target/quarkus-app/quarkus-run.jar
```
You can check if the service was registered correctly using `wanaku capabilities list`.

> [!IMPORTANT]
> Remember to set the parameters in the `application.properties` file and also adjust the authentication settings.

To customize your service, adjust the delegate and client classes.

### Adjusting Your Tool Invoker Capability

After created, then most of the work is to adjust the auto-generated `Delegate` and `Client` classes to invoke the service and provide the returned response.

In those cases, then you also need to write a class that leverages [Apache Camel's](http://camel.apache.org) `ProducerTemplate` and (or, sometimes, both) `ConsumerTemplate` to interact with the system you are implementing connectivity too.

## Adding a New MCP Server Capability

### Creating a New MCP Server

To create a custom mcp server, you can run:

```shell
wanaku capabilities create mcp --name s3
```

To run the newly created service enter the directory that was created (i.e., `cd wanaku-mcp-servers-s3`), then build the project using Maven (`mvn clean package`).

> [!NOTE]
> Capabilities services are created, by default, using [Apache Camel](http://camel.apache.org). However, it is possible to create purely Quarkus-based capabilities using the option `--type=quarkus`.

Then, launch it using:

```shell
java -Dwanaku.service.registration.uri=http://localhost:8080 -Dquarkus.grpc.server.port=9901 ... -jar target/quarkus-app/quarkus-run.jar
```

You can check if the service was registered correctly using `wanaku forwards list`.

> [!IMPORTANT]
> Remember to set the parameters in the `application.properties` file.

### Adjusting Your MCP Server Capability

After created, then most of the work is to adjust the auto-generated `Tool` class to implement the mcp server tool.

## Creating MCP Servers Using Maven

Alternatively, you can create MCP servers directly using Maven archetypes:

```shell
mvn -B archetype:generate -DarchetypeGroupId=ai.wanaku -DarchetypeArtifactId=wanaku-mcp-servers-archetype \
  -DarchetypeVersion=0.0.8 -DgroupId=ai.wanaku -Dpackage=ai.wanaku.mcp.servers.s3 -DartifactId=wanaku-mcp-servers-s3 \
  -Dname=S3 -Dwanaku-version=0.0.8 -Dwanaku-capability-type=camel
```

> [!IMPORTANT]
> When using the maven way, please make sure to adjust the version of Wanaku to be used by correctly setting the `wanaku-version` property to the base Wanaku version to use.

### Adjusting the Maven-Generated MCP Server

After creating the mcp server, open the `pom.xml` file to add the dependencies for your project. Using the example above, we would include the following dependencies:

```xml
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-aws-s3</artifactId>
    </dependency>
```

Adjust the gRPC port in the `application.properties` file by adjusting the `quarkus.grpc.server.port` property.

> [!NOTE]
> You can also provide the port when launching (i.e., `java -Dquarkus.grpc.server.port=9190 -jar target/quarkus-app/quarkus-run.jar`)

Then, build the project:

```shell
mvn clean package
```

And run it:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

## Implementing Services in Other Languages

The communication between Wanaku MCP Router and its downstream services is capable of talking to any type of service using gRPC. Therefore, it's possible to implement services in any language that supports it.

For those cases, leverage the `.proto` files in the `core-exchange` module for creating your own service.

> [!CAUTION]
> At this time, Wanaku is being intensively developed, therefore, we cannot guarantee backwards compatibility of the protocol.

> [!NOTE]
> For plain Java, you can still generate the project using the archetype, but in this case, you must implement your own delegate from scratch and adjust the dependencies.

## Configuration

### Adjusting the Announcement Address

You can adjust the address used to announce to the MCP Router using either (depending on whether using a tool or a resource provider):

* `wanaku.service.registration.announce-address=my-host`

This is particularly helpful when running a capability service in the cloud, behind a proxy or firewall.

### Adjusting the Authentication Parameters

When configuring your custom capability, you'll need to set the following authentication parameters:

* `quarkus.oidc-client.auth-server-url=http://localhost:8543/realms/wanaku`
* `quarkus.oidc-client.client-id=wanaku-service`
* `quarkus.oidc-client.refresh-token-time-skew=1m`
* `quarkus.oidc-client.credentials.secret=<insert key here>`

Make sure to replace `<insert key here>` with the actual client secret from your Keycloak configuration.

## Available Built-in Capabilities

### Resource Providers

Visit the [Resource Providers documentation](../capabilities/providers/README.md) to check all the providers that come built-in with Wanaku.

> [!NOTE]
> Most users should rely on the [Camel Integration Capability for Wanaku](https://wanaku.ai/docs/camel-integration-capability/).

### Tool Services

Visit the [Tool Services documentation](../capabilities/tools/README.md) to check all the tools that come built-in with Wanaku.

> [!NOTE]
> Most users should rely on the [Camel Integration Capability for Wanaku](https://wanaku.ai/docs/camel-integration-capability/).

## Related Documentation

- [Installation Guide](installation.md) - Installing and running capability services
- [Security Guide](security.md) - Configuring capability authentication
- [Advanced Usage](advanced-usage.md) - Managing capabilities
- [Configuration Reference](configurations.md) - Complete configuration options
- [Architecture](architecture.md) - Understanding Wanaku's design
- [Contributing Guide](../CONTRIBUTING.md) - Contributing to Wanaku
