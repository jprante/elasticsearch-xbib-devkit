package org.elasticsearch.analysis.common;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.UpperCaseFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.MultiTermAwareComponent;

public class UpperCaseTokenFilterFactory extends AbstractTokenFilterFactory implements MultiTermAwareComponent {

    public UpperCaseTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new UpperCaseFilter(tokenStream);
    }

    @Override
    public Object getMultiTermComponent() {
        return this;
    }
}


