package com.bao.fileserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileServer {

    private static final int PORT = 8089;

    public static void main(String[] args) throws IOException {
        String folder = "C:\\javaRefactor\\src\\com\\bao\\fileserve";
        if (!Files.isDirectory(Paths.get(folder))) {
            System.out.println("Invalid path or not a folder");
            System.exit(1);
        }
        FileManager fileManager = new FileManager(folder);
        HttpRequestHandler requestListener = new HttpRequestHandler(PORT);
        requestListener.onIndex(() -> fileManager.listFile());
        requestListener.onGetFile(file -> fileManager.readFile(file));
        requestListener.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server is stopping");
            requestListener.stop();
            System.out.println("Server has stopped");
        }));
    }
}