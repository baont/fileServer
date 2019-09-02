package com.bao.fileserver.inter;

import java.io.IOException;
import java.util.List;

/**
 * Implement this interface to return a list of file name in a folder
 */
@FunctionalInterface
public interface FileListSupplier {
    public List<String> get() throws IOException;
}
