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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Antoine Vandecreme
 */
public class MainTest {

    private static final String testDataFolder = "../test-data/";
    private static final String pyramidFilesFolder = "grand-canyon-landscape-overlooking_files";

    @Test
    public void testMain() throws IOException {
        Path tmpDir = Files.createTempDirectory("pyramidio-test");
        Main.main(new String[]{
            "-i",
            testDataFolder + "grand-canyon-landscape-overlooking.jpg",
            "-o",
            tmpDir.toString()
        });

        assertDirectoryEquals(
                new File(testDataFolder, pyramidFilesFolder),
                new File(tmpDir.toFile(), pyramidFilesFolder),
                "Pyramid files should be generated correctly");
    }

    private void assertDirectoryEquals(File expected, File actual,
            String message) {
        String diff = assertDirectoryEquals(expected, actual);
        if (diff != null) {
            Assert.fail(message + " (" + diff + ")");
        }
    }

    private String assertDirectoryEquals(File expected, File actual) {
        File[] files = expected.listFiles();
        for (File file : files) {
            String name = file.getName();
            File actualFile = new File(actual, name);
            if (!actualFile.exists()) {
                return "File " + actualFile + " is missing";
            }
            if (file.isFile()) {
                // A better test would be to compare the hash
                if (file.length() != actualFile.length()) {
                    return "File " + actualFile + " differs from " + file;
                }
            } else {
                String subDirDiff = assertDirectoryEquals(file, actualFile);
                if (subDirDiff != null) {
                    return subDirDiff;
                }
            }
        }

        for (File file : actual.listFiles()) {
            String name = file.getName();
            File expectedFile = new File(expected, name);
            if (!expectedFile.exists()) {
                return "Extra file " + file;
            }
        }

        return null;
    }
}
