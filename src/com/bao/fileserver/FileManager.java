package com.bao.fileserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Handle listing and getting the content of files within a folder
 * use caches to reduce disk access
 * This class is thread-safe
 */
public class FileManager {
    private String folder;
    // cache file list and content of each file to reduce disk access
    private volatile List<String> fileList;
    private ConcurrentMap<String, String> contentPerFile;

    public FileManager(String folder) {
        this.folder = folder;
        contentPerFile = new ConcurrentHashMap<>();
    }

    public void startWatch() throws IOException {
        WatchService watchService
                = FileSystems.getDefault().newWatchService();

        Path path = Paths.get(folder);

        path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        new Thread(() -> {
            WatchKey key = null;
            do {
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    // nothing to do here, just log the exception
                    e.printStackTrace();
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    // something in this folder has changed, reset the cache
                    fileList = null;
                    contentPerFile.clear();
                }
            } while (key.reset());
        }).start();
    }

    public List<String> listFile() throws IOException {
        if (fileList != null) {
            return fileList;
        }
        synchronized (this) {
            if (fileList == null) {
                File folder = new File(this.folder);
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles == null) {
                    throw new IOException("Could not get the list of file for folder " + this.folder);
                }
                fileList = Arrays.stream(listOfFiles)
                        .filter(File::isFile)
                        .map(File::getName).collect(Collectors.toList());

            }
            return fileList;
        }
    }

    public String readFile(String file) throws IOException {
        // no external synchronize is needed because of the nature of ConcurrentMap
        contentPerFile.putIfAbsent(file,
                new String(Files.readAllBytes(Paths.get(folder + File.separator + file))));
        return contentPerFile.get(file);
    }


}
