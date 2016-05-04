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
import gov.nist.isg.archiver.TarArchiver;
import gov.nist.isg.archiver.TarOnHdfsArchiver;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class FilesArchiverFactoryTest {

    @Test
    public void testNoScheme() throws IOException {
        String filePath = "testfolder";
        File file = new File(filePath);
        file.delete();
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(filePath);
        assertEquals("No scheme should create a DirectoryArchiver",
                archiver.getClass(), DirectoryArchiver.class);
        file.delete();
    }

    @Test
    public void testNoTarScheme() throws IOException {
        String tarFilePath = "testfolder.tar";
        File tarFile = new File(tarFilePath);
        tarFile.delete();
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                tarFilePath);
        assertEquals("No scheme with tar extension should create a TarArchiver",
                archiver.getClass(), TarArchiver.class);
        tarFile.delete();
    }

    @Test
    public void testFileScheme() throws IOException {
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                "file:///tmp/testfolder");
        assertEquals("file:// scheme should create a DirectoryArchiver",
                archiver.getClass(), DirectoryArchiver.class);
    }

    @Test
    public void testFileTarScheme() throws IOException, URISyntaxException {
        String tarFilePath = "file:///tmp/testfolder.tar";
        File tarFile = new File(new URI(tarFilePath));
        tarFile.delete();
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                tarFilePath);
        assertEquals("file:// scheme with tar extension should create a "
                + "TarArchiver", archiver.getClass(), TarArchiver.class);
        tarFile.delete();
    }

    @Test
    @Ignore
    public void testHdfsScheme() throws IOException {
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                "hdfs://localhost:9000/testfolder");
        assertEquals("hdfs:// scheme should create a HdfsArchiver",
                archiver.getClass(), HdfsArchiver.class);
    }

    @Test
    @Ignore
    public void testHdfsTarScheme() throws IOException {
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                "hdfs://localhost:9000/testfolder.tar");
        assertEquals("hdfs:// scheme with tar extension should create a "
                + "TarOnHdfsArchiver",
                archiver.getClass(), TarOnHdfsArchiver.class);
    }

    @Test
    @Ignore
    public void testS3Scheme() throws IOException {
        FilesArchiver archiver = FilesArchiverFactory.createFromURI(
                "s3://bucket/file");
        assertEquals("s3:// scheme should create a S3Archiver",
                archiver.getClass(), S3Archiver.class);
    }
}
