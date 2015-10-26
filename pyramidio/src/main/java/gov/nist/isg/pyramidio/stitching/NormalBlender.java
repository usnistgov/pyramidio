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

import gov.nist.isg.pyramidio.tools.BufferedImageHelper;
import java.awt.image.BufferedImage;

/**
 *
 * @author Antoine Vandecreme
 */
public class NormalBlender implements Blender {

    private final BufferedImage result;

    public NormalBlender(int width, int height, BufferedImage sampleImage) {
        result = BufferedImageHelper.createBufferedImage(
                width, height, sampleImage);
    }

    @Override
    public void blend(BufferedImage image, int x, int y) {
        result.getRaster().setRect(x, y, image.getRaster());
    }

    @Override
    public BufferedImage getResult() {
        return result;
    }

}
