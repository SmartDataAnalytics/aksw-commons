## AKSW Commons

A modular utility collection for solving recurrent basic tasks in a productive and robust way.

### Modules

* [Lambdas: Serializable](aksw-commons-lambdas-parent/aksw-commons-lambdas-serializable): Interfaces derived from the Java8 functions and collector ones that extend Serializable.
* [Lambdas: Throwing](aksw-commons-lambdas-parent/aksw-commons-lambdas-throwing): Alternate version of the Java8 function interfaces that declare to throw exceptions.
* [Beans](aksw-commons-beans): An API to wrap an entity and declare custom getters+setters (including type conversions such as Integer-to-Long), annotations, constructors. For example, if a wrapped class does not expose a no-arg ctor, then the model allows for providing a lambda that can be used instead. This module does not depend on spring-core directly, but e.g. the [ConversionServiceAdapter](aksw-commons-beans/src/main/java/org/aksw/commons/beans/model/ConversionServiceAdapter.java) is designed for spring interoperability.
* [Collectors](aksw-commons-collectors) A framework for composable serializable aggregators suitable for application in map/reduce scenarios. The central class is [ParallelAggregator](aksw-commons-collectors/src/main/java/org/aksw/commons/collector/domain/ParallelAggregator.java). The resulting aggregators support being viewed as Java8 collectors for use with Java8 streams and can also be serialized for parallel computation with e.g. Hadoop/Apache Spark. Depends on *Lambdas: Serializable*.
* [Collection utilities](aksw-commons-collections): Features mutable collection views with corresponding iterators and miscellaneous classes for special use cases.
* [Entity Codec: Core](aksw-commons-entity-codecs-parent/aksw-commons-entity-codecs-core): A framework for composable encoding and decoding entities of type "T" (in contrast to codecs usually tied to byte[]). The main use case is quoting and escaping of strings.
* [Entity Codec: SQL](aksw-commons-entity-codecs-parent/aksw-commons-entity-codecs-sql): An adaption of *Entity Codecs: Core* for the SQL domain. Features the [SqlCodec](aksw-commons-entity-codecs-parent/aksw-commons-entity-codecs-sql/src/main/java/org/aksw/commons/sql/codec/api/SqlCodec.java) interface which bundles codecs for for the various SQL identifier types such as column names, table names and aliases.
* [SQL](aksw-commons-sql) Utilities to parse SQL statements (using [JSqlParser](https://github.com/JSQLParser/JSqlParser) and (re-)encode mentioned identifiers using *Entity Codec: SQL)*.
* [RX](aksw-commons-rx) Additional operators and utilities for the Reactive eXtensions for Java [RxJava](https://github.com/ReactiveX/RxJava). Most prominently features on-line aggregation of consecutive items belonging to the same group using [FlowableOperatorSequentialGroupBy](aksw-commons-rx/src/main/java/org/aksw/commons/rx/op/FlowableOperatorSequentialGroupBy.java). Also includes an operator for [measuring throughput](aksw-commons-rx/src/main/java/org/aksw/commons/rx/op/OperatorObserveThroughput.java).
* [XML](aksw-commons-util-xml): Static convenience methods for loading XML and evaluating XPath expressions.


### Where is it used?

AKSW Commons is essentially the jena-idenpendent code from our [jena-sparql-api](https://github.com/SmartDataAnalytics/jena-sparql-api) Semantic Web toolkit.

* [RDFUnit](https://github.com/AKSW/RDFUnit): RDF data quality assessment framework
* [DL-Learner](https://github.com/AKSW/RDFUnit): Framework for symbolic machine learning
* [Facete](https://github.com/AKSW/RDFUnit): Faceted Search framework
* [SANSA](https://github.com/AKSW/RDFUnit): Big Data RDF Processing and Analytics framework


### License

This code is released under the Apache License Version 2.0.

