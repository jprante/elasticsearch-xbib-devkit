package org.elasticsearch.test;

import org.elasticsearch.cluster.Diff;
import org.elasticsearch.cluster.Diffable;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.common.xcontent.ToXContent;

import java.io.IOException;

/**
 * An abstract test case to ensure correct behavior of Diffable.
 *
 * This class can be used as a based class for tests of MetaData.Custom classes and other classes that support,
 * Writable serialization, XContent-based serialization and is diffable.
 */
public abstract class AbstractDiffableSerializationTestCase<T extends Diffable<T> & ToXContent> extends AbstractSerializingTestCase<T> {

    /**
     *  Introduces random changes into the test object
     */
    protected abstract T makeTestChanges(T testInstance);

    protected abstract Reader<Diff<T>> diffReader();

    public void testDiffableSerialization() throws IOException {
        DiffableTestUtils.testDiffableSerialization(this::createTestInstance, this::makeTestChanges, getNamedWriteableRegistry(),
            instanceReader(), diffReader());
    }

}
