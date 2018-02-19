package org.elasticsearch.index.mapper;

import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.InternalSettingsPlugin;
import org.junit.Before;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;

public abstract class AbstractNumericFieldMapperTestCase extends ESSingleNodeTestCase {
    protected Set<String> TYPES;
    protected Set<String> WHOLE_TYPES;
    protected IndexService indexService;
    protected DocumentMapperParser parser;

    @Before
    public void setup() {
        indexService = createIndex("test");
        parser = indexService.mapperService().documentMapperParser();
        setTypeList();
    }

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return pluginList(InternalSettingsPlugin.class);
    }


    protected abstract void setTypeList();

    public void testDefaults() throws Exception {
        for (String type : TYPES) {
            doTestDefaults(type);
        }
    }

    protected abstract void doTestDefaults(String type) throws Exception;

    public void testNotIndexed() throws Exception {
        for (String type : TYPES) {
            doTestNotIndexed(type);
        }
    }

    protected abstract void doTestNotIndexed(String type) throws Exception;

    public void testNoDocValues() throws Exception {
        for (String type : TYPES) {
            doTestNoDocValues(type);
        }
    }

    protected abstract void doTestNoDocValues(String type) throws Exception;

    public void testStore() throws Exception {
        for (String type : TYPES) {
            doTestStore(type);
        }
    }

    protected abstract void doTestStore(String type) throws Exception;

    public void testCoerce() throws Exception {
        for (String type : TYPES) {
            doTestCoerce(type);
        }
    }

    protected abstract void doTestCoerce(String type) throws IOException;

    public void testDecimalCoerce() throws Exception {
        for (String type : WHOLE_TYPES) {
            doTestDecimalCoerce(type);
        }
    }

    protected abstract void doTestDecimalCoerce(String type) throws IOException;

    public void testNullValue() throws IOException {
        for (String type : TYPES) {
            doTestNullValue(type);
        }
    }

    protected abstract void doTestNullValue(String type) throws IOException;

    public void testEmptyName() throws IOException {
        // after version 5
        for (String type : TYPES) {
            String mapping = XContentFactory.jsonBuilder().startObject().startObject("type")
                .startObject("properties").startObject("").field("type", type).endObject().endObject()
                .endObject().endObject().string();

            IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
                () -> parser.parse("type", new CompressedXContent(mapping))
            );
            assertThat(e.getMessage(), containsString("name cannot be empty string"));
        }
    }

}
