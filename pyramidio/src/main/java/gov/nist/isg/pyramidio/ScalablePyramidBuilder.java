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
import gov.nist.isg.pyramidio.tools.BufferedImageHelper;
import gov.nist.isg.pyramidio.tools.ImageResizingHelper;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;

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
        new TileBuilder(imageReader, fileName, archiver)
                .buildTile(0, 0, 0, parallelism);
    }

    private class TileBuilder {

        private final PartialImageReader imageReader;

        private final FilesArchiver archiver;
        private final int nbLevels;

        private final String imgDir;

        private final int originalWidth;
        private final int originalHeight;

        public TileBuilder(PartialImageReader imageReader, String fileName,
                FilesArchiver archiver) throws IOException {
            this.imageReader = imageReader;
            this.archiver = archiver;

            originalWidth = imageReader.getWidth();
            originalHeight = imageReader.getHeight();

            String nameWithoutExtension = FilenameUtils.getBaseName(fileName);
            String descriptorName = nameWithoutExtension + '.' + descriptorExt;
            DziFile dziFile = new DziFile(tileSize, overlap, tileFormat,
                    originalWidth, originalHeight);
            dziFile.write(descriptorName, archiver);

            int maxDim = Math.max(originalWidth, originalHeight);
            nbLevels = (int) Math.ceil(Math.log(maxDim) / Math.log(2));

            imgDir = fileName + "_files";
        }

        public BufferedImage buildTile(int level, int tileRow,
                int tileColumn, int parallelism) throws IOException {
            if (parallelism <= 1) {
                return buildTile(level, tileRow, tileColumn);
            }

            ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
            try {
                return forkJoinPool.invoke(
                        new TileBuilderTask(level, tileRow, tileColumn));
            } finally {
                forkJoinPool.shutdownNow();
            }
        }

        private BufferedImage buildTile(int level, int tileRow, int tileColumn)
                throws IOException {
            BufferedImage result;

            if (level == nbLevels) {
                result = getTile(tileRow, tileColumn);
            } else {
                Dimension tileDimensions = getTileDimensions(
                        level, tileRow, tileColumn);

                if (tileDimensions.width == 0 || tileDimensions.height == 0) {
                    return null;
                }

                // The tile we are currently computing is a downsampling of
                // 4 tiles at level + 1 (except in the corners)
                BufferedImage topLeft = buildTile(
                        level + 1, tileRow * 2, tileColumn * 2);
                BufferedImage topRight = buildTile(
                        level + 1, tileRow * 2, tileColumn * 2 + 1);
                BufferedImage bottomLeft = buildTile(
                        level + 1, tileRow * 2 + 1, tileColumn * 2);
                BufferedImage bottomRight = buildTile(
                        level + 1, tileRow * 2 + 1, tileColumn * 2 + 1);

                int bigWidth = topLeft.getWidth()
                        + (topRight == null ? 0
                                : topRight.getWidth() - 2 * overlap);
                int bigHeight = topLeft.getHeight()
                        + (bottomLeft == null ? 0
                                : bottomLeft.getHeight() - 2 * overlap);

                result = BufferedImageHelper.createBufferedImage(
                        bigWidth, bigHeight, topLeft);

                WritableRaster raster = result.getRaster();

                int rightTilesX = tileSize - overlap
                        + (tileColumn == 0 ? 0 : overlap);
                int bottomTilesY = tileSize - overlap
                        + (tileRow == 0 ? 0 : overlap);

                raster.setRect(0, 0, topLeft.getRaster());
                if (topRight != null) {
                    raster.setRect(rightTilesX, 0, topRight.getRaster());
                }
                if (bottomLeft != null) {
                    raster.setRect(0, bottomTilesY, bottomLeft.getRaster());
                }
                if (bottomRight != null) {
                    raster.setRect(rightTilesX, bottomTilesY,
                            bottomRight.getRaster());
                }

                result = ImageResizingHelper.resizeImage(result,
                        tileDimensions.width, tileDimensions.height);
            }

            if (result != null) {
                String dir = FilenameUtils.concat(
                        imgDir, Integer.toString(level));
                String outputFile = FilenameUtils.concat(
                        dir, tileColumn + "_" + tileRow);
                writeImage(result, tileFormat, outputFile, archiver);
            }
            return result;
        }

        private class TileBuilderTask extends RecursiveTask<BufferedImage> {

            private final int level;
            private final int tileRow;
            private final int tileColumn;

            public TileBuilderTask(int level, int tileRow, int tileColumn) {
                this.level = level;
                this.tileRow = tileRow;
                this.tileColumn = tileColumn;
            }

            @Override
            protected BufferedImage compute() {
                BufferedImage result;

                if (level == nbLevels) {
                    try {
                        result = getTile(tileRow, tileColumn);
                    } catch (IOException ex) {
                        throw new RuntimeException("Cannot read tile at row "
                                + tileRow + " column " + tileColumn + ".", ex);
                    }
                } else {
                    Dimension tileDimensions = getTileDimensions(
                            level, tileRow, tileColumn);

                    if (tileDimensions.width == 0
                            || tileDimensions.height == 0) {
                        return null;
                    }

                    // The tile we are currently computing is a downsampling of
                    // 4 tiles at level + 1 (except in the corners)
                    TileBuilderTask topLeftTask = new TileBuilderTask(
                            level + 1, tileRow * 2, tileColumn * 2);
                    TileBuilderTask topRightTask = new TileBuilderTask(
                            level + 1, tileRow * 2, tileColumn * 2 + 1);
                    TileBuilderTask bottomLeftTask = new TileBuilderTask(
                            level + 1, tileRow * 2 + 1, tileColumn * 2);
                    TileBuilderTask bottomRightTask = new TileBuilderTask(
                            level + 1, tileRow * 2 + 1, tileColumn * 2 + 1);

                    topLeftTask.fork();
                    topRightTask.fork();
                    bottomLeftTask.fork();
                    BufferedImage bottomRight = bottomRightTask.compute();
                    BufferedImage topLeft = topLeftTask.join();
                    BufferedImage topRight = topRightTask.join();
                    BufferedImage bottomLeft = bottomLeftTask.join();

                    int bigWidth = topLeft.getWidth()
                            + (topRight == null ? 0
                                    : topRight.getWidth() - 2 * overlap);
                    int bigHeight = topLeft.getHeight()
                            + (bottomLeft == null ? 0
                                    : bottomLeft.getHeight() - 2 * overlap);

                    result = BufferedImageHelper.createBufferedImage(
                            bigWidth, bigHeight, topLeft);

                    WritableRaster raster = result.getRaster();

                    int rightTilesX = tileSize - overlap
                            + (tileColumn == 0 ? 0 : overlap);
                    int bottomTilesY = tileSize - overlap
                            + (tileRow == 0 ? 0 : overlap);

                    raster.setRect(0, 0, topLeft.getRaster());
                    if (topRight != null) {
                        raster.setRect(rightTilesX, 0, topRight.getRaster());
                    }
                    if (bottomLeft != null) {
                        raster.setRect(0, bottomTilesY, bottomLeft.getRaster());
                    }
                    if (bottomRight != null) {
                        raster.setRect(rightTilesX, bottomTilesY,
                                bottomRight.getRaster());
                    }

                    result = ImageResizingHelper.resizeImage(result,
                            tileDimensions.width, tileDimensions.height);
                }

                if (result != null) {
                    String dir = FilenameUtils.concat(
                            imgDir, Integer.toString(level));
                    String outputFile = FilenameUtils.concat(
                            dir, tileColumn + "_" + tileRow);
                    try {
                        writeImage(result, tileFormat, outputFile, archiver);
                    } catch (IOException ex) {
                        throw new RuntimeException("Cannot write tile at level "
                                + level + " row " + tileRow + " column "
                                + tileColumn + ".", ex);
                    }
                }
                return result;
            }

        }

        private Dimension getTileDimensions(int level, int row, int col) {
            double factor = Math.pow(2, nbLevels - level);
            int levelWidth = (int) Math.ceil(originalWidth / factor);
            int levelHeight = (int) Math.ceil(originalHeight / factor);

            int nbCols = (int) Math.ceil((double) levelWidth / tileSize);
            int nbRows = (int) Math.ceil((double) levelHeight / tileSize);
            if (col >= nbCols || row >= nbRows) {
                return new Dimension();
            }

            int x = col * tileSize - (col == 0 ? 0 : overlap);
            int y = row * tileSize - (row == 0 ? 0 : overlap);
            int w = tileSize + (col == 0 ? 1 : 2) * overlap;
            int h = tileSize + (row == 0 ? 1 : 2) * overlap;

            if (x + w > levelWidth) {
                w = levelWidth - x;
            }
            if (y + h > levelHeight) {
                h = levelHeight - y;
            }
            return new Dimension(w, h);
        }

        private BufferedImage getTile(int row, int col)
                throws IOException {
            int x = col * tileSize - (col == 0 ? 0 : overlap);
            int y = row * tileSize - (row == 0 ? 0 : overlap);
            int w = tileSize + (col == 0 ? 1 : 2) * overlap;
            int h = tileSize + (row == 0 ? 1 : 2) * overlap;

            if (x + w > originalWidth) {
                w = originalWidth - x;
            }
            if (y + h > originalHeight) {
                h = originalHeight - y;
            }

            Rectangle region = new Rectangle(x, y, w, h);
            if (region.isEmpty()) {
                return null;
            }

            return imageReader.read(region);
        }
    }

    /**
     * Write an image to the archiver.
     *
     * @param image the image to write
     * @param format the image file format to use (png, jpeg...)
     * @param fileName the file where the image must be written to without the
     * file extension
     * @param archiver the FilesArchiver to use
     * @throws IOException
     */
    private static void writeImage(final BufferedImage image,
            final String format, String fileName, FilesArchiver archiver)
            throws IOException {
        fileName = fileName + "." + format;

        boolean write = archiver.appendFile(fileName,
                new FilesArchiver.FileAppender<Boolean>() {
                    @Override
                    public Boolean append(OutputStream outputStream)
                    throws IOException {
                        return ImageIO.write(image, format, outputStream);
                    }
                });
        if (!write) {
            throw new IOException("No " + format + " image writer found.");
        }
    }

}
