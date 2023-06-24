package org.wanghailu.mybatismix.util;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.parsing.XPathParser;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.support.XmlFileResource;

import java.io.InputStream;

/**
 * 加载mybatis的xml文件
 *
 * @author cdhuang
 * @date 2019/6/6
 */
public class MybatisUtils {
    
    /**
     * 加载xml文件
     * @param configuration
     * @param inputStream
     * @param resource
     */
    public static void loadMybatisXmlMapper(MybatisMixConfiguration configuration,InputStream inputStream, String resource) {
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream,
                configuration, resource, configuration.getSqlFragments());
        xmlMapperBuilder.parse();
    }
    
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
