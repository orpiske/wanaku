package ai.wanaku.cli.main.commands.start;

import ai.wanaku.api.exceptions.WanakuException;
import ai.wanaku.cli.main.commands.BaseCommand;
import ai.wanaku.core.util.ProcessRunner;
import ai.wanaku.core.util.VersionHelper;
import java.io.File;
import org.jboss.logging.Logger;
import picocli.CommandLine;

public abstract class StartBase extends BaseCommand {
    private static final Logger LOG = Logger.getLogger(StartBase.class);

    @CommandLine.Option(names = { "--file" }, description = "An optional service file for starting Wanaku", arity = "0..1")
    protected String name;

    protected abstract void startWanaku();

    protected void createProject(String baseCmd) {
//        String cmd = String.format("%s -DartifactId=wanaku-routing-%s-service -Dname=%s -Dwanaku-version=%s",
//                baseCmd, name.toLowerCase(), name, VersionHelper.VERSION);
//
//        String[] split = cmd.split(" ");
//        final File projectDir = new File(path);
//        try {
//            ProcessRunner.run(projectDir, split);
//        } catch (WanakuException e) {
//            LOG.error(e.getMessage(), e);
//            System.exit(-1);
//        }
    }
}
