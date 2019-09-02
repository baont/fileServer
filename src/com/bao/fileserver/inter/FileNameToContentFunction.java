package com.bao.fileserver.inter;

import java.io.IOException;

/**
 * Implement this interface to return the content of a text file
 */
@FunctionalInterface
public interface FileNameToContentFunction {
    public String apply(String fileName) throws IOException;
}
