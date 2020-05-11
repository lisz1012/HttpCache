package com.lisz.controller;

public class MyFile {

    private String name;
    private long lastModified;
    private long expireTime;

    private static MyFile file ;

    private MyFile() {

        this.name = "file...";
        this.lastModified = System.currentTimeMillis() /1000 * 1000;
        this.expireTime = 10 *1000;
    }

    public long getExpireTime() {
        return expireTime;
    }
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getLastModified() {
        return lastModified;
    }
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public static MyFile getInstance() {
        // TODO Auto-generated method stub
        if(file==null) {
            file = new MyFile();
        }
        return file;
    }

}