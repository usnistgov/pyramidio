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

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import gov.nist.isg.archiver.DirectoryArchiver;
import gov.nist.isg.archiver.FilesArchiver;
import gov.nist.isg.archiver.TarArchiver;
import gov.nist.isg.archiver.HdfsArchiver;
import gov.nist.isg.archiver.TarOnHdfsArchiver;

import java.util.logging.Logger;

public class FilesArchiverFactoryTest {
    private static final Logger logger = Logger.getLogger(
            FilesArchiverFactoryTest.class.getName());

    @Test
    public void testNoScheme() throws Exception {
        String filePath = "testfolder";
        File file = new File(filePath);
        file.delete();
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver(filePath);
        assertEquals(archiver.getClass(), DirectoryArchiver.class);
        file.delete();
    }

    @Test
    public void testNoTarScheme() throws Exception {
        String tarFilePath = "testfolder.tar";
        File tarFile = new File(tarFilePath);
        tarFile.delete();
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver(tarFilePath);
        assertEquals(archiver.getClass(), TarArchiver.class);
        tarFile.delete();
    }

    @Test
    public void testFileScheme() throws Exception {
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver("file:///tmp/testfolder");
        assertEquals(archiver.getClass().toString() , DirectoryArchiver.class.toString());
        assertEquals(archiver.getClass(), DirectoryArchiver.class);
    }

    @Test
    public void testFileTarScheme() throws Exception {
        String tarFilePath = "file:///tmp/testfolder.tar";
        File tarFile = new File(new URI(tarFilePath));
        boolean deleted = tarFile.delete();
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver(tarFilePath);
        assertEquals(archiver.getClass(), TarArchiver.class);
        tarFile.delete();
    }

    @Test
    @Ignore
    public void testHdfsScheme() throws Exception {
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver("hdfs://localhost:9000/testfolder");
        assertEquals(archiver.getClass(), HdfsArchiver.class);
    }

    @Test
    @Ignore
    public void testHdfsTarScheme() throws Exception {
        FilesArchiver archiver = FilesArchiverFactory.makeFilesArchiver("hdfs://localhost:9000/testfolder.tar");
        assertEquals(archiver.getClass(), TarOnHdfsArchiver.class);
    }
}
