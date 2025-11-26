# Advanced Usage

This guide covers advanced Wanaku features including namespaces, MCP forwards, URI construction, and capability management.

## Overview

As you become more familiar with Wanaku, you'll want to leverage advanced features to:

- Organize tools and resources using namespaces
- Aggregate multiple MCP servers through forwards
- Build dynamic URIs with parameters and request bodies
- Manage and configure capability services

## Managing Namespaces

Wanaku introduces the concept of namespaces to help users organize and isolate tools and resources, effectively managing the Large Language Model (LLM) context. This prevents context bloat and improves the efficiency of your Wanaku deployments.

### What Are Namespaces

Namespaces provide a mechanism to group related tools and resources.

Each namespace acts as a separate logical container, ensuring that the LLM context for tools within one namespace does not interfere with tools in another. This is particularly useful when you have a large number of tools or when different sets of tools are used for distinct purposes.

Wanaku provides a fixed set of 10 available slots for namespaces, named from `ns-0` to `ns-9`. It also provides a `default` namespace, which is used if none is specified and a special `public` namespace that can be accessed without any authentication.

### Using Namespaces

To associate a tool or resource with a specific namespace, use the `--namespace` option when adding it:

```shell
wanaku tools add -n "meow-facts-3" --description "Retrieve random facts about cats" --uri "https://meowfacts.herokuapp.com?count={count or 1}" --type http --property "count:int,The count of facts to retrieve" --namespace test --required count
```

In the example above, the `meow-facts-3` tool will be associated with the first freely available namespace.

When you provide a namespace name like `test`, Wanaku automatically associates it with an available numerical slot from ns-0 to ns-9.

### Checking Namespace Assignments

You can verify which namespace a tool or resource has been assigned to by using the `wanaku namespaces list` command.

This command will display a list of all active namespaces, their unique IDs, and their corresponding paths.

The output will look similar to this:

```shell
id                                   name   path
28560e66-d94c-44a2-b032-779b5542132a        http://localhost:8080/ns-4/mcp/sse
43b5d7a7-4e7d-4109-960b-ac7695b6f2d3 public http://localhost:8080/public/mcp/sse
93c5bfdf-0e09-4da5-82fa-4eec3bf6b1b4        http://localhost:8080/ns-3/mcp/sse
bfd112d2-32cb-475a-9f55-63301519152b        http://localhost:8080/ns-7/mcp/sse
f5915650-4daa-4616-95c6-5aafceffb026        http://localhost:8080/ns-1/mcp/sse
db89fedd-ffe6-4dee-b051-bcd5285bb9c9        http://localhost:8080/ns-2/mcp/sse
d4249e11-9368-4c5b-bb66-981d2d2e69c7        http://localhost:8080/ns-0/mcp/sse
8898fab6-3774-427f-8400-8c6f6fd9a97e        http://localhost:8080/ns-6/mcp/sse
fe8cc1f2-2355-4009-ba68-4faeefe937f7        http://localhost:8080/ns-5/mcp/sse
a3dfaaf6-3655-4bcc-8c48-3d183b6d675b        http://localhost:8080/ns-8/mcp/sse
8832e2c7-3bd9-4f9b-88ba-982cc20a43de        http://localhost:8080/ns-9/mcp/sse
<default>                                   http://localhost:8080//mcp/sse
```

In this output, you can see the mapping of internal namespace IDs to their corresponding ns-X paths.

> [!IMPORTANT]
> For Streamable HTTP, remove the `/sse` from the path (i.e.: `http://localhost:8080/ns-1/mcp/`).

### The Default Namespace

If you do not specify a namespace when adding a tool or resource, it will automatically be added to the default namespace.

The default namespace acts as a general container for tools that don't require specific isolation.

You can identify the default namespace in the wanaku namespaces list output by its `<default>` name.

### Managing Labels on Namespaces

Labels provide a flexible way to organize and filter namespaces. You can add metadata to namespaces in the form of key-value pairs, making it easier to manage and query them.

#### Adding Labels to Namespaces

You can add labels to an existing namespace using the `wanaku namespaces label add` command.

To specify which namespace to add labels to, you need the namespace ID from the `wanaku namespaces list` output (the first column):

```shell
# Add a single label to a namespace
wanaku namespaces label add --id 28560e66-d94c-44a2-b032-779b5542132a --label env=production

# Add multiple labels at once
wanaku namespaces label add --id 28560e66-d94c-44a2-b032-779b5542132a -l env=production -l tier=backend -l version=2.0
```

If a label key already exists, its value will be updated to the new value.

#### Adding Labels to Multiple Namespaces

You can add labels to multiple namespaces at once using label expressions:

```shell
# Add a label to all namespaces matching a label expression
wanaku namespaces label add --label-expression 'category=internal' --label migrated=true

# Add multiple labels to namespaces matching complex expressions
wanaku namespaces label add -e 'env=staging & tier=backend' -l reviewed=true -l compliant=yes
```

#### Removing Labels from Namespaces

To remove labels from a namespace, use the `wanaku namespaces label remove` command:

```shell
# Remove a single label from a namespace
wanaku namespaces label remove --id 28560e66-d94c-44a2-b032-779b5542132a --label env

# Remove multiple labels at once
wanaku namespaces label remove --id 28560e66-d94c-44a2-b032-779b5542132a -l env -l tier -l version
```

#### Removing Labels from Multiple Namespaces

Similar to adding labels, you can remove labels from multiple namespaces using label expressions:

```shell
# Remove labels from all namespaces matching an expression
wanaku namespaces label remove --label-expression 'category=temp' --label temp

# Remove multiple labels from matching namespaces
wanaku namespaces label remove -e 'migrated=true' -l temp -l draft
```

#### Listing Namespaces with Label Filters

You can filter namespaces by their labels when listing them:

```shell
# List all namespaces with a specific label
wanaku namespaces list --label-filter 'env=production'

# List namespaces matching complex expressions
wanaku namespaces list --label-filter 'env=production & tier=backend'
```

## Accessing Other MCP Servers (MCP Forwards)

The MCP bridge in Wanaku allows it to act as a central gateway or proxy to other MCP servers that use HTTP as the transport mechanism.

This feature enables a centralized endpoint for aggregating tools and resources provided by other MCP servers.

### Listing Forwards

To view a list of currently configured forwards, use the `wanaku forwards list` command:

```bash
wanaku forwards list
```

This command displays information about each forward, including its name, service URL, and any other relevant details.

This can be useful for managing and troubleshooting MCP server integrations.

### Adding Forwards

To add an external MCP server to the Wanaku instance, use the `wanaku forwards add` command:

```bash
wanaku forwards add --service="http://your-mcp-server.com:8080/mcp/sse" --name my-mcp-server
```

* `--service`: The URL of the external MCP server's SSE (Server-Sent Events) endpoint
* `--name`: A unique human-readable name for the forward, used for identification and management purposes

Once a forward is added, all tools and resources provided by the external MCP server will be mapped in the Wanaku instance.

These tools and resources can then be accessed as if they were local to the server.

### Removing Forwards

To remove a specific external MCP server from the Wanaku instance, use the `wanaku forwards remove` command:

```bash
wanaku forwards remove --name my-mcp-server
```

* `--name`: The human-readable name for the forward to be removed

> [!WARNING]
> Forward removal operations cannot be undone. Once removed, the tools and resources from those MCP servers will no longer be accessible.

> [!NOTE]
> Attempting to remove a non-existent forward will result in an error message.

### Example Use Case

Suppose you have two MCP servers: `http://mcp-server1.com:8080/mcp/sse` and `http://mcp-server2.com:8080/mcp/sse`.

To integrate these external MCP servers into your Wanaku instance, follow these steps:

1.  Add the first forward using the `wanaku forwards add` command:

```shell
wanaku forwards add --service="http://mcp-server1.com:8080/mcp/sse" --name mcp-server-1
```

2.  Use the `wanaku forwards list` command to confirm that the forward has been successfully added:

```bash
wanaku forwards list
```

3. Verify that all tools and resources from `mcp-server1` are now accessible within your Wanaku instance using `wanaku tools list`

```shell
Name               Type               URI
tavily-search-local => tavily          => tavily://search?maxResults={parameter.value('maxResults')}
meow-facts      => mcp-remote-tool => <remote>
dog-facts       => mcp-remote-tool => <remote>
camel-rider-quote-generator => mcp-remote-tool => <remote>
tavily-search   => mcp-remote-tool => <remote>
laptop-order    => mcp-remote-tool => <remote>
```

4.  Add the second forward using the same command:
```bash
wanaku forwards add --service="http://mcp-server2.com:8080/mcp/sse" --name mcp-server-2
```

5. Confirm that tools and resources from both external MCP servers are now integrated into your Wanaku instance (use `wanaku tools list`)
6. Use the `wanaku forwards list` command to view the updated list of forwards:
```bash
wanaku forwards list
```

By leveraging the MCP bridge feature, you can create a centralized endpoint for aggregating tools and resources from multiple external MCP servers, simplifying management and increasing the overall functionality of your Wanaku instance.

## Understanding URIs

Universal Resource Identifiers (URI) are central to Wanaku.

They are used to define the location of resources, the tool invocation request that Wanaku will receive from the Agent/LLM and the location of configuration and secret properties.

Understanding URIs is critical to leverage Wanaku and create flexible definitions of tools and resources.

### Flexible Input Data

Some services may require a more flexible definition of input data.

For instance, consider HTTP endpoints with dynamic parameters:

* `http://my-host/api/{someId}`
* `http://my-host/api/{someId}/create`
* `http://my-host/api/{someId}/link/to/{anotherId}`

In cases where the service cannot predetermine the actual tool addresses, users must define them when creating the tool.

### Creating URIs

Building the URIs is not always as simple as defining their address. Sometimes, optional parameters need to be filtered out or query parameters need to be built. To help with that, Wanaku comes with a couple of expressions to build them.

To access the values, you can use the expression `{parameter.value('name')}`. For instance, to get the value of the parameter `id` you would use the expression `{parameter.value('id')}`. You can also provide default values if none are provided, such as `http://my-host/{parameter.valueOrElse('id', 1)}/data` (this would provide the value `1` if the parameter `id` is not set).

It is also possible to build the query part of URIs with the `query` method. For instance, to create a URI such as `http://my-host/data?id=456` you could use `http://my-host/data{parameter.query('id')}`. If the `id` parameter is not provided, this would generate a URI such as `http://my-host/data`. This can take multiple parameters, so it is possible to pass extra variables such as `{parameter.query('id', 'name', 'location', ...)}`.

> [!IMPORTANT]
> Do not provide the `?` character. It is added automatically by the parsing code if necessary.

Building the query part of URIs can be quite complex if there are too many. To avoid that, you can use `{parameter.query}` to build a query composed of all query parameters.

The values for the queries will be automatically encoded, so a URI defined as `http://my-host/{parameter.query('id', 'name')}` would generate `http://my-host/?id=456&name=My+Name+With+Spaces` if provided with a name value of `"My Name With Spaces"`.

### Dealing with Request Bodies

The `wanaku_body` property is a special argument used to indicate that the associated property or argument should be included in the body of the data exchange, rather than as a parameter.

For instance, in an HTTP call, `wanaku_body` specifies that the property should be part of the HTTP body, not the HTTP URI.

The handling of such parameters may vary depending on the service being used.

Currently special arguments:

* `wanaku_body`

## Managing Capabilities

Configurations in Wanaku have two distinct scopes:

1. Capability service configurations
2. Tool definition configurations

### Capability Service Configurations

These configurations are essential for setting up the capability provider itself.

This includes details required for the transport mechanism used to access the capability, such as usernames and passwords for authenticating with the underlying system that provides the capability.

Each capability service may have its own specific set of configurations. As such, check the capability service documentation for details.

### Tool Definition Configurations

These configurations are specific to individual tools that leverage a particular capability. They include:

* Names and identifiers that differentiate tools using the same capability, like specific Kafka topics or the names of database tables
* Operational properties that dictate how the tool behaves, such as the type of HTTP method (`GET`, `POST`, `PUT`), or operational settings like timeout configurations and idempotence flags

These configurations are handled when adding a new tool to Wanaku MCP Router.

> [!NOTE]
> For more details about configuring capabilities for tools, see the [Managing Tools Guide](managing-tools.md#configuring-tool-capabilities).

### Listing Capabilities

The `wanaku capabilities list` command provides a comprehensive view of all service capabilities available in the Wanaku Router. It discovers and displays both management tools and resource providers, along with their current operational status and activity information.

The command combines data from multiple API endpoints to present a unified view of the system's capabilities in an easy-to-read table format.

The command displays the results in a table with the following columns:

| Column | Description |
|--------|-------------|
| **service** | Name of the service |
| **serviceType** | Type/category of the service |
| **host** | Hostname or IP address where the service runs |
| **port** | Port number the service listens on |
| **status** | Current operational status (`active`, `inactive`, or `-`) |
| **lastSeen** | Formatted timestamp of last activity |

For instance, running the command, should present you with an output similar to this:

#### Sample Output
![Terminal output showing the result of running 'wanaku capabilities list' command displaying registered capability services](imgs/cli-capabilities-list.png)

### Displaying Service Capability Details

The `wanaku capabilities show` command lets you view detailed information for a specific service capability within the Wanaku MCP Router.

This includes its configuration parameters, current status, and connection information.

```bash
wanaku capabilities show <service> [--host <url>]
```

* `<service>`: The service name to show details for (e.g., http, sqs, file)
* `--host <url>`: The API host URL (default: http://localhost:8080)

When you execute the command, Wanaku displays comprehensive details about the chosen service type. If multiple instances of the same service exist, an interactive menu will appear, allowing you to select the specific instance you wish to view.

For example, to show the details for the HTTP service:

```shell
wanaku capabilities show http
```

Or, show details for SQS service linked with to a specific Wanaku MCP router running at `http://api.example.com:8080`:

```shell
wanaku capabilities show sqs --host http://api.example.com:8080
```

The command displays two main sections:

1. **Capability Summary**: Basic service information in table format:
- Service name and type
- Host and port
- Current status
- Last seen timestamp

2. **Configurations**: Detailed configuration parameters:
- Parameter names
- Parameter descriptions

![Terminal output showing detailed information for a specific capability service including status, URI, and available operations](imgs/capabilities-show.png)

#### Interactive Selection

When multiple instances of the same service are found, you'll see:
- A warning message indicating multiple matches
- An interactive selection prompt with service details
- Choose your desired instance using arrow keys and Enter

![Terminal output showing an interactive prompt for selecting a capability service from a numbered list](imgs/capabilities-show-choose.png)

> [!NOTE]
> The Wanaku CLI provides clear exit codes to indicate the outcome of a command:
> - `0`: The command executed successfully.
> - `1`: An error occurred (e.g., no capabilities were found, or there were issues connecting to the API).

## Related Documentation

- [Managing Tools](managing-tools.md) - Working with MCP tools
- [Managing Resources](managing-resources.md) - Working with resources and prompts
- [Extending Wanaku](extending-wanaku.md) - Creating custom capabilities
- [Configuration Reference](configurations.md) - Configuration options
- [Architecture](architecture.md) - System design and components
- [Troubleshooting](troubleshooting.md) - Solving common issues
