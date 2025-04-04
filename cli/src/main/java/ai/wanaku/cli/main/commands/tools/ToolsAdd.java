package ai.wanaku.cli.main.commands.tools;

import ai.wanaku.api.types.InputSchema;
import ai.wanaku.api.types.Property;
import ai.wanaku.api.types.ToolReference;
import ai.wanaku.cli.main.commands.BaseCommand;
import ai.wanaku.cli.main.services.ToolsService;
import ai.wanaku.cli.main.support.PropertyHelper;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import java.net.URI;
import java.util.List;
import org.jboss.logging.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "add",description = "Add tools")
public class ToolsAdd extends BaseCommand {
    private static final Logger LOG = Logger.getLogger(ToolsAdd.class);

    @CommandLine.Option(names = {"--host"}, description = "The API host", defaultValue = "http://localhost:8080",
            arity = "0..1")
    protected String host;

    @CommandLine.Option(names = {"-n", "--name"}, description="Name of the tool", required = true)
    private String name;

    @CommandLine.Option(names = {"-d", "--description"}, description="Description of the tool", required = true)
    private String description;

    @CommandLine.Option(names = {"-u", "--uri"}, description="URI of the tool", required = true)
    private String uri;

    @CommandLine.Option(names = {"--type"}, description="Type of the tool (i.e: http)", required = true)
    private String type;

    @CommandLine.Option(names = {"--input-schema-type"},
            description = "Type of input schema (e.g., 'string', 'object')",
            defaultValue = "object")
    private String inputSchemaType;

    @CommandLine.Option(names = {"-p", "--property"},
            description = "Property name and value (e.g., '--property name:type,description)")
    private List<String> properties;

    @CommandLine.Option(names = {"-r", "--required"},
            description = "List of required property names (e.g., '-r foo bar')", arity = "0..*")
    private List<String> required;

    ToolsService toolsService;

    @Override
    public void run() {
        ToolReference toolReference = new ToolReference();

        toolReference.setName(name);
        toolReference.setDescription(description);
        toolReference.setUri(uri);
        toolReference.setType(type);

        InputSchema inputSchema = new InputSchema();
        inputSchema.setType(inputSchemaType);

        if (properties != null) {
            for (String propertyStr : properties) {
                PropertyHelper.PropertyDescription result = PropertyHelper.parseProperty(propertyStr);

                Property property = new Property();
                property.setType(result.dataType());
                property.setDescription(result.description());

                inputSchema.getProperties().put(result.propertyName(), property);
            }
        }

        toolReference.setInputSchema(inputSchema);

        if (required != null) {
            inputSchema.setRequired(required);
        }

        toolsService = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(host))
                .build(ToolsService.class);

        toolsService.add(toolReference);
    }

}
