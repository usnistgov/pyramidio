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
package gov.nist.isg.pyramidio.stitching;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author Antoine Vandecreme
 */
public class ImageTile {

    private final File file;
    private final Rectangle region;
    private final double correlation;

    public ImageTile(File file, Rectangle region, double correlation) {
        this.file = file;
        this.region = region;
        this.correlation = correlation;
    }

    public File getFile() {
        return file;
    }

    public Rectangle getRegion() {
        return region;
    }

    public double getCorrelation() {
        return correlation;
    }

    public Rectangle getIntersectionWithStitchedImageRegion(Rectangle rectangle) {
        return rectangle.intersection(region);
    }

    public BufferedImage readStitchedImageRegion(Rectangle rectangle)
            throws IOException {
        Rectangle intersection = rectangle.intersection(this.region);
        if (intersection.isEmpty()) {
            return null;
        }

        Rectangle tileRegion = new Rectangle(
                intersection.x - region.x,
                intersection.y - region.y,
                intersection.width,
                intersection.height);
        return readTileRegion(tileRegion);
    }

    /**
     * Read part of the tile in the specified region (in this tile coordinates)
     *
     * @param region
     * @return
     * @throws IOException
     */
    public BufferedImage readTileRegion(Rectangle region) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IOException("No image reader found for file " + file);
            }
            ImageReader reader = readers.next();
            reader.setInput(iis);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(region);
            return reader.read(0, param);
        }
    }

}
