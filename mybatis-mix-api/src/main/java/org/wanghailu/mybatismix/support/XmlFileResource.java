package org.wanghailu.mybatismix.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * xml文件资源，剥离spring依赖
 * @author cdhuang
 * @date 2023/1/9
 */
public class XmlFileResource {
    
    public static final String RESOURCE_PREFIX = "file [";
    
    public static final String RESOURCE_SUFFIX = "]";
    
    public static XmlFileResource getXmlFileResource(String resource) {
        if (resource != null && resource.startsWith(RESOURCE_PREFIX) && resource.equals(RESOURCE_SUFFIX)) {
            return new XmlFileResource(
                    resource.substring(RESOURCE_PREFIX.length(), resource.length() - RESOURCE_SUFFIX.length()));
        }
        return null;
    }
    
    private String fileName;
    
    private String filePath;
    
    private File file;
    
    public XmlFileResource(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
        this.fileName = file.getName();
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public File getFile() {
        return file;
    }
    
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        XmlFileResource that = (XmlFileResource) o;
        return Objects.equals(filePath, that.filePath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }
    
    @Override
    public String toString() {
        return RESOURCE_PREFIX + filePath + RESOURCE_SUFFIX;
    }
}
