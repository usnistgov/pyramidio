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
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 *
 * @author Antoine Vandecreme
 */
public class SequenceFileArchiver implements FilesArchiver {

    private final SequenceFile.Writer writer;

    public SequenceFileArchiver(String filePath) throws IOException {
        this(new Path(filePath), new Configuration());
    }

    public SequenceFileArchiver(Path filePath, Configuration conf)
            throws IOException {
        writer = SequenceFile.createWriter(conf,
                SequenceFile.Writer.file(filePath),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(BytesWritable.class));
    }

    @Override
    public <T> T appendFile(String path, FilesArchiver.FileAppender<T> appender)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        T result = appender.append(baos);
        BytesWritable bytes = new BytesWritable(baos.toByteArray());
        writer.append(new Text(path), bytes);
        return result;
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
        writer.close();
    }
}
