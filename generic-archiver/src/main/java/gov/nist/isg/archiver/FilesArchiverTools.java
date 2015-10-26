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
import java.io.IOException;

/**
 *
 * @author Antoine Vandecreme
 */
public class FilesArchiverTools {

    private FilesArchiverTools() {
    }

    /**
     * Add a whole folder from the file system to the archive.
     *
     * @param archiver the archiver in which the folder is appended
     * @param folder the folder to append in the archiver
     * @throws IOException if the folder can not be added
     */
    public static void addFolderToArchive(FilesArchiver archiver, File folder)
            throws IOException {
        addFolderToArchive(archiver, folder, "");
    }

    /**
     * Add a whole folder from the file system to the archive. All files are
     * prefixed with the basePath
     *
     * @param archiver the archiver in which the folder is appended
     * @param folder the folder to append in the archiver
     * @param basePath the base path under which the folder will be appended
     * @throws IOException if the folder can not be added
     */
    public static void addFolderToArchive(FilesArchiver archiver, File folder,
            String basePath) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Path " + folder + " is not a directory.");
        }
        File[] files = folder.listFiles();
        for (File file : files) {
            String filename = file.getName();
            String concat = new File(basePath, filename).getPath();
            if (file.isFile()) {
                archiver.appendFile(concat, file);
            } else if (file.isDirectory()) {
                addFolderToArchive(archiver, file, concat);
            }
        }
    }
}
