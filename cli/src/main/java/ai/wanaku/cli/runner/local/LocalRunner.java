package ai.wanaku.cli.runner.local;

import ai.wanaku.cli.types.Environment;
import ai.wanaku.cli.types.Service;
import ai.wanaku.cli.types.WanakuDeployment;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LocalRunner {

    private final DockerClientConfig config;
    private final DockerClient dockerClient;

    public LocalRunner() {
        this(null, null);
    }

    public LocalRunner(String username, String password) {
        this.config = configureClient(username, password);
        this.dockerClient = createDockerClient();
        ;
    }

    private DockerClientConfig configureClient(String username, String password) {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryUsername(username)
                .withRegistryPassword(password)
                .build();
    }

    private DockerClient createDockerClient() {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null) {
            dockerHost = "unix:///var/run/docker.sock";
        }

        ZerodepDockerHttpClient client = new ZerodepDockerHttpClient.Builder()
                .dockerHost(URI.create(dockerHost))
                .build();

        return DockerClientBuilder
                .getInstance(config)
                .withDockerHttpClient(client)
                .build();
    }

    public void start(WanakuDeployment deployment) {
        List<Network> exec = dockerClient.listNetworksCmd().exec();
        Optional<Network> networkOptional = exec.stream()
                .filter(n -> n.getName().equals("wanaku-net"))
                .findAny();

        if (!networkOptional.isPresent()) {
            CreateNetworkResponse wanaku = dockerClient.createNetworkCmd().withName("wanaku-net").exec();
        }



        List<Service> infrastructure = deployment.getInfrastructure();
        for (Service service : infrastructure) {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            boolean exists = containers.stream().anyMatch(container ->
                    Arrays.asList(container.getNames()).contains(service.getName()));

            if (exists) {
              dockerClient.startContainerCmd(service.getName()).exec();
            } else {
                dockerClient.createContainerCmd(service.getImage())
                        .withName(service.getName())
                        .withHostConfig(HostConfig.newHostConfig().withNetworkMode("wanaku-net"))
                        .exec();
            }
        }

        Environment environment = deployment.getEnvironment();
        List<String> envs = environment.getVariables();
        for (var entry : envs) {
            System.out.println("Env: " + entry);
        }
    }
}
