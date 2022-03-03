package org.aksw.commons.util.ref;

import java.time.Instant;

import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import junit.framework.Assert;

public class TestSingletonLazyRef {

    @Test
    public void test() throws InterruptedException {
        SerializableSupplier<Ref<Instant>> refSupp = () -> RefImpl.create(Instant.now(), (Object)null, (AutoCloseable)null);

        // Wrapping as a singleton should now always yield the same instant - even after a serialization roundtrip
        SingletonLazyRef<Instant> singleton = SingletonLazyRef.create(refSupp);

        Ref<Instant> ref1 = singleton.get();
        Instant expected = ref1.get();

        try (Ref<Instant> ref2 = singleton.get()) {
            Instant actual2 = ref2.get();
            Assert.assertEquals(expected, actual2);
        }

        SingletonLazyRef<Instant> newSingleton = SerializationUtils.roundtrip(singleton);

        try (Ref<Instant> ref3 = newSingleton.get()) {
            Instant actual3 = ref3.get();
            Assert.assertEquals(expected, actual3);
        }


        // Closing the primary ref should cause the next request to refSupp to create a ref with a fresh value
        ref1.close();
        Thread.sleep(1);

        try (Ref<Instant> newRef = newSingleton.get()) {
            Instant newExpected = newRef.get();
            Assert.assertNotSame(newExpected, expected);
        }
    }
}
