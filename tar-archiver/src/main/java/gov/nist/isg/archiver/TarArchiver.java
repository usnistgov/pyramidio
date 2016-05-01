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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 *
 * @author Antoine Vandecreme
 */
public class TarArchiver implements FilesArchiver {

    private final TarArchiveOutputStream tarOutput;

    protected TarArchiver(TarArchiveOutputStream tarOutput) {
        this.tarOutput = tarOutput;
    }

    public TarArchiver(File tarFile) throws FileNotFoundException, IOException {
        this(new TarArchiveOutputStream(new FileOutputStream(tarFile)));
    }

    @Override
    public <T> T appendFile(String path, FileAppender<T> appender)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        T result = appender.append(baos);
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(baos.size());
        synchronized (this) {
            tarOutput.putArchiveEntry(entry);
            tarOutput.write(baos.toByteArray());
            tarOutput.closeArchiveEntry();
        }
        return result;
    }

    @Override
    public <T> T appendBigFile(String path, FileAppender<T> appender)
            throws IOException {
        File tempFile = File.createTempFile("tarArchiver", ".tmp");
        try {
            T result;
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                result = appender.append(fos);
            }
            appendFile(path, tempFile);
            return result;
        } finally {
            tempFile.delete();
        }
    }

    @Override
    public void appendFile(String path, File file) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(file, path);
        synchronized (this) {
            tarOutput.putArchiveEntry(entry);
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, tarOutput);
            }
            tarOutput.closeArchiveEntry();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        tarOutput.close();
    }
}
