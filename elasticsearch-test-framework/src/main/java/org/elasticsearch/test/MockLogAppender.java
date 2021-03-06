package org.elasticsearch.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.elasticsearch.common.regex.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test appender that can be used to verify that certain events were logged correctly
 */
public class MockLogAppender extends AbstractAppender {

    private static final String COMMON_PREFIX = System.getProperty("es.logger.prefix", "org.elasticsearch.");

    private List<LoggingExpectation> expectations;

    public MockLogAppender() throws IllegalAccessException {
        super("mock", RegexFilter.createFilter(".*(\n.*)*", new String[0], false, null, null), null);
        expectations = new ArrayList<>();
    }

    public void addExpectation(LoggingExpectation expectation) {
        expectations.add(expectation);
    }

    @Override
    public void append(LogEvent event) {
        for (LoggingExpectation expectation : expectations) {
            expectation.match(event);
        }
    }

    public void assertAllExpectationsMatched() {
        for (LoggingExpectation expectation : expectations) {
            expectation.assertMatched();
        }
    }

    public interface LoggingExpectation {
        void match(LogEvent event);

        void assertMatched();
    }

    public abstract static class AbstractEventExpectation implements LoggingExpectation {
        protected final String name;
        protected final String logger;
        protected final Level level;
        protected final String message;
        volatile boolean saw;

        public AbstractEventExpectation(String name, String logger, Level level, String message) {
            this.name = name;
            this.logger = getLoggerName(logger);
            this.level = level;
            this.message = message;
            this.saw = false;
        }

        @Override
        public void match(LogEvent event) {
            if (event.getLevel().equals(level) && event.getLoggerName().equals(logger)) {
                if (Regex.isSimpleMatchPattern(message)) {
                    if (Regex.simpleMatch(message, event.getMessage().getFormattedMessage())) {
                        saw = true;
                    }
                } else {
                    if (event.getMessage().getFormattedMessage().contains(message)) {
                        saw = true;
                    }
                }
            }
        }
    }

    public static class UnseenEventExpectation extends AbstractEventExpectation {

        public UnseenEventExpectation(String name, String logger, Level level, String message) {
            super(name, logger, level, message);
        }

        @Override
        public void assertMatched() {
            assertThat(name, saw, equalTo(false));
        }
    }

    public static class SeenEventExpectation extends AbstractEventExpectation {

        public SeenEventExpectation(String name, String logger, Level level, String message) {
            super(name, logger, level, message);
        }

        @Override
        public void assertMatched() {
            assertThat(name, saw, equalTo(true));
        }
    }

    public static class PatternSeenEventExcpectation implements LoggingExpectation {

        protected final String name;
        protected final String logger;
        protected final Level level;
        protected final String pattern;
        volatile boolean saw;

        public PatternSeenEventExcpectation(String name, String logger, Level level, String pattern) {
            this.name = name;
            this.logger = logger;
            this.level = level;
            this.pattern = pattern;
        }

        @Override
        public void match(LogEvent event) {
            if (event.getLevel().equals(level) && event.getLoggerName().equals(logger)) {
                if (Pattern.matches(pattern, event.getMessage().getFormattedMessage())) {
                    saw = true;
                }
            }
        }

        @Override
        public void assertMatched() {
            assertThat(name, saw, equalTo(true));
        }

    }

    private static String getLoggerName(String name) {
        if (name.startsWith("org.elasticsearch.")) {
            name = name.substring("org.elasticsearch.".length());
        }
        return COMMON_PREFIX + name;
    }
}
