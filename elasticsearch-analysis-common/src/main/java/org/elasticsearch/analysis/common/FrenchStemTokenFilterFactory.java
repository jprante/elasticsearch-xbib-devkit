package org.elasticsearch.analysis.common;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;
import org.tartarus.snowball.ext.FrenchStemmer;

public class FrenchStemTokenFilterFactory extends AbstractTokenFilterFactory {

    private final CharArraySet exclusions;

    FrenchStemTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.exclusions = Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        tokenStream = new SetKeywordMarkerFilter(tokenStream, exclusions);
        return new SnowballFilter(tokenStream, new FrenchStemmer());
    }
}
