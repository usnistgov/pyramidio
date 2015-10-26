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
 * Defines a way to read portions of images without loading the full image in
 * memory.
 *
 * @author Antoine Vandecreme
 */
public interface PartialImageReader {

    BufferedImage read() throws IOException;

    BufferedImage read(Rectangle rectangle) throws IOException;

    int getWidth();

    int getHeight();

}
