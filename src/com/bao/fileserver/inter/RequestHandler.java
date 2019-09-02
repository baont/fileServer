package com.bao.fileserver.inter;

/**
 * This inter is to be implemented by classes that listen to requests from clients and return responses to them
 */
public interface RequestHandler {

    /**
     * invoked when client wants a the file list
     * @param fileNameListSpplier supplies the file name list
     */
    public void onIndex(FileListSupplier fileNameListSpplier);

    /**
     * invoked when client wants content of a particular file
     * @param nameToContentFunction function that accepts a file name then returns its content
     */
    public void onGetFile(FileNameToContentFunction nameToContentFunction);

    public void start();

    public void stop();
}
