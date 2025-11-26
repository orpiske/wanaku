# Managing Resources and Prompts

This guide covers how to work with MCP resources, prompts, and shared data in Wanaku. Resources allow AI agents to consume data, while prompts provide reusable templates for LLM interactions.

## Overview

In Wanaku, resources and prompts serve different but complementary purposes:

- **Resources**: Any data that can be read by using the MCP protocol (files, database records, static content)
- **Prompts**: Reusable templates that can leverage multiple tools and provide example interactions for LLMs
- **Shared Data**: Static data shared between Wanaku and its capabilities (configuration files, route definitions)

## Managing MCP Resources

MCP resources allow an AI agent to consume data—such as files or records—and inject additional information into its context. Just like tools, resources require a capability that can access the system storing and providing access to the resource (i.e.: FTP, AWS S3, NFS, local filesystem, etc.).

### Exposing Resources

The `wanaku resources expose` command allows you to make an existing resource available via your Wanaku MCP Router instance.

For example, suppose you have a file named `test-mcp-2.txt` on your home directory on a host that has the `file` capability running, and you want to expose it.

This is how you can do it:

```shell
wanaku resources expose --location=$HOME/test-mcp-2.txt --mimeType=text/plain --description="Sample resource added via CLI" --name="test mcp via CLI" --type=file
```

In this example:

* `--location=$HOME/test-mcp-2.txt`: Specifies the local path to the resource you want to expose
* `--mimeType=text/plain`: Defines the MIME type of the resource, indicating its content format
* `--description="Sample resource added via CLI"`: Provides a descriptive text for the resource
* `--name="test mcp via CLI"`: Assigns a human-readable name to the exposed resource
* `--type=file`: Indicates that the exposed resource is a file

> [!IMPORTANT]
> It's important to note that this location refers to a location that the capability (downstream service) is able to access. The exact meaning of "location" depends on the type of the capability. For example:
> * For a `file` type, it means the capability needs direct access to the file, implying it's likely running on a host with direct physical access to the file.
> * For an `ftp` type, it means the capability needs access to the FTP server storing the file.
>
> Always check the documentation for the capability provider that you are using for additional details about the location specifier.

### Organizing Resources with Labels

Just like tools, you can organize resources using labels:

```shell
wanaku resources expose --location=$HOME/documents/report.pdf --mimeType=application/pdf --description="Q4 Financial Report" --name="q4-report" --type=file --label category=finance --label year=2024 --label department=accounting
```

Labels help you:
- Organize resources by category, department, or project
- Track resource lifecycles and versions
- Filter and manage resources more effectively
- Implement batch operations on groups of resources

### Managing Labels on Existing Resources

After creating resources, you can add or remove labels without modifying the resource definition:

**Adding labels to an existing resource:**
```shell
# Add labels to a specific resource
wanaku resources label add --name "q4-report" --label archived=true --label reviewed=yes

# Add labels to multiple resources using label expressions (-e is short for --label-expression)
wanaku resources label add -e 'category=finance' --label migrated=true
```

**Removing labels from an existing resource:**
```shell
# Remove labels from a specific resource
wanaku resources label remove --name "q4-report" --label temporary --label draft

# Remove labels from multiple resources using label expressions (-e is short for --label-expression)
wanaku resources label remove -e 'status=archived' --label legacy
```

**Note:** When adding a label with a key that already exists, the value will be updated. When removing a non-existent label, it will be silently ignored.

### Listing Resources

The `wanaku resources list` command allows you to view all resources currently exposed by your Wanaku MCP Router instance.

Executing this command will display a list of available resources, including their names and descriptions.

```shell
wanaku resources list
```

### Removing Resources

Resources can be removed from the UI or via the CLI using the `wanaku resources remove` command.

#### Removing a Single Resource by Name

To remove a specific resource by name:

```shell
wanaku resources remove --name "q4-report"
```

#### Batch Removal Using Label Expressions

Similar to tools, you can remove multiple resources at once using label expressions:

```shell
wanaku resources remove -e 'year=2023'
```

This command will:
1. Find all resources with the label `year=2023`
2. Display a preview table showing which resources will be removed
3. Prompt for confirmation before removal
4. Report the number of resources removed

**Examples:**

Remove all draft documents:
```shell
wanaku resources remove -e 'status=draft'
```

Remove archived resources from a specific department:
```shell
wanaku resources remove -e 'department=sales & status=archived'
```

Remove resources that are not marked as important:
```shell
wanaku resources remove -e '!priority=high'
```

For automated scripts, skip the confirmation prompt:
```shell
wanaku resources remove -e 'year=2022' -y
```

> [!WARNING]
> Resource removal operations cannot be undone. Always review the preview table before confirming removal.

## Managing MCP Prompts

Prompts are reusable templates that can leverage multiple tools and provide example interactions for LLMs. They are part of the MCP (Model Context Protocol) specification and enable:

* Creating standardized message templates with variable substitution
* Defining argument schemas for dynamic prompt generation
* Referencing tools that the prompt can utilize
* Supporting multiple content types (text, images, audio, embedded resources)
* Providing example interactions to guide LLM behavior

### Adding Prompts Using CLI

The `wanaku prompts add` command allows you to create new prompts in your Wanaku MCP Router instance.

#### Basic Example

```shell
wanaku prompts add \
  --name "code-review" \
  --description "Review code for quality and security issues" \
  --message "user:text:Please review the following code: {{code}}" \
  --message "assistant:text:I'll analyze this code for potential issues." \
  --argument "code:The code to review:true"
```

In this example:
* `--name "code-review"`: Assigns a unique identifier for the prompt
* `--description`: Provides a human-readable description
* `--message`: Defines messages in the prompt (can be specified multiple times)
* `--argument`: Defines template arguments (format: `name:description:required`)

#### Message Format

The `--message` option supports multiple content types:

**Text Messages** (default):
```shell
--message "user:text:Your message here"
--message "user:Your message here"  # Backward compatible shorthand
```

**Image Messages**:
```shell
--message "user:image:iVBORw0KGgoAAAANSUhEUgAAAAUA...:image/png"
```

**Audio Messages**:
```shell
--message "user:audio:UklGRiQAAABXQVZFZm10IBAAAA...:audio/wav"
```

**Embedded Resource Messages**:
```shell
--message "user:resource:file:///path/to/file.txt:File content:text/plain"
```

#### Template Variable Substitution

Prompts support Mustache-style variable substitution using `{{variable}}` syntax:

```shell
wanaku prompts add \
  --name "translate" \
  --description "Translate text between languages" \
  --message "user:text:Translate the following text from {{source_lang}} to {{target_lang}}: {{text}}" \
  --argument "source_lang:Source language:true" \
  --argument "target_lang:Target language:true" \
  --argument "text:Text to translate:true"
```

#### Tool References

Prompts can reference specific tools they may utilize:

```shell
wanaku prompts add \
  --name "api-test" \
  --description "Test API endpoints" \
  --message "user:text:Test the API endpoint {{endpoint}}" \
  --tool-reference "http-get" \
  --tool-reference "http-post" \
  --argument "endpoint:API endpoint URL:true"
```

#### Namespace Support

Prompts can be organized into namespaces for isolation:

```shell
wanaku prompts add \
  --name "review" \
  --description "Code review prompt" \
  --namespace "ns-0" \
  --message "user:text:Review this code: {{code}}" \
  --argument "code:Code to review:true"
```

Supported namespaces: `ns-0` through `ns-9`, `default`, and `public`.

### Adding Prompts Using the UI

You can also manage prompts through the Wanaku Web UI:

1. Navigate to the Prompts page in the Web UI
2. Click "Add Prompt"
3. Fill in the form:
   - **Name**: Unique identifier for the prompt
   - **Description**: Human-readable description
   - **Messages (JSON)**: Array of message objects
   - **Arguments (JSON)**: Array of argument objects (optional)
   - **Tool References (JSON)**: Array of tool names (optional)
   - **Namespace**: Namespace for isolation (optional)

Example message JSON formats:

**Text Message**:
```json
{
  "role": "user",
  "content": {
    "type": "text",
    "text": "Review {{code}}"
  }
}
```

**Image Message**:
```json
{
  "role": "user",
  "content": {
    "type": "image",
    "data": "iVBORw0KGgoAAAANSUhEUgAAAAUA...",
    "mimeType": "image/png"
  }
}
```

**Audio Message**:
```json
{
  "role": "user",
  "content": {
    "type": "audio",
    "data": "UklGRiQAAABXQVZFZm10IBAAAA...",
    "mimeType": "audio/wav"
  }
}
```

**Embedded Resource Message**:
```json
{
  "role": "user",
  "content": {
    "type": "resource",
    "resource": {
      "location": "file:///path/to/file.txt",
      "description": "File content",
      "mimeType": "text/plain"
    }
  }
}
```

### Listing Prompts

View all prompts currently available in your Wanaku MCP Router instance:

```shell
wanaku prompts list
```

This displays all prompts with their names, descriptions, and namespaces.

### Editing Prompts

You can edit an existing prompt using the CLI.

The `wanaku prompts edit` command allows you to modify an existing prompt. Only the fields you specify will be updated:

```shell
wanaku prompts edit \
  --name "code-review" \
  --description "Updated description for code review" \
  --message "user:text:Please review this code: {{code}}"
```

All options except `--name` are optional:
- If `--description` is provided, it replaces the existing description
- If `--message` is provided, it replaces **all** existing messages
- If `--argument` is provided, it replaces **all** existing arguments
- If `--tool-references` is provided, it replaces **all** existing tool references
- If `--namespace` is provided, it replaces the existing namespace

Example of updating only the description:
```shell
wanaku prompts edit --name "code-review" --description "New description"
```

Example of updating messages:
```shell
wanaku prompts edit \
  --name "code-review" \
  --message "user:text:Review the following code for security issues: {{code}}" \
  --message "assistant:text:I'll perform a security audit."
```

### Removing Prompts

Remove a prompt by name:

```shell
wanaku prompts remove --name "code-review"
```

## Managing Shared Data

Wanaku provides a data store feature that allows you to share static data between Wanaku and its capabilities.

This is particularly useful for storing configuration files, route definitions, and other static resources that capabilities need to access at runtime.

A primary use case for the data store is storing Apache Camel routes and associated files for the Camel Integration Capability. By storing route definitions in the data store, you can dynamically configure integrations without rebuilding or redeploying capabilities.

> [!IMPORTANT]
> Authentication is required to access the data store API. Make sure you're logged in using `wanaku auth login` before using data store commands.

### Adding Data to the Data Store

The `wanaku data-store add` command allows you to upload files to the data store. Files are automatically Base64 encoded when stored.

```shell
wanaku data-store add --read-from-file /path/to/file.yaml
```

By default, the data store entry will be named after the filename. You can specify a custom name using the `--name` option:

```shell
wanaku data-store add --read-from-file /path/to/employee-routes.camel.yaml --name employee-routes
```

In this example:
* `--read-from-file`: Specifies the local file path to upload
* `--name`: (Optional) Assigns a custom name to the stored data

The file contents are automatically Base64 encoded before being sent to the server, ensuring binary-safe storage.

### Listing Stored Data

View all data currently stored in the data store:

```shell
wanaku data-store list
```

This displays a table showing:
- **ID**: Unique identifier for each stored item
- **Name**: The name of the stored data
- **Data**: A preview of the stored content (truncated to 50 characters)
- **Labels**: Labels associated with the data store entry

You can filter the list using label expressions:

```shell
# Filter by label expression
wanaku data-store list -e 'category=routes'
```

See the label expression guide (`wanaku man label-expression`) for detailed syntax information.

### Managing Labels on Data Stores

Data stores support labels for organization and filtering, similar to tools and resources.

**Adding labels to a data store:**

```shell
# Add labels to a specific data store by ID
wanaku data-store label add --id <data-store-id> --label category=routes --label env=production

# Add labels to multiple data stores using label expressions
wanaku data-store label add -e 'category=config' --label migrated=true
```

**Removing labels from a data store:**

```shell
# Remove labels from a specific data store by ID
wanaku data-store label remove --id <data-store-id> --label temporary --label draft

# Remove labels from multiple data stores using label expressions
wanaku data-store label remove -e 'status=deprecated' --label legacy
```

When adding a label with a key that already exists, the value will be updated. When removing a non-existent label, it will be silently ignored.

### Removing Data from the Data Store

Remove stored data using either the ID or name:

```shell
# Remove by ID
wanaku data-store remove --id <data-store-id>

# Remove by name
wanaku data-store remove --name employee-routes
```

> [!NOTE]
> The data store is also accessible via the REST API at `/api/v1/data-store` and through the Wanaku web interface under the Data Stores page, where you can upload, download, and manage stored data using a graphical interface.

## Related Documentation

- [Managing Tools](managing-tools.md) - Working with MCP tools
- [Advanced Usage](advanced-usage.md) - Namespaces and access control
- [Extending Wanaku](extending-wanaku.md) - Creating custom resource providers
- [Configuration Reference](configurations.md) - Configuration options for capabilities
- [Troubleshooting](troubleshooting.md) - Solving resource-related issues
