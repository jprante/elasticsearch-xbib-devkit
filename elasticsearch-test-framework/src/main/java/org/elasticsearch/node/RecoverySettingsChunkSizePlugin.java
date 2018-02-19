package org.elasticsearch.node;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.indices.recovery.RecoverySettings;
import org.elasticsearch.plugins.Plugin;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Marker plugin that will trigger {@link MockNode} making {@link #CHUNK_SIZE_SETTING} dynamic.
 */
public class RecoverySettingsChunkSizePlugin extends Plugin {
    /**
     * The chunk size. Only exposed by tests.
     */
    public static final Setting<ByteSizeValue> CHUNK_SIZE_SETTING = Setting.byteSizeSetting("indices.recovery.chunk_size",
            RecoverySettings.DEFAULT_CHUNK_SIZE, Property.Dynamic, Property.NodeScope);

    @Override
    public List<Setting<?>> getSettings() {
        return singletonList(CHUNK_SIZE_SETTING);
    }
}
