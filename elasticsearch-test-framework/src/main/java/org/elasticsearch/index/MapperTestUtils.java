package org.elasticsearch.index;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.similarity.SimilarityService;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.indices.mapper.MapperRegistry;
import org.elasticsearch.test.IndexSettingsModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.elasticsearch.test.ESTestCase.createTestAnalysis;


public class MapperTestUtils {

    public static MapperService newMapperService(NamedXContentRegistry xContentRegistry,
                                                 Path tempDir,
                                                 Settings indexSettings,
                                                 String indexName) throws IOException {
        IndicesModule indicesModule = new IndicesModule(Collections.emptyList());
        return newMapperService(xContentRegistry, tempDir, indexSettings, indicesModule, indexName);
    }

    public static MapperService newMapperService(NamedXContentRegistry xContentRegistry, Path tempDir, Settings settings,
                                                 IndicesModule indicesModule, String indexName) throws IOException {
        Settings.Builder settingsBuilder = Settings.builder()
            .put(Environment.PATH_HOME_SETTING.getKey(), tempDir)
            .put(settings);
        if (settings.get(IndexMetaData.SETTING_VERSION_CREATED) == null) {
            settingsBuilder.put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT);
        }
        Settings finalSettings = settingsBuilder.build();
        MapperRegistry mapperRegistry = indicesModule.getMapperRegistry();
        IndexSettings indexSettings = IndexSettingsModule.newIndexSettings(indexName, finalSettings);
        IndexAnalyzers indexAnalyzers = createTestAnalysis(indexSettings, finalSettings).indexAnalyzers;
        SimilarityService similarityService = new SimilarityService(indexSettings, null, Collections.emptyMap());
        return new MapperService(indexSettings,
            indexAnalyzers,
            xContentRegistry,
            similarityService,
            mapperRegistry,
            () -> null);
    }
}
