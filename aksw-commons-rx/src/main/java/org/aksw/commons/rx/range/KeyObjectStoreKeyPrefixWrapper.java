package org.aksw.commons.rx.range;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Iterables;

public class KeyObjectStoreKeyPrefixWrapper
    extends KeyObjectStoreDelegateBase
{
    protected List<String> prefix;

    public KeyObjectStoreKeyPrefixWrapper(KeyObjectStore delegate, List<String> prefix) {
        super(delegate);
        this.prefix = prefix;
    }

    public static KeyObjectStore wrap(KeyObjectStore delegate, List<String> prefix) {
        return new KeyObjectStoreKeyPrefixWrapper(delegate, prefix);
    }


    public List<String> getPrefix() {
        return prefix;
    }

    @Override
    public <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException {
        return super.get(Iterables.concat(prefix, keySegments));
    }

    @Override
    public void put(Iterable<String> keySegments, Object obj) throws IOException {
        super.put(Iterables.concat(prefix, keySegments), obj);
    }
}
