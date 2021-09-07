---
title: Overview
nav_order: 1
---

# Documentation

This is the documentation of aksw-commons.
It contains domain designs and utilities with as few dependencies as possible (most prominently guava).

## Highlights

* Aggregator Composition Framework
    * Write aggregators for Java8 Streams and use them with e.g. Apache Spark. Aggregators are serializable and meant for parallel computing.
    * Used in [SANSA Stack](https://github.com/SmartDataAnalytics/aksw-commons)

* Path Traversal Framework
    * Introduces `Path<T>` which has all non-filesystem dependent methods as `java.nio.file.Path`. Can be easily adapted to any kind of segement type by providing an implementation of `PathOps<T>`. Also provides a native wrapper for nio paths.





