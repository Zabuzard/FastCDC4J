# FastCDC4J
[![maven-central](https://img.shields.io/maven-central/v/io.github.zabuzard.fastcdc4j/fastcdc4j)](https://img.shields.io/maven-central/v/io.github.zabuzard.fastcdc4j/fastcdc4j) [![javadoc](https://javadoc.io/badge2/io.github.zabuzard.fastcdc4j/fastcdc4j/javadoc.svg?style=flat)](https://javadoc.io/doc/io.github.zabuzard.fastcdc4j/fastcdc4j) [![Java](https://img.shields.io/badge/Java-14%2B-ff696c)](https://img.shields.io/badge/Java-14%2B-ff696c) [![license](https://img.shields.io/github/license/Zabuzard/FastCDC4J)](https://img.shields.io/github/license/Zabuzard/FastCDC4J)

FastCDC4J is a fast and efficient content-defined chunking solution for
data deduplication implementing the FastCDC algorithm and offering the
functionality as simple library.

It is able to split files into chunks, based on the content.
Chunks are created deterministic and will likely be preserved even if the
file is modified or data moved, hence it can be used for data deduplication.
It offers chunking of:

* `InputStream`
* `byte[]`
* `Path`, including directory traversal
* `Stream<Path>`

By utilizing the following built-in chunkers:

* FastCDC - Wen Xia et al. ([publication](https://www.usenix.org/system/files/conference/atc16/atc16-paper-xia.pdf))
* modified FastCDC - Nathan Fiedler ([source](https://github.com/nlfiedler/fastcdc-rs))
* Fixed-Size-Chunking

and providing a high degree of customizable by offering
ways to manipulate the algorithm.

The main interface of the chunkers provide the following methods:

* `Iterable<Chunk> chunk(InputStream stream, long size)`
* `Iterable<Chunk> chunk(final byte[] data)`
* `Iterable<Chunk> chunk(final Path path)`
* `Iterable<Chunk> chunk(final Stream<? extends Path> paths)`

# Requirements

* Requires at least **Java 14**

# Download

Maven:

```xml
<dependency>
   <groupId>io.github.zabuzard.fastcdc4j</groupId>
   <artifactId>fastcdc4j</artifactId>
   <version>1.3</version>
</dependency>
```

Jar downloads are available from the [release section](https://github.com/ZabuzaW/FastCDC4J/releases).

# Documentation

* [API Javadoc](https://javadoc.io/doc/io.github.zabuzard.fastcdc4j/fastcdc4j)
  or alternatively from the [release section](https://github.com/ZabuzaW/FastCDC4J/releases)

# Getting started

1. Integrate **FastCDC4J** into your project.
   The API is contained in the module `io.github.zabuzard.fastcdc4j`.
4. Create a chunker using `ChunkerBuilder`
5. Chunk files using the methods offered by `Chunker`

# Examples
Suppose you have a directory filled with lots of files that is frequently
modified and results have to be uploaded to a server.
However, you want to skip upload for data that was already uploaded in the past.

Hence, you want to chunk your files and set up a local chunk file cache.
If a chunk is already contained from a previous upload, upload can be skipped.

![Build example](https://i.imgur.com/kieqJtM.png)
![Cache example](https://i.imgur.com/o41I3n3.png)

```java
var buildPath = ...
var cachePath = ...

var chunker = new ChunkerBuilder().build();
var chunks = chunker.chunk(buildPath);

for (Chunk chunk : chunks) {
    var chunkPath = cachePath.resolve(chunk.getHexHash());
    if (!Files.exists(chunkPath)) {
        Files.write(chunkPath, chunk.getData());
        // Upload chunk ...
    }
}
```

Even if files in the build are modified or data is shifted around,
chunks will likely be preserved, resulting in an efficient data deduplication.

***

Directory traversal is executed single-threaded, however multi-threaded
chunking on a per-file base can be implemented easily:

```java
Consumer<? super Iterable<Chunk>> chunkAction = ...

// Files.walk has poor multi-threading characteristics, use a List instead
var files = Files.walk(buildPath)
    .filter(Files::isRegularFile)
    .collect(Collectors.toList());

files.parallelStream()
    .map(chunker::chunk)
    .forEach(chunkAction);
```

# Builder

The chunker builder `ChunkerBuilder` offers highly customizable algorithms.
Offered built-in chunkers are:

* `FastCDC`
* `Nlfiedler Rust` - a modified variant of `FastCDC`
* `Fixed Size Chunking`

It is also possible to add custom chunkers either by implementing
the interface `Chunker` or by implementing the simplified
interface `IterativeStreamChunkerCore`.
A chunker can be set by using `setChunkerOption(ChunkerOption)`,
`setChunkerCore(IterativeStreamChunkerCore)` and `setChunker(Chunker)`.

***

The chunkers will try to strive for an expected chunk size
settable by `setExpectedChunkSize(int)`. A minimal size given
by `setMinimalChunkSizeFactor(double)` and a maximal size given
by `setMaximalChunkSizeFactor(double)`.

***

Most of the chunkers internally use a hash table as source for
predicted noise to steer the algorithm, a custom table can be
provided by `setHashTable(long[])`.
Alternatively, `setHashTableOption(HashTableOption)` can be used
to choose from predefined tables:

* `RTPal`
* `Nlfiedler Rust`

***

The algorithms are heavily steered by masks which define the cut-points.
By default, they are generated randomly using a fixed seed that can
be changed by using `setMaskGenerationSeed(long)`.

There are different techniques available to generate masks,
they can be set using `setMaskOption(MaskOption)`:
* `FastCDC`
* `Nlfiedler Rust`

To achieve a distribution of chunk sizes as close as possible to
the expected size, normalization levels are used during mask generation.
`setNormalizationLevel(int)` is used to change the level.
The higher the level, the closer the sizes are to the expected size,
for the cost of a worse deduplication rate.

Alternatively, masks can be set manually using `setMaskSmall(long)`
for the mask used when the chunk is still smaller than the expected
size and `setMaskLarge(long)` for bigger chunks respectively.

***

After a chunk has been read, a hash is generated based on its content.
The algorithm used for this process can be set by `setHashMethod(String)`,
it has to be supported and accepted by `java.security.MessageDigest`.

***

Finally, a chunker using the selected properties can be created using `build()`.

The **default configuration** of the builder is:
* Chunker option: `ChunkerOption#FAST_CDC`
* Expected size: `8 * 1024`
* Minimal size factor: `0.25`
* Maximal size factor: `8`
* Hash table option: `HashTableOption#RTPAL`
* Mask generation seed: `941568351`
* Mask option: `MaskOption#FAST_CDC`
* Normalization level: `2`
* Hash method: `SHA-1`

The methods `fastCdc()`, `nlFiedlerRust()` and `fsc()` can be used to
get a configuration that uses the given algorithms as originally proposed.
