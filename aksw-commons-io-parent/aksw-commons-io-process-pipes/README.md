Low-level High-performance IO components



## Process Pipes
This is an abstraction that enables transparent use and/or fallback to system calls (when they are available).
The framework enables transformation of input either as a file or an input stream to output either as a file or output stream.
In addition the construction of pipes is possible i.e. using the data of an output stream as input to the subsequent processor.


Typically processes can read from an input stream or a file and write to an output stream or a file.
Reading from a file allows for split-based processing (such as performed by Hadoop) and may thus be superior to consuming data
sequentially from an input stream.



### System Call Specifications
The central interface to capture the capabilities of a system call is shown below.
Note, that only `cmdStreamToStream` needs to be specified because the path based versions can be realized using it alone.
However, this prevents potential performance benefits of using file-based io (such as bypassing the JVM).


```java
public interface SysCallPipeSpec {
    default String[] cmdStreamToStream() { return null; }
    default Function<Path, String[]> cmdBuilderStreamToPath() { return null; }
    default Function<Path, String[]> cmdBuilderPathToStream() { return null; }
    default Function<Path, Path> cmdBuilderPathToPath() { return null; }
}
```


For example, lbzip2 may be described using:
```java
public class SysCallPipeSpecLbzip2 implements SysCallPipeSpec {
   // -c indicates to output to STDOUT
   @Override default String[] cmdStreamToStream() { return new String[]{"/usr/bin/lbzip2", "-c"}; }
}
```


### PipeTransforms
The SysCallPipeSpec describes only a system call and is thus unsuitable for describing a Java native transformation.
The Java-abstraction is provided by `PipeTransform` which provide the methods to turn a Java InputStream or Path into an OutputStream or Path:

```java
public interface PipeTransform {
    default Function<InputStream, InputStream> mapStreamToStream() { return null; }
    default Function<Path, InputStream> mapPathToStream() { return null; }
    default BiFunction<Path, Path, FileCreation> mapPathToPath() { return null; }
    default BiFunction<InputStream, Path, FileCreation> mapStreamToPath() { return null; }
}
```

### Codecs
A codecs is a pair comprised of a transformation function (the encoder) together with its inverse (the decoder).
In the case of **lossless** codecs it holds that `input = decode(encode(input))`.
Compression codecs are forms of codecs that typically operate on sequences of bytes.

```
public interface Codec {
    PipeTransform encoder();
    PipeTransform decoder();
}

```




