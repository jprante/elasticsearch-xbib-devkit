package org.elasticsearch.analysis.common;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.minhash.MinHashFilterFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TokenFilterFactoryAdapter for {@link MinHashFilterFactory}
 *
 */
public class MinHashTokenFilterFactory extends AbstractTokenFilterFactory {

    private final MinHashFilterFactory minHashFilterFactory;

    MinHashTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        minHashFilterFactory = new MinHashFilterFactory(convertSettings(settings));
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return minHashFilterFactory.create(tokenStream);
    }

    private Map<String, String> convertSettings(Settings settings) {
        Map<String, String> settingMap = new HashMap<>();
        settingMap.put("hashCount", settings.get("hash_count"));
        settingMap.put("bucketCount", settings.get("bucket_count"));
        settingMap.put("hashSetSize", settings.get("hash_set_size"));
        settingMap.put("withRotation", settings.get("with_rotation"));
        return settingMap;
    }
}