package org.wanghailu.mybatismix.hotdeploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.support.XmlFileResource;
import org.wanghailu.mybatismix.util.MybatisUtils;
import org.wanghailu.mybatismix.util.ReflectUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * mapper.xml热部署管理器
 *
 * @author cdhuang
 * @date 2021/9/9
 */
public class MapperHotDeployManager extends BaseManager {
    
    private final Logger logger = LoggerFactory.getLogger(MapperHotDeployManager.class);
    
    protected static final ThreadLocal<Boolean> reloadContext = new ThreadLocal<>();
    
    protected Set<XmlFileResource> xmlFileResources = new HashSet<>();
    
    protected Map<String, String> pathNamespaceMap = new HashMap<>();
    
    protected Map<String, String> namespacePathMap = new HashMap<>();
    
    protected Map<String, byte[]> lastSuccessMap = new HashMap<>();
    
    private MapperHotDeployWatcher mapperHotDeployWatcher;
    
    public void addXmlFileResource(String resource) {
        XmlFileResource xmlFileResource = XmlFileResource.getXmlFileResource(resource);
        if (xmlFileResource != null && reloadContext.get() == null) {
            xmlFileResources.add(xmlFileResource);
        }
    }
    
    @Override
    public void initAfterMybatisInit() {
        if (configuration.getBoolProperty(ConfigurationKeyConstant.hotDeploy$enable, false)) {
            for (XmlFileResource xmlFileResource : xmlFileResources) {
                String namespace = MybatisUtils.getNamespace(xmlFileResource);
                pathNamespaceMap.put(xmlFileResource.toString(), namespace);
                namespacePathMap.put(namespace, xmlFileResource.toString());
            }
            mapperHotDeployWatcher = new MapperHotDeployWatcher(this);
            //noinspection AlibabaAvoidManuallyCreateThread
            Thread thread = new Thread(mapperHotDeployWatcher, "mapperHotDeployWatcher");
            thread.setDaemon(true);
            thread.start();
        }
        super.initAfterMybatisInit();
    }
    
    @Override
    public void close() {
        if (mapperHotDeployWatcher != null) {
            mapperHotDeployWatcher.close();
        }
    }
    
    /**
     * 重新加载mapper
     *
     * @param xmlPath
     * @param fileName
     * @param eventKindFlag
     */
    public void reloadMapper(String xmlPath, String fileName, int eventKindFlag) {
        try {
            XmlFileResource mapperResource = new XmlFileResource(xmlPath);
            try {
                reloadContext.set(true);
                configuration.getManager(MappedStatementManager.class).readWriteLock.writeLock().lock();
                removeMapper(mapperResource);
                if (eventKindFlag >= 0) {
                    String namespace = MybatisUtils.getNamespace(mapperResource);
                    String oldNamespacePath = namespacePathMap.get(namespace);
                    if (oldNamespacePath != null && !mapperResource.toString().equals(oldNamespacePath)) {
                        throw new RuntimeException("namespace冲突，冲突的另一xml为：" + oldNamespacePath);
                    }
                    MybatisUtils.loadMybatisXmlMapper(configuration, mapperResource.getInputStream(),
                            mapperResource.toString());
                    commit(mapperResource, namespace);
                    logger.info("已重新加载" + fileName);
                } else {
                    logger.info("移除" + fileName);
                }
            } catch (Exception e) {
                rollback(mapperResource);
                logger.error("加载" + fileName + "失败，" + e.getMessage());
            } finally {
                reloadContext.remove();
                configuration.getManager(MappedStatementManager.class).readWriteLock.writeLock().unlock();
            }
        } catch (Exception e) {
            logger.error("加载" + fileName + "失败，" + e.getMessage(), e);
        }
    }
    
    
    protected void commit(XmlFileResource mapperResource, String namespace) {
        try {
            byte[] bytes = TruckUtils.copyInputStreamToByteArray(mapperResource.getInputStream());
            lastSuccessMap.put(mapperResource.toString(), bytes);
            pathNamespaceMap.put(mapperResource.toString(), namespace);
            namespacePathMap.put(namespace, mapperResource.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
    }
    
    protected void rollback(XmlFileResource mapperResource) {
        try {
            String resource = mapperResource.toString();
            if (lastSuccessMap.containsKey(resource)) {
                MybatisUtils.loadMybatisXmlMapper(configuration, new ByteArrayInputStream(lastSuccessMap.get(resource)),
                        resource);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    protected String[] removeFields = new String[] {"mappedStatements", "caches", "resultMaps", "parameterMaps",
            "keyGenerators", "sqlFragments"};
    
    public void removeMapper(XmlFileResource mapperResource) {
        String removeNameSpace = pathNamespaceMap.get(mapperResource.toString());
        if (removeNameSpace != null) {
            logger.trace(
                    "清理Mybatis的namespace={}在mappedStatements、caches、resultMaps、parameterMaps、keyGenerators、sqlFragments中的缓存",
                    removeNameSpace);
            for (String removeField : removeFields) {
                Object value = ReflectUtils.getFieldValue(configuration, removeField);
                if (value instanceof Map) {
                    Map<String, Object> map = (Map) value;
                    List<String> list = map.keySet().stream().filter(o -> o.startsWith(removeNameSpace + "."))
                            .collect(Collectors.toList());
                    logger.trace("需要清理的元素: {}", list);
                    list.forEach(k -> map.remove(k));
                }
            }
        }
        configuration.removeLoadedResource(mapperResource.toString());
        if (removeNameSpace != null) {
            configuration.removeLoadedResource("namespace:" + removeNameSpace);
        }
    }
    
    public Set<String> getWatchPaths() {
        Set<String> pathSet = new HashSet<>();
        for (XmlFileResource mapperLocation : xmlFileResources) {
            try {
                String path = mapperLocation.getFile().getParentFile().getAbsolutePath();
                pathSet.add(path);
            } catch (Exception e) {
                logger.info("获取资源路径失败", e);
                throw new RuntimeException("获取资源路径失败");
            }
        }
        return pathSet;
    }
    
    protected int getMapperSize() {
        return pathNamespaceMap.size();
    }
}