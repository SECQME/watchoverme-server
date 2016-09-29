package com.secqme.util;

/**
 *
 * @author coolboykl
 */
public enum MediaFileType {
     AUDIO("3gp"),
     VIDEO("mp4"),
     PICTURE("jpg");
     
     private String fileExt;

     MediaFileType(String fileExt) {
         this.fileExt = fileExt;
     }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

     
}
