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
package gov.nist.isg.pyramidio.cli;

import gov.nist.isg.archiver.DirectoryArchiver;
import gov.nist.isg.archiver.FilesArchiver;
import gov.nist.isg.archiver.HdfsArchiver;
import gov.nist.isg.archiver.S3Archiver;
import gov.nist.isg.archiver.SequenceFileArchiver;
import gov.nist.isg.archiver.TarArchiver;
import gov.nist.isg.archiver.TarOnHdfsArchiver;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

public class FilesArchiverFactory {

    private static final Logger logger = Logger.getLogger(
            FilesArchiverFactory.class.getName());

    private static final String S3_SCHEME = "s3";
    private static final String FILE_SCHEME = "file";
    private static final String HDFS_SCHEME = "hdfs";

    private static final String EMPTY_STRING = "";

    private static final String TAR_EXTENSION = "tar";
    private static final String SEQ_EXTENSION = "seq";

    public static FilesArchiver createFromURI(String uri)
            throws IOException {
        try {
            URI outputURI = new URI(uri);
            String scheme = outputURI.getScheme();
            logger.info("Got scheme " + scheme + " for URI " + uri);

            if (scheme == null || scheme.equalsIgnoreCase(EMPTY_STRING)) {
                return makeDirectoryArchiver(new File(uri));
            }
            if (scheme.equalsIgnoreCase(FILE_SCHEME)) {
                return makeDirectoryArchiver(new File(outputURI));
            }
            if (scheme.equalsIgnoreCase(HDFS_SCHEME)) {
                return makeHdfsArchiver(uri);
            }
            if (scheme.equalsIgnoreCase(S3_SCHEME)) {
                return makeS3Archiver(outputURI);
            }
            throw new IllegalArgumentException("Unsupported scheme " + scheme);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Unable to parse the URI " + uri, e);
        }
    }

    private static FilesArchiver makeDirectoryArchiver(File outputFile)
            throws IOException {
        String extension = FilenameUtils.getExtension(outputFile.getName());
        if (extension.equalsIgnoreCase(TAR_EXTENSION)) {
            if (outputFile.exists()) {
                throw new IOException("The path '" + outputFile
                        + "' already exists.");
            }
            logger.info("Making tar archiver for " + outputFile);
            return new TarArchiver(outputFile);
        }
        logger.info("Making directory archiver for " + outputFile);
        return new DirectoryArchiver(outputFile);
    }

    private static FilesArchiver makeHdfsArchiver(String outputFolder)
            throws IOException {
        String extension = FilenameUtils.getExtension(outputFolder);
        if (extension.equalsIgnoreCase(TAR_EXTENSION)) {
            return new TarOnHdfsArchiver(outputFolder);
        }
        if (extension.equalsIgnoreCase(SEQ_EXTENSION)) {
            return new SequenceFileArchiver(outputFolder);
        }
        return new HdfsArchiver(outputFolder);
    }

    private static FilesArchiver makeS3Archiver(URI outputURI) {
        return new S3Archiver(outputURI);
    }
}
