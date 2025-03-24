package ai.wanaku.core.uri;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URIHelperTest {

    @Test
    void testEmptyQuery() {
        String uri = URIHelper.buildUri("ftp://test", Map.of());
        assertEquals("ftp://test", uri);
    }

    @Test
    void testQuery() {
        String uri = URIHelper.buildUri("ftp://test", Map.of("key", "value"));
        assertEquals("ftp://test?key=value", uri);
    }


    @Test
    void testQueryTwoParams() {
        SortedMap <String, String> params = new TreeMap<>();

        params.put("key1", "value1");
        params.put("key2", "value2");
        String uri = URIHelper.buildUri("ftp://test", params);
        assertEquals("ftp://test?key1=value1&key2=value2", uri);
    }

}