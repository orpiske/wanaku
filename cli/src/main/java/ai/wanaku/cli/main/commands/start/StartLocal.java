package ai.wanaku.cli.main.commands.start;

import jakarta.inject.Inject;

import ai.wanaku.api.exceptions.WanakuException;
import ai.wanaku.cli.main.support.WanakuCliConfig;
import ai.wanaku.cli.runner.local.LocalRunner;
import ai.wanaku.cli.types.WanakuDeployment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.jboss.logging.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "local",description = "Create a new tool service")
public class StartLocal extends StartBase {
    private static final Logger LOG = Logger.getLogger(StartLocal.class);

    @Inject
    WanakuCliConfig config;

    @Override
    protected void startWanaku() {
        ObjectMapper mapper = new ObjectMapper();

        WanakuDeployment wanakuDeployment = null;
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/services-default.json")) {
             wanakuDeployment = mapper.readValue(resourceAsStream, WanakuDeployment.class);

        } catch (IOException e) {
            throw new WanakuException(e);
        }

        LocalRunner localRunner = new LocalRunner();
        try {
            localRunner.start(wanakuDeployment);
        } catch (WanakuException e) {
            System.out.println(e.getMessage());

            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        startWanaku();
    }
}
