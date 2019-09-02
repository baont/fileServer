package com.bao.fileserver.inter;

public interface TextFileCache {
    public void put(String fileName, String content);

    public String get(String fileName);

    public void setMaxCapacity(int maxCapacity);
}
