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
package gov.nist.isg.archiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antoine Vandecreme (Initial implementation)
 * @author Julien Amelot (Added multi-process)
 */
public class DirectoryArchiver implements FilesArchiver {

    private final File directory;

    private static final Logger logger = Logger.getLogger(
            DirectoryArchiver.class.getName());

    public DirectoryArchiver(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IOException("The path '" + directory
                        + "' is not a directory.");
            }
        } else {
            //attempts to create it
            int attempts = 0;

            while (!directory.exists()) {
                /**
                 * check if the directory still does not exists it could have
                 * been created by an other process between now and the previous
                 * check e.g. the thread processing the second image fails as
                 * the thread processing the 1st image is working on creating
                 * the directory
                 *
                 */
                attempts++;
                boolean created = directory.mkdirs();

                if (!created) {
                    //could not create the directory

                    if (attempts > 10) {
                        //we give up on trying
                        throw new IOException("Cannot create directory '"
                                + directory + "'");
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            throw new IOException("Interrupted while waiting to"
                                    + " create directory " + directory, ex);
                        }
                    }

                }
            }
            if (attempts == 0) {
                logger.log(Level.FINEST,
                        "Directory was created (but not by this thread)");
            } else {
                logger.log(Level.FINEST,
                        "Directory was created after {0} attempts", attempts);
            }

        }
        this.directory = directory;
    }

    @Override
    public <T> T appendFile(String path, FileAppender<T> appender)
            throws IOException {
        File file = new File(directory, path);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                if (!parent.exists()) {
                    throw new IOException("Cannot create directory " + parent);
                } else {
                    logger.log(Level.FINEST,
                            "Directory was created (but not by us)");
                }
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            return appender.append(fos);
        }
    }

    @Override
    public <T> T appendBigFile(String path, FileAppender<T> appender)
            throws IOException {
        return appendFile(path, appender);
    }

    @Override
    public void appendFile(String path, File file) throws IOException {
        File outputFile = new File(directory, path);
        Files.copy(file.toPath(), outputFile.toPath());
    }

    @Override
    public void close() throws IOException {
    }
}
