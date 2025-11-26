# Security and Authentication

This guide covers security configuration and authentication for Wanaku, including API protection, UI access control, and service authentication.

## Overview

Security in Wanaku involves controlling access to the management APIs and web interface while ensuring that only authorized users can modify tools, resources, and configurations.

> [!NOTE]
> Authentication and authorization currently apply only to the management APIs and UI, not to the MCP endpoints themselves. This feature is experimental and under active development.

## Wanaku Security Model

Wanaku's security model focuses on:

- **API Protection**: Securing management operations for tools, resources, and configuration
- **UI Access Control**: Restricting access to the web console
- **Service Authentication**: Ensuring capability services can authenticate with the router
- **MCP Authentication**: Ensuring MCP calls are authenticated

## MCP Authentication

Currently, Wanaku supports:

* OAuth authentication with code grant
* Automatic client registration

> [!IMPORTANT]
> When using Automatic client registration, access is granted per-namespace. Applications need to request a new client ID and grant if they change the namespace in use.

For these to work, Keycloak needs to be configured so that authentication is properly supported.

Wanaku comes with a [template configuration](https://github.com/wanaku-ai/wanaku/blob/main/deploy/auth/wanaku-config.json) that can be imported into Keycloak to set up the realm, clients and everything else needed for Wanaku to work.

> [!IMPORTANT]
> After importing this, make sure to adjust the secrets used by the services and any other potentially sensitive configuration.

## Configuring Wanaku Components for Secure Access

Each Wanaku component requires a specific set of configurations for secure access. You can find the full set of configuration options in the [Configuration Guide](configurations.md).

The configuration varies depending on the component's role in the system.

### Router Backend Security Configuration

The backend service handles API operations and requires [OIDC configuration](https://quarkus.io/guides/security-oidc-configuration-properties-reference) with service credentials.

Some of the configurations you may need to change are:

```properties
# Address of the Keycloak authentication server - adjust to your Keycloak instance
auth.server=http://localhost:8543

# Address used by the OIDC proxy
auth.proxy=http://localhost:${quarkus.http.port}

# Client identifier configured in Keycloak for the backend service
quarkus.oidc.client-id=wanaku-mcp-router

# Avoid forcing HTTPS (for development)
quarkus.oidc.resource-metadata.force-https-scheme=false
```

#### References

As a reference for understanding what is going on under the hood, the following guides may be helpful:

* [Secure MCP OIDC Proxy](https://quarkus.io/blog/secure-mcp-oidc-proxy/)
* [Secure MCP Server OAuth 2](https://quarkus.io/blog/secure-mcp-server-oauth2/)
* [Secure MCP SSE Server](https://quarkus.io/blog/secure-mcp-sse-server/)

### Capability Services Security Configuration

Wanaku requires capability services to be authenticated in order to register themselves. Capability services act as [OIDC clients](https://quarkus.io/guides/security-openid-connect-client-reference) and authenticate with the router using client credentials.

Some of the settings you may need to adjust are:

```properties
# Address of the Keycloak authentication server - adjust to your Keycloak instance
auth.server=http://localhost:8543

# Address of the KeyCloak authentication server
quarkus.oidc-client.auth-server-url=${auth.server}/realms/wanaku

# Client secret from Keycloak for service authentication - replace with your actual secret
quarkus.oidc-client.credentials.secret=aBqsU3EzUPCHumf9sTK5sanxXkB0yFtv
```

> [!IMPORTANT]
> - Capability services use the OIDC *client* component (`quarkus.oidc-client.*`), which differs from the main router configuration
> - The client secret values shown here are examples from the default configuration - replace them with your actual Keycloak client secrets
> - Ensure the auth-server-url points to your actual Keycloak instance

## CLI Authentication

The Wanaku CLI supports authentication to securely interact with the Wanaku MCP Router API. Authentication credentials are stored locally and automatically included in API requests.

### Authentication Modes

The CLI currently supports the following authentication modes:

- **token** (default): Use an API token for authentication via Bearer token
- **username** and **password**

### Authentication Commands

#### Login

Store authentication credentials for use with subsequent CLI commands:

```shell
wanaku auth login --api-token <your-api-token>
```

**Options:**
- `--api-token <token>`: API token for authentication (required)
- `--auth-server <url>`: Authentication server URL (optional)
- `--mode <mode>`: Authentication mode - `token` or `oauth2` (default: `token`)

**Example:**

```shell
wanaku auth login --api-token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

With custom authentication server:

```shell
wanaku auth login \
  --api-token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... \
  --auth-server https://keycloak.example.com \
  --mode token
```

#### Status

Check the current authentication status and view stored credentials:

```shell
wanaku auth status
```

This command displays:
- Current authentication mode
- Masked API token (showing first and last 4 characters)
- Authentication server URL (if configured)
- Masked refresh token (if available)
- Credentials file location
- Whether credentials are currently stored

**Example output:**
```
Authentication Status:
=====================
Mode: token
API Token: eyJh***VCJ9
Auth Server: https://keycloak.example.com
Credentials File: /Users/username/.wanaku/credentials
Has Credentials: true
```

#### Logout

Clear all stored authentication credentials:

```shell
wanaku auth logout
```

This command removes all authentication data from the local credentials file.

#### Token Management

Display the raw authentication token (useful for debugging or using with other tools):

```shell
wanaku auth token
```

This outputs the raw API token without masking.

### Using Authentication with Commands

Once authenticated via `wanaku auth login`, all subsequent CLI commands will automatically include the authentication token in their requests.

#### Per-Command Token Override

You can override the stored authentication token for a single command:

```shell
wanaku tools list --token <temporary-token>
```

#### Disabling Authentication

To explicitly disable authentication for a command:

```shell
wanaku tools list --no-auth
```

### Credential Storage

Authentication credentials are stored in:
```
~/.wanaku/credentials
```

This file is a Java properties file containing:
- `api.token`: The API bearer token
- `refresh.token`: OAuth2 refresh token (when applicable)
- `auth.mode`: The authentication mode (token, oauth2, etc.)
- `auth.server.url`: The authentication server URL

> [!CAUTION]
> The credentials file contains sensitive authentication tokens. Ensure proper file permissions are set to prevent unauthorized access.
> On Unix-like systems, you should restrict access: `chmod 600 ~/.wanaku/credentials`

### Authentication Flow

The CLI authentication process works as follows:

1. **Login**: User provides API token via `wanaku auth login --api-token <token>`
2. **Storage**: Token is stored in `~/.wanaku/credentials`
3. **Auto-Injection**: The CLI automatically reads the token and adds it as a Bearer token to the `Authorization` header for all API requests
4. **Validation**: The Wanaku Router validates the token on each request
5. **Logout**: User can clear credentials via `wanaku auth logout`

### Troubleshooting Authentication

#### Token Not Working

If you receive authentication errors:

1. Check token validity:
   ```shell
   wanaku auth status
   ```

2. Verify the token hasn't expired
3. Ensure you're using the correct authentication server URL
4. Try logging in again with a fresh token

#### Clear and Reset

To completely reset authentication:

```shell
wanaku auth logout
wanaku auth login --api-token <new-token>
```

#### Manual Credential Management

You can manually edit or remove the credentials file if needed:

```shell
# View credentials
cat ~/.wanaku/credentials

# Remove credentials manually
rm ~/.wanaku/credentials
```

### Security Best Practices

1. **Token Protection**: Never share your API tokens or commit them to version control
2. **Regular Rotation**: Rotate tokens periodically for enhanced security
3. **Use Environment Variables**: For CI/CD, consider using `--token` flag with environment variables instead of storing tokens
4. **File Permissions**: Ensure credentials file has restricted permissions (600)
5. **Logout When Done**: Use `wanaku auth logout` when finished working on shared systems

### Example Workflows

#### Basic Authentication Workflow

```shell
# Login with API token
wanaku auth login --api-token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Verify authentication
wanaku auth status

# Use authenticated commands
wanaku tools list
wanaku resources list
wanaku data-store list

# Logout when done
wanaku auth logout
```

#### CI/CD Usage

For automated scripts, use token override instead of storing credentials:

```shell
# Use token from environment variable
wanaku tools list --token $WANAKU_API_TOKEN

# Or disable authentication for public endpoints
wanaku tools list --no-auth
```

## Production Security Hardening

For production deployments, implement these additional security measures:

### 1. TLS/SSL Configuration

Enable HTTPS for all external endpoints:

```properties
quarkus.http.ssl.certificate.files=/path/to/certificate.pem
quarkus.http.ssl.certificate.key-files=/path/to/private-key.pem
quarkus.http.insecure-requests=disabled
```

### 2. Token Expiration

Configure appropriate token lifetimes in Keycloak:
- Access token: 5-15 minutes
- Refresh token: 8 hours
- Session timeout: Based on your security requirements

### 3. Network Security

- Use network policies to restrict traffic between components
- Deploy services in private networks
- Use service mesh for encrypted service-to-service communication
- Enable firewall rules to limit exposure

### 4. Secret Management

- Use Kubernetes Secrets or external secret managers (Vault, AWS Secrets Manager)
- Never hardcode secrets in configuration files
- Rotate secrets regularly
- Use sealed secrets for GitOps workflows

### 5. Audit Logging

Enable access logging to track authentication and authorization events:

```properties
quarkus.http.access-log.enabled=true
quarkus.http.access-log.pattern=combined
```

### 6. Role-Based Access Control (Future)

Fine-grained RBAC is planned for future versions. Currently, all authenticated users have admin access.

## Protocol Support

Wanaku supports MCP via SSE (deprecated) or via Streamable HTTP.

The MCP endpoint exposed by Wanaku can be accessed on the path `/mcp/sse` of the host you are using (for instance, if running locally, that would mean `http://localhost:8080/mcp/sse`).

The Streamable HTTP endpoint can be accessed on the path `/mcp/`.

> [!IMPORTANT]
> Make sure to check the details about namespaces, as Wanaku offers different namespaces where MCP Tools and MCP Resources can be registered. This is documented in the [Advanced Usage Guide](advanced-usage.md).

## Related Documentation

- [Installation Guide](installation.md) - Setting up Keycloak and authentication
- [Configuration Reference](configurations.md) - Complete configuration options
- [Advanced Usage](advanced-usage.md) - Namespaces and access control
- [Troubleshooting](troubleshooting.md) - Solving authentication issues
- [Security Policy](../SECURITY.md) - Reporting vulnerabilities
