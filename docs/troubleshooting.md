# Troubleshooting

This guide provides solutions to common issues you may encounter while using Wanaku.

## Authentication Issues

### Cannot Authenticate with the Router

**Symptoms:**
- CLI commands fail with authentication errors
- Web UI redirects to Keycloak but login fails

**Solutions:**

1. Verify Keycloak is running and accessible:
   ```shell
   curl http://localhost:8543/health
   ```

2. Check that the Keycloak realm is properly configured:
   - Ensure the `wanaku` realm exists
   - Verify the `wanaku-mcp-router` client is configured
   - Confirm user accounts have been created

3. Clear stored credentials and re-authenticate:
   ```shell
   rm ~/.wanaku/credentials
   wanaku auth login --url http://localhost:8080
   ```

4. Verify the router can reach Keycloak:
   - Check the `auth.server` configuration property
   - Ensure network connectivity between components

### Token Expired Errors

**Symptoms:**
- Commands work initially but fail after some time
- Error messages about expired tokens

**Solutions:**

1. Re-authenticate with the router:
   ```shell
   wanaku auth login --url http://localhost:8080
   ```

2. Check token lifetime settings in Keycloak if tokens expire too quickly

## Service Registration Issues

### Capability Services Not Appearing in the Router

**Symptoms:**
- Services start successfully but don't show up in `wanaku capabilities list`
- Tools or resources from a service are not available

**Solutions:**

1. Verify the service registration configuration:
   ```properties
   # In the capability service application.properties
   wanaku.service.registration.enabled=true
   wanaku.service.registration.uri=http://localhost:8080
   ```

2. Check service logs for registration errors:
   ```shell
   # Look for registration-related errors
   grep -i "registration" /path/to/service.log
   ```

3. Verify network connectivity between the service and router:
   ```shell
   # From the service host
   curl http://localhost:8080/q/health
   ```

4. Check if the service is using the correct OIDC credentials:
   - Verify `quarkus.oidc-client.credentials.secret` matches the secret in Keycloak
   - Ensure the `wanaku-service` client exists in Keycloak

5. Check the router backend logs for incoming registration requests

### Service Shows as "Offline" or "Unhealthy"

**Symptoms:**
- Service appears in `wanaku capabilities list` but marked as offline
- Intermittent availability

**Solutions:**

1. Verify the service is running:
   ```shell
   # Check if the gRPC port is listening
   netstat -an | grep 9009
   ```

2. Check the registration interval and ensure heartbeats are being sent:
   ```properties
   # In application.properties
   wanaku.service.registration.interval=10s
   ```

3. Review service health and ensure it's not crashing or restarting

## Connection Issues

### Cannot Connect to the Router from MCP Clients

**Symptoms:**
- MCP clients fail to connect
- Timeout errors when connecting

**Solutions:**

1. Verify the router is running and accessible:
   ```shell
   curl http://localhost:8080/q/health
   ```

2. Check the correct MCP endpoint is being used:
   - SSE transport: `http://localhost:8080/mcp/sse`
   - Streamable HTTP: `http://localhost:8080/mcp/`

3. For namespace-specific connections, ensure the correct path:
   ```shell
   # For namespace ns-1
   http://localhost:8080/ns-1/mcp/sse
   ```

4. Verify firewall rules allow traffic on port 8080

5. Check CORS settings if connecting from a web application:
   ```properties
   quarkus.http.cors.enabled=true
   quarkus.http.cors.origins=http://localhost:3000
   ```

## Tool and Resource Issues

### Tools or Resources Not Appearing in MCP Clients

**Symptoms:**
- `wanaku tools list` shows tools, but they don't appear in the MCP client
- Resources are registered but not accessible

**Solutions:**

1. Verify the tool/resource is in the correct namespace:
   ```shell
   wanaku tools list
   wanaku namespaces list
   ```

2. Check if the client is connected to the correct namespace endpoint

3. Refresh the MCP client connection

4. Verify the capability service providing the tool is online:
   ```shell
   wanaku capabilities list
   ```

### Tool Invocation Fails

**Symptoms:**
- Tool appears in client but execution fails
- Error messages when calling a tool

**Solutions:**

1. Check the tool URI is correct:
   ```shell
   wanaku tools list
   ```

2. Verify the capability service is running and healthy

3. Review capability service logs for errors during tool execution

4. Ensure required configuration or secrets are properly set:
   ```shell
   wanaku tools list
   ```

5. For HTTP tools, verify the target endpoint is accessible from the service

### Resource Read Fails

**Symptoms:**
- Resource appears but cannot be read
- Empty or error responses when accessing resources

**Solutions:**

1. Verify the resource URI and that the target exists:
   ```shell
   wanaku resources list
   ```

2. Check file permissions if using file-based resources

3. Verify network access if using remote resources (S3, FTP, etc.)

4. Review provider service logs for errors

## Build and Deployment Issues

### Build Fails with Missing Dependencies

**Symptoms:**
- Maven build errors
- Missing artifact errors

**Solutions:**

1. Ensure you're using the correct Maven version:
   ```shell
   mvn --version  # Should be 3.x
   ```

2. Clear Maven cache and rebuild:
   ```shell
   rm -rf ~/.m2/repository/ai/wanaku
   mvn clean install
   ```

3. Verify internet connectivity for downloading dependencies

### Native Build Fails

**Symptoms:**
- Native compilation errors
- GraalVM-related failures

**Solutions:**

1. Verify GraalVM is properly installed:
   ```shell
   java -version  # Should show GraalVM
   native-image --version
   ```

2. Check the [Quarkus native build guide](https://quarkus.io/guides/building-native-image) for system requirements

3. Try building without native mode first to isolate the issue:
   ```shell
   mvn clean package
   ```

### Container Deployment Fails

**Symptoms:**
- Pods in CrashLoopBackOff state
- ImagePullBackOff errors

**Solutions:**

1. Verify the container image exists and is accessible:
   ```shell
   podman pull quay.io/wanaku/wanaku-router-backend:latest
   ```

2. Check pod logs for startup errors:
   ```shell
   oc logs <pod-name>
   kubectl logs <pod-name>
   ```

3. Verify ConfigMaps and Secrets are properly mounted:
   ```shell
   oc describe pod <pod-name>
   ```

4. Check resource limits and ensure sufficient memory/CPU

5. Verify Keycloak is accessible from the pods:
   ```shell
   oc exec <pod-name> -- curl http://keycloak:8080/health
   ```

## Performance Issues

### Slow Response Times

**Symptoms:**
- Tools take a long time to execute
- Resource reads are slow
- MCP clients experience timeouts

**Solutions:**

1. Check router and service resource usage:
   ```shell
   top
   htop
   ```

2. Review logs for errors or warnings

3. Verify network latency between components:
   ```shell
   ping <service-host>
   ```

4. Check Infinispan cache performance and consider adjusting:
   ```properties
   wanaku.infinispan.max-state-count=10
   ```

5. For Kubernetes deployments, ensure adequate resource limits:
   ```yaml
   resources:
     requests:
       memory: "512Mi"
       cpu: "500m"
     limits:
       memory: "1Gi"
       cpu: "1000m"
   ```

## Logging and Debugging

### Enable Debug Logging

To get more detailed logs for troubleshooting:

**Router backend:**
```properties
quarkus.log.level=DEBUG
quarkus.log.category."ai.wanaku".level=DEBUG
quarkus.mcp.server.traffic-logging.enabled=true
```

**Capability services:**
```properties
quarkus.log.level=DEBUG
quarkus.log.category."ai.wanaku".level=DEBUG
```

**CLI:**
```shell
wanaku --verbose tools list
```

### Access Logs

Check logs in these locations:

- **Router backend:** Look for `wanaku-router-backend.log` or check container logs
- **Capability services:** Check individual service log files
- **Kubernetes:** `oc logs <pod-name>` or `kubectl logs <pod-name>`

## Getting Help

If you continue to experience issues:

1. Check the [GitHub Issues](https://github.com/wanaku-ai/wanaku/issues) for similar problems
2. Review the [documentation](https://github.com/wanaku-ai/wanaku/tree/main/docs)
3. Join the [community discussions](https://github.com/wanaku-ai/wanaku/discussions)
4. Open a new issue with:
   - Wanaku version
   - Deployment environment (local, OpenShift, etc.)
   - Steps to reproduce
   - Relevant log excerpts
   - Configuration (with secrets redacted)

## Related Documentation

- [Installation Guide](installation.md) - Setup and deployment
- [Security Guide](security.md) - Authentication and authorization
- [Configuration Reference](configurations.md) - Configuration options
- [Architecture](architecture.md) - System design
- [FAQ](faq.md) - Frequently asked questions
