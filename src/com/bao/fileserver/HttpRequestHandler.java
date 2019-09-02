package com.bao.fileserver;

import com.bao.fileserver.inter.FileListSupplier;
import com.bao.fileserver.inter.FileNameToContentFunction;
import com.bao.fileserver.inter.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpRequestHandler implements RequestHandler {

    private static final String INDEX_PATH = "index";
    private static final String GET_FILE_PATH = "get";
    private static final int THREAD_POOL_SIZE = 10;

    private HttpServer server;
    private FileListSupplier fileNameListSupplier;
    private FileNameToContentFunction nameToContentFunction;

    public HttpRequestHandler(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        createServerContext();
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    }

    @Override
    public void onIndex(FileListSupplier fileNameListSupplier) {
        this.fileNameListSupplier = fileNameListSupplier;
    }

    @Override
    public void onGetFile(FileNameToContentFunction nameToContentFunction) {
        this.nameToContentFunction = nameToContentFunction;
    }

    @Override
    public void start() {
        if (fileNameListSupplier == null) {
            throw new IllegalStateException("fileNameListSupplier is not provided");
        }
        if (nameToContentFunction == null) {
            throw new IllegalStateException("nameToContentFunction is not provided");
        }
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    private void createServerContext() {
        server.createContext("/", exchange -> {
            // to remove the first "/"
            String path = exchange.getRequestURI().getPath().substring(1);
            if (INDEX_PATH.equals(path)) {
                writeListFileResponse(exchange);
            } else if (path.startsWith(GET_FILE_PATH)) {
                writeFileContentResponse(exchange);
            } else {
                tryWriteResponse(exchange, "unknown command");
            }
        });
    }

    private void writeFileContentResponse(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        String response;
        try {
            response = "ok" + System.lineSeparator() +
                    nameToContentFunction.apply(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            response = "error";
        }
        tryWriteResponse(exchange, response);
    }

    private void writeListFileResponse(HttpExchange exchange) {
        String response;
        try {
            response = String.join(System.lineSeparator(), fileNameListSupplier.get());
        } catch (IOException e) {
            e.printStackTrace();
            response = "Could not get the file list";
        }
        tryWriteResponse(exchange, response);
    }

    private void tryWriteResponse(HttpExchange exchange, String response) {
        try {
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException e) {
            // there nothing we could do here, just log the exception
            e.printStackTrace();
        }
    }

}
