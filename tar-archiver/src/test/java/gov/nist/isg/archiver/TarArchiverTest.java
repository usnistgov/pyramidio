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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author antoinev
 */
public class TarArchiverTest {

    /**
     * Test of appendFile method, of class TarArchiver.
     */
    @Test
    public void testAppendFile() throws Exception {
        String fileName = "hello";
        final byte[] content = "Hello".getBytes();

        File tempFile = File.createTempFile("tarArchiverTest", ".tar");
        try {
            try (TarArchiver instance = new TarArchiver(tempFile)) {
                Boolean result = instance.appendFile(fileName,
                        new FilesArchiver.FileAppender<Boolean>() {
                            @Override
                            public Boolean append(OutputStream outputStream)
                            throws IOException {
                                outputStream.write(content);
                                return true;
                            }
                        });
                assertTrue(result);
            }
            try (TarArchiveInputStream in = new TarArchiveInputStream(
                    new FileInputStream(tempFile))) {
                ArchiveEntry nextEntry = in.getNextEntry();
                String name = nextEntry.getName();
                assertEquals(fileName, name);
                byte[] buff = new byte[content.length];
                in.read(buff, 0, content.length);
                assertArrayEquals(content, buff);
            }
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Test of appendBigFile method, of class TarArchiver.
     */
    @Test
    public void testAppendBigFile() throws Exception {
        String fileName = "hello";
        final byte[] content = "Hello".getBytes();

        File tempFile = File.createTempFile("tarArchiverTest", ".tar");
        try {
            try (TarArchiver instance = new TarArchiver(tempFile)) {
                Boolean result = instance.appendBigFile(fileName,
                        new FilesArchiver.FileAppender<Boolean>() {
                            @Override
                            public Boolean append(OutputStream outputStream)
                            throws IOException {
                                outputStream.write(content);
                                return true;
                            }
                        });
                assertTrue(result);
            }
            try (TarArchiveInputStream in = new TarArchiveInputStream(
                    new FileInputStream(tempFile))) {
                ArchiveEntry nextEntry = in.getNextEntry();
                String name = nextEntry.getName();
                assertEquals(fileName, name);
                byte[] buff = new byte[content.length];
                in.read(buff, 0, content.length);
                assertArrayEquals(content, buff);
            }
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testAppendFileSystem() throws Exception {
        File inFile = File.createTempFile("tarArchiverTest", ".txt");
        try {
            final byte[] content = "Hello".getBytes();
            try (FileOutputStream out = new FileOutputStream(inFile)) {
                out.write(content);
            }

            File tempFile = File.createTempFile("tarArchiverTest", ".tar");
            try {
                try (TarArchiver instance = new TarArchiver(tempFile)) {
                    instance.appendFile(inFile.getName(), inFile);
                }
                try (TarArchiveInputStream in = new TarArchiveInputStream(
                        new FileInputStream(tempFile))) {
                    ArchiveEntry nextEntry = in.getNextEntry();
                    String name = nextEntry.getName();
                    assertEquals(inFile.getName(), name);
                    byte[] buff = new byte[content.length];
                    in.read(buff, 0, content.length);
                    assertArrayEquals(content, buff);
                }

            } finally {
                tempFile.delete();
            }
        } finally {
            inFile.delete();
        }
    }
}
