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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author Antoine Vandecreme
 */
public class HdfsArchiver implements FilesArchiver {

    private final FileSystem fs;
    private final Path directory;

    public HdfsArchiver(String directory) throws IOException {
        this(new Path(directory), new Configuration());
    }

    public HdfsArchiver(Path directory, Configuration conf)
            throws IOException {
        this(directory, directory.getFileSystem(conf));
    }

    public HdfsArchiver(Path directory, FileSystem fs)
            throws IOException {
        if (fs.exists(directory)) {
            if (!fs.isDirectory(directory)) {
                throw new IOException("The path " + directory + " is not a directory.");
            }
        } else {
            boolean created = fs.mkdirs(directory);
            if (!created) {
                throw new IOException("Cannot create directory " + directory + ".");
            }
        }
        this.directory = directory;
        this.fs = fs;
    }

    @Override
    public <T> T appendFile(String path, FileAppender<T> appender)
            throws IOException {
        Path file = new Path(directory, path);
        Path parent = file.getParent();
        if (!fs.exists(parent)) {
            boolean created = fs.mkdirs(parent);
            if (!created) {
                throw new IOException("Cannot create directory " + parent);
            }
        }
        try (FSDataOutputStream stream = fs.create(file)) {
            return appender.append(stream);
        }
    }

    @Override
    public <T> T appendBigFile(String path, FileAppender<T> appender)
            throws IOException {
        return appendFile(path, appender);
    }

    @Override
    public void appendFile(String path, File file) throws IOException {
        try (final FileInputStream fis = new FileInputStream(file)) {
            appendFile(path, new FileAppender<Void>() {
                @Override
                public Void append(OutputStream outputStream) throws IOException {
                    IOUtils.copy(fis, outputStream);
                    return null;
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}
