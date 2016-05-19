/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.isg.pyramidio;

import gov.nist.isg.archiver.FilesArchiver;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Pyramid builder class which is scalable for very large images not fitting in
 * memory while minimizing the read accesses.
 *
 * Each region forming a tile at the maximum zoom level is read only once. A
 * tile at level n is computed as soon as enough tiles at level n+1 have been
 * computed. (A tile at level n is composed of 4 tiles at level n+1).
 *
 * @author Antoine Vandecreme
 */
public class ScalablePyramidBuilder {

    private final int tileSize;
    private final int overlap;
    private final String tileFormat;
    private final String descriptorExt;

    public ScalablePyramidBuilder() {
        this(254, 1, "png", "dzi");
    }

    /**
     * Create a new pyramid builder
     *
     * @param tileSize the tile size to use (default 254)
     * @param overlap the overlap between tiles (default 1)
     * @param tileFormat the file format to use for the tiles (default png)
     * @param descriptorExt the descriptor extension (default dzi)
     */
    public ScalablePyramidBuilder(int tileSize, int overlap, String tileFormat,
            String descriptorExt) {
        this.tileSize = tileSize;
        this.overlap = overlap;
        this.tileFormat = tileFormat;
        this.descriptorExt = descriptorExt;
        ImageIO.setUseCache(false);
    }

    /**
     * Build the DeepZoom pyramid from the given image reader.
     *
     * @param imageReader the image reader used to generate the deep zoom image
     * @param fileName the filename of the image
     * @param archiver the archiver to use to store the image
     * @throws IOException
     */
    public void buildPyramid(PartialImageReader imageReader, String fileName,
            FilesArchiver archiver) throws IOException {
        buildPyramid(imageReader, fileName, archiver, 1);
    }

    /**
     * Build the DeepZoom pyramid from the given image reader.
     *
     * @param imageReader the image reader used to generate the deep zoom image
     * @param fileName the filename of the image
     * @param archiver the archiver to use to store the image
     * @param parallelism the parallelism level
     * @throws IOException
     */
    public void buildPyramid(PartialImageReader imageReader, String fileName,
            FilesArchiver archiver, int parallelism) throws IOException {
        buildPyramid(imageReader, fileName, archiver, parallelism, 0);
    }

    /**
     * Build the DeepZoom pyramid from the given image reader.
     *
     * @param imageReader the image reader used to generate the deep zoom image
     * @param fileName the filename of the image
     * @param archiver the archiver to use to store the image
     * @param parallelism the parallelism level
     * @param maxImageCachePercentage the maximum portion of the input image
     * which should be cached in RAM. Set to 0 for no cache, 1 to cache the
     * entire image. Default to 0.
     * @throws IOException
     */
    public void buildPyramid(PartialImageReader imageReader, String fileName,
            FilesArchiver archiver, int parallelism, float maxImageCachePercentage) throws IOException {
        new TileBuilder(tileSize, overlap, tileFormat, descriptorExt,
                imageReader, fileName, archiver)
                .build(parallelism, maxImageCachePercentage);
    }

}
