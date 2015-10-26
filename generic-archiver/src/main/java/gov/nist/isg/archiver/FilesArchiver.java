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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Antoine Vandecreme
 */
public interface FilesArchiver extends Closeable {

    public interface FileAppender<T> {

        T append(OutputStream outputStream) throws IOException;
    }

    /**
     * Append a file to the archive
     *
     * @param <T> The type returned by the appender's append method
     * @param path The relative path of the file in the archive
     * @param appender The appender implementation
     * @return The value returned by the appender's append method
     * @throws IOException if the file can not be appended
     */
    public <T> T appendFile(String path, FileAppender<T> appender)
            throws IOException;

    /**
     * Append a file to the archive. This method consume less memory than the
     * appendFile method but is slower
     *
     * @param <T> The type returned by the appender's append method
     * @param path The relative path of the file in the archive
     * @param appender The appender implementation
     * @return The value returned by the appender's append method
     * @throws IOException if the file can not be appended
     */
    public <T> T appendBigFile(String path, FileAppender<T> appender)
            throws IOException;

    /**
     * Append a file from the standard file system.
     *
     * @param path The relative path of the file in the archive
     * @param file The file to append in the archive
     * @throws IOException if the file can not be appended
     */
    public void appendFile(String path, File file) throws IOException;
}
