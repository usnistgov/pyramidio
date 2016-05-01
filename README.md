# PyramidIO: image pyramid reader/writer tool

## CLI usage

The CLI allows to build a DZI pyramid from an image.
To start the CLI, one should use `pyramidio-cli-[version].jar` like this:

```
java -jar pyramidio-cli-[version].jar -i my-image.jpg -o (my-output-folder || scheme:///path/file[.tar, .seq]
```

```
Examples:
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o outputfolder
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o file:///tmp/outputfolder.tar
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o s3://my-image-bucket/outputfolder
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o hdfs://localhost:9000/outputfolder
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o hdfs://localhost:9000/outputfolder.tar
* java -jar pyramidio-cli-[version].jar -i my-image.jpg -o hdfs://localhost:9000/outputfolder.seq

```

To get the list of all the options, one can type:
```
java -jar pyramidio-cli-[version].jar -h
```

## Library usage

### Maven dependency

To use PyramidIO as a library, one should setup the maven dependencies like this:

```
<dependency>
    <groupId>gov.nist.isg</groupId>
    <artifactId>pyramidio</artifactId>
    <version>...</version>
</dependency>
```

Add the `tar-archiver` artifact for tar support and `hdfs-archiver` for HDFS support.

### Write a DZI pyramid

To write a DZI pyramid, one should use the gov.nist.isg.pyramidio.ScalablePyramidBuilder class:
```java
ScalablePyramidBuilder spb = new ScalablePyramidBuilder(tileSize, tileOverlap, tileFormat, "dzi");
FilesArchiver archiver = new DirectoryArchiver(outputFolder);
PartialImageReader pir = new BufferedImageReader(imageFile);
spb.buildPyramid(pir, "pyramidName", archiver, parallelism);
```
Currently the available `FilesArchiver`s are:
* `DirectoryArchiver`: save files in a directory on the filesystem.
* `TarArchiver`: save files in a tar file on the filesystem.
* `SequenceFileArchiver`: save files in a Hadoop sequence file.
* `HdfsArchiver`: save files on a HDFS filesystem.
* `TarOnHdfsArchiver`: save files in a tar file created on a HDFS filesystem.
* `S3Archiver`: save files to a folder on a S3 bucket.

As for the `PartialImageReader`s:
* `BufferedImageReader`: read an image from the disk and store it in RAM.
* `DeepZoomImageReader`: read a DZI pyramid.
* `MistStitchedImageReader`: read a [MIST](https://github.com/NIST-ISG/MIST) translation vector.

### Read a DZI pyramid

To read a DZI pyramid, one should use the `DeepZoomImageReader` class:
```java
File dziFile = new File("my-image.dzi");
DeepZoomImageReader reader = new DeepZoomImageReader(dziFile);
BufferedImage wholeImageZoom0_01 = reader.getWholeImage(0.01);
BufferedImage regionAtZoom0_1 = reader.getSubImage(
    new Rectangle(x, y, width, height), 0.1);
```

## Disclaimer:

This software was developed at the National Institute of Standards and Technology by employees of the Federal Government in the course of their official duties. Pursuant to title 17 Section 105 of the United States Code this software is not subject to copyright protection and is in the public domain. This software is an experimental system. NIST assumes no responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic. We would appreciate acknowledgement if the software is used.
