# IO Framework

The IO framework provides special purpose abstractions which are neither covered by java.nio nor hadoop.

## ArrayOps

The purpose of the ArrayOps interface is to enable operations over arrays regardless whether the items are of primitive types or non-primitive types.
Essentially it provides read and write methods to transfer items in batch to or from an array (or more generically: an array-like object).

An excerpt of the interface is shown below. The most relevant method is `copy` which can be implemented efficiently to transfer items in batch
across array-like objects.
For example, there exist dedicated implementations exist for `byte[]` and `Object[]` which internally leverage System.arraycopy and thus avoid
slow access to individual items.

```java
public interface ArrayOps<A> {
    A create(int size);

    Object get(A array, int index);
    void set(A array, int index, Object value);

    int length(A array);

    void fill(A array, int offset, int length, Object value);
    void copy(A src, int srcPos, A dest, int destPos, int length);
    Object getDefaultValue();
}
```

The `HasArrayOps` interface can be used on implementations to expose their underlying `ArrayOps`.

```java
public interface HasArrayOps<A> {
    ArrayOps<A> getArrayOps();
}
```

## DataStreams

The rationale for DataStreams is to provide a common infrastructure to operate on streams of both primitive types (especially bytes) and objects.
This is an enabler for compontents that can efficiently operate on both primitive and and non-primitive types, such as our AdvancedRangeCache.

The DataStream is designed to be compatbile with  `java.nio.ReadableByteChannel` but generalizes the read operation to arbitrary arrays (or lists) of objects.

```java
public interface DataStream<A>
    extends HasArrayOps<A>, Closeable
{
    boolean isOpen();
    int read(A array, int position, int length) throws IOException ;
}
```

### Bridging DataStreams to Conventional Types

The class `DataStreams` provides methods that bridge data streams to conventional types, namely Iterator, ReadableByteChannel, and InputStream.


```java
DataStream<byte[]> dataStream = DataStream.of(ArrayOps.BYTE, new byte[]);
Iterator<Byte> DataStreams.newBoxedIterator(dataStream);

DataStream<byte[]> xxx = DataStreams.of(ArrayOps.BYTE, new byte[] {'a', 'b', 'c'});
Iterator<Byte> it = DataStreams.newBoxedIterator(xxx);
while (it.hasNext()) {
    System.out.println((char)it.next().byteValue());
}
// Output is:
// a
// b
// c


ReadableByteChannel channel = DataStream.newChannel(dataStream);
InputStream in = DataStreams.newInputStream(dataStream);
```


## Caching of DataStreamSources

`DataStreamSources` provides the `cache` methods for setting up in-memory and disk-based caching.
The cache system is designed to cache any accessed data concurrently regardless of the amount of data being accessed.
The advanced cache implementations manage data internally in pages.
Pages can be concurrently synchronized to disk even while they are being in used by consumer or producer threads.
Once the number of pages exceed a configurable threshold, unused pages will be evicted from memory.
Accessing an evicted page can either attempt to reload data cached on the disk or trigger retrieval of fresh data from the source.

A basic setup of caching is shown below:


```java
java.nio.Path cacheBaseFolder = Path.of(StandardSystemProperties.JAVA_IO_TMPDIR.value());
DataStreamSource<byte[]> source = DataStreamSources.of(java.nio.Path.of("myFile"));

DataStreamSource<byte[]> cached;

boolean useDisk = true;
AdvancedRangeCacheConfig cacheConfig = AdvancedRangeCacheConfigImpl.createDefault();
if (useDisk) {
    cached = DataStreamSources.cache(source, cacheBaseFolder, "cacheEntryForMyFile", cacheConfig);
} else {
    cached = DataStreamSources.cache(
                 source,
                 // Cache up to 100 pages with a capacity of 4096 items
                 SliceInMemoryCache.create(ArrayOps.BYTE, 4096, 100),
                 cacheConfig);
}

DataStream<byte[]> cachedDataStream = cached.newDataStream(Range.atLeast(1000));
```

Any reads from a cached data stream updates the cache.
The caching system takes care of coordinating concurrent requests and scheduling retrievals from the source.


Disk-based caching uses the Kryo framework for serialization.
An overload of the `cache()` function exists which accepts a custom KryoPool.


