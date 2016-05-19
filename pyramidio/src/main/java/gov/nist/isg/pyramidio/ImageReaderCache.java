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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author Antoine Vandecreme
 */
class ImageReaderCache {

    private final BufferedImage cachedImage;
    private final Rectangle cachedRegion;

    ImageReaderCache(PartialImageReader imageReader, Rectangle cacheRegion)
            throws IOException {
        cachedImage = imageReader.read(cacheRegion);
        cachedRegion = cacheRegion;
    }

    BufferedImage read(Rectangle rectangle) throws IOException {
        if (!cachedRegion.contains(rectangle)) {
            throw new IOException(rectangle + " is outside of cached region "
                    + cachedRegion);
        }
        return cachedImage.getSubimage(
                rectangle.x - cachedRegion.x,
                rectangle.y - cachedRegion.y,
                rectangle.width,
                rectangle.height);
    }

}
