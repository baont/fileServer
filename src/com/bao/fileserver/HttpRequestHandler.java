package com.bao.fileserver;

import com.bao.fileserver.inter.FileListSupplier;
import com.bao.fileserver.inter.FileNameToContentFunction;
import com.bao.fileserver.inter.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpRequestHandler implements RequestHandler {

    private static final String INDEX_PATH = "index";
    private static final String GET_FILE_PATH = "get";
    private static final int THREAD_POOL_SIZE = 10;

    private HttpServer server;
    private FileListSupplier fileNameListSupplier;
    private FileNameToContentFunction nameToContentFunction;

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    public HttpRequestHandler(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        createIndexContext();
        createGetFileContext();

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

    private void createIndexContext() {
        server.createContext("/" + INDEX_PATH, exchange -> tryOrElseReturn500(exchange, () -> {
            String response = String.join(System.lineSeparator(), fileNameListSupplier.get());
            writeResponse(exchange, 200, response);
        }));
    }

    private void createGetFileContext() {
        server.createContext("/" + GET_FILE_PATH, exchange -> tryOrElseReturn500(exchange, () -> {
            String path = exchange.getRequestURI().getPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String response = "OK" + System.lineSeparator() +
                    nameToContentFunction.apply(fileName);
            writeResponse(exchange, 200, response);
        }));
    }

    private void tryOrElseReturn500(HttpExchange exchange, RunnableWithException runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            // log the exception
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String response = pw.toString();
            try {
                writeResponse(exchange, 200, response);
            } catch (IOException ioEx) {
                // nothing we can do here, just log the exception
                ioEx.printStackTrace();
            }
        }
    }

    private void writeResponse(HttpExchange exchange, int responseCode, String response) throws IOException {
        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
