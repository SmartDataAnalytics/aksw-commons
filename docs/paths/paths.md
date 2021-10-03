# Path Abstraction

## Essentials

In order to reference an arbitrary piece of data it is necessary to have representation for such a reference.
Paths are somewhere between simple identifiers and query languages. Foremost, a fundamental aspect of paths is that they are sequences of segments
for which `parent` and `children` functions are defined.

The main classes introduced by this module are:

* [Path](../../aksw-commons-paths/src/main/java/org/aksw/commons/path/core/Path.java) which provides all the non-filesystem-specific methods of `java.nio.Path` generalized to an arbitary segment type `<T>`.
* [PathBase](../../aksw-commons-paths/src/main/java/org/aksw/commons/path/core/PathBase.java) is a base implementation that eventually delegates all methods to a `PathOps` instance.
* [PathOps](aksw-commons-paths/src/main/java/org/aksw/commons/path/core/PathOps.java) bundles common functionality for paths, namely path construction, string de-/serialization, comparison and self/parent token declarations (such as `.` and `..`).


The following default implementations are provided which also serve as an example for custom implementations:

* [PathNio](develop/aksw-commons-paths/src/main/java/org/aksw/commons/path/core/PathNio.java) is a wrapper for `java.nio.Path`. It enables passing nio paths to any function expecting a `Path<String>` type.
* [PathStr](develop/aksw-commons-paths/src/main/java/org/aksw/commons/path/core/PathStr.java) is an implementation based on conventional strings. In constrast to `java.nio.Path` there is no dependency on file system specifics.


This module only serves as a framework for more powerful path implementations, such as where segments are predicate expressions which filter columns of tables to matching rows.

Essential features of this path framework are both the string **de-/serialization** which makes paths suitable in distributed computing as well as the **comparison** which allows for testing whether two references are equal and thus point to the same entity. Any implementation for a custom segment type should therefore pay attention to these aspects.

## Examples

The following examples demonstrate the use of the PathStr implementation.


```java
import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;

Path<String> a = PathOpsStr.create("/hello/world/");
Path<String> b = a.resolve("/hello/world");
Assert.assertEquals(a, b); // true because trailing slashes are ignored

Path<String> c = a.resolve("hi"));

System.out.println(c);
// /hello/world/hi

System.out.println(a.relativize(c))
// hi

System.out.println(c.relativize(c))
// ..

```




