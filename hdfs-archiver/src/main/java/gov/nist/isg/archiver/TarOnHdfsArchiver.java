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

import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author Antoine Vandecreme
 */
public class TarOnHdfsArchiver extends TarArchiver {

    private static FileSystem getFileSystem(Path filePath, Configuration conf,
            boolean writeCheckSum)
            throws IOException {
        FileSystem fs = filePath.getFileSystem(conf);
        fs.setWriteChecksum(writeCheckSum);
        return fs;
    }

    public TarOnHdfsArchiver(String filePath) throws IOException {
        this(new Path(filePath), new Configuration());
    }

    public TarOnHdfsArchiver(Path filePath, Configuration conf)
            throws IOException {
        this(filePath, conf, false);
    }

    public TarOnHdfsArchiver(Path filePath, Configuration conf, boolean writeCheckSum)
            throws IOException {
        this(filePath, getFileSystem(filePath, conf, writeCheckSum));
    }

    public TarOnHdfsArchiver(Path filePath, FileSystem fs)
            throws IOException {
        super(new TarArchiveOutputStream(fs.create(filePath)));
    }
}
