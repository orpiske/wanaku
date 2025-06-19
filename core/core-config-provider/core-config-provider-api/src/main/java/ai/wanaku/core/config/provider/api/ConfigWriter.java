package ai.wanaku.core.config.provider.api;

import java.net.URI;

public interface ConfigWriter {

    URI write(String id, String data);
}
