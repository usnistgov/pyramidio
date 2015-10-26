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
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

/**
 *
 * @author Antoine Vandecreme
 */
public class SequenceFileExtractor {

    private final Configuration conf;
    private final SequenceFile.Reader reader;
    private final Text currentFile;
    private final BytesWritable currentContent;

    public SequenceFileExtractor(Path sequenceFile, Configuration conf)
            throws IOException {
        this.conf = conf;
        reader = new SequenceFile.Reader(conf,
                SequenceFile.Reader.file(sequenceFile));
        currentFile = (Text) ReflectionUtils.newInstance(
                reader.getKeyClass(), conf);
        currentContent = (BytesWritable) ReflectionUtils.newInstance(
                reader.getValueClass(), conf);
    }

    public List<String> getFilesList() throws IOException {
        reader.sync(0);
        List<String> result = new ArrayList<>();

        while (reader.next(currentFile)) {
            result.add(currentFile.toString());
        }
        return result;
    }

    public void extractAll(Path outputPath) throws IOException {
        reader.sync(0);

        while (reader.next(currentFile, currentContent)) {
            Path file = new Path(outputPath, currentFile.toString());
            FileSystem fs = file.getFileSystem(conf);
            if (fs.exists(file)) {
                throw new IOException("File " + file + " already exists.");
            }
            Path parent = file.getParent();
            if (parent != null) {
                if (!fs.exists(parent)) {
                    fs.mkdirs(parent);
                }
            }
            FSDataOutputStream stream = fs.create(file);
            try {
                stream.write(currentContent.getBytes(), 0,
                        currentContent.getLength());
            } finally {
                IOUtils.closeStream(stream);
            }
        }
    }

    public void close() throws IOException {
        reader.close();
    }
}
