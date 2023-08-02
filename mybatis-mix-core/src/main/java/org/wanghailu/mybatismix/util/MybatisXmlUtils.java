package org.wanghailu.mybatismix.util;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.parsing.XPathParser;
import org.wanghailu.mybatismix.support.XmlFileResource;

/**
 * 加载mybatis的xml文件
 */
public class MybatisXmlUtils {
    
    
    /**
     * 获得xml文件中的namespace
     * @param resource
     * @return
     */
    public static String getNamespace(XmlFileResource resource) {
        try {
            XPathParser parser = new XPathParser(resource.getInputStream(), true, null, new XMLMapperEntityResolver());
            return parser.evalNode("/mapper").getStringAttribute("namespace");
        } catch (Exception e) {
            throw new RuntimeException("ERROR: 解析xml中namespace失败", e);
        }
    }

    


}
