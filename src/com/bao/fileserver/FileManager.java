package com.bao.fileserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManager {
    private String folder;

    public FileManager(String folder) {
        this.folder = folder;
    }

    public List<String> listFile() throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(folder))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            return result;
        }
    }

    public String readFile(String file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(folder + File.separator + file)));
    }
}
