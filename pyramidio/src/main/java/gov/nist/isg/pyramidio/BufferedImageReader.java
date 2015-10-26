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
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Antoine Vandecreme
 */
public class BufferedImageReader implements PartialImageReader {

    private final BufferedImage image;

    public BufferedImageReader(BufferedImage image) {
        this.image = image;
    }

    public BufferedImageReader(File imageFile) throws IOException {
        image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("No codec found for image " + imageFile);
        }
    }

    @Override
    public BufferedImage read() throws IOException {
        return image;
    }

    @Override
    public BufferedImage read(Rectangle rectangle) throws IOException {
        return image.getSubimage(rectangle.x, rectangle.y,
                rectangle.width, rectangle.height);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

}
