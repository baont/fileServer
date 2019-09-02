package com.bao.fileserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * to make this thread-safe
 */
public class FileManager {
    private String folder;
    // cache file list and content of each file to reduce disk access
    private List<String> fileList;
    private ConcurrentMap<String, String> contentPerFile;

    public FileManager(String folder) {
        this.folder = folder;
        contentPerFile = new ConcurrentHashMap<>();
    }

    public List<String> listFile() throws IOException {
        if (fileList == null) {
            File folder = new File(this.folder);
            File[] listOfFiles = folder.listFiles();

            fileList = Arrays.asList(listOfFiles).stream()
                    .filter(file -> file.isFile())
                    .map(file -> file.getName()).collect(Collectors.toList());

        }
        return fileList;
    }

    public String readFile(String file) throws IOException {
        // we could not use computeIfAbsent here because the lambda expression does not has
        // IOException in its signature
        if (!contentPerFile.containsKey(file)) {
            contentPerFile.put(file,
                    new String(Files.readAllBytes(Paths.get(folder + File.separator + file))));
        }
        return contentPerFile.get(file);
    }
}
