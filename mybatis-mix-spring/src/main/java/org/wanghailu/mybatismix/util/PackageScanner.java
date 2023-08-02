package org.wanghailu.mybatismix.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 根据包路径和注解，扫描class
 */
public class PackageScanner {

    private static Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    private static final String RESOURCE_PATTERN = "/**/*.class";

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private List<String> packagesList;

    private List<TypeFilter> includeFilters = new ArrayList<>();

    private List<TypeFilter> excludeFilters = new ArrayList<>();

    private Set<Class<?>> classSet = new HashSet<>();

    /**
     * 构造函数
     *
     * @param packagesToScan 指定哪些包需要被扫描,支持多个包"package.a,package.b"并对每个包都会递归搜索
     * @param classes        指定扫描包中含有特定注解标记或父类或接口的bean,支持多个条件(注意，这里的策略是只要有其中一个条件就算是符合条件)
     */
    public PackageScanner(String packagesToScan, Class<?>... classes) {
        packagesList = Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(packagesToScan));
        if (classes != null) {
            for (Class<?> c : classes) {
                if (Annotation.class.isAssignableFrom(c)) {
                    includeFilters.add(new AnnotationTypeFilter((Class<? extends Annotation>) c, false));
                } else {
                    includeFilters.add(new AssignableTypeFilter(c));
                }
            }
        }
    }

    /**
     * 将符合条件的Bean以Class集合的形式返回
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Set<Class<?>> getClassSet() {
        return getClassSet(true);
    }

    public Set<Class<?>> getClassSet(boolean throwException) {
        try {
            this.classSet.clear();
            if (!this.packagesList.isEmpty()) {
                for (String pkg : this.packagesList) {
                    String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                            ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
                    Resource[] resources = this.resourcePatternResolver.getResources(pattern);
                    MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
                    for (Resource resource : resources) {
                        try {
                            if (resource.isReadable()) {
                                MetadataReader reader = readerFactory.getMetadataReader(resource);
                                String className = reader.getClassMetadata().getClassName();
                                if (matchesEntityTypeFilter(reader, readerFactory)) {
                                    this.classSet.add(Class.forName(className));
                                }
                            }
                        } catch (Throwable e) {
                            if (throwException) {
                                ExceptionUtils.throwException(e);
                            } else {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (throwException) {
                ExceptionUtils.throwException(e);
            } else {
                logger.error(e.getMessage(), e);
            }
        }
        return this.classSet;
    }


    /**
     * 检查当前扫描到的Bean含有任何一个指定的注解标记
     *
     * @param reader
     * @param readerFactory
     * @return
     * @throws IOException
     */
    private boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory readerFactory) throws IOException {
        boolean include = false;
        if (!this.includeFilters.isEmpty()) {
            for (TypeFilter filter : this.includeFilters) {
                if (filter.match(reader, readerFactory)) {
                    include = true;
                    break;
                }
            }
        }
        boolean exclude = false;
        if (include) {
            if (!this.excludeFilters.isEmpty()) {
                for (TypeFilter filter : this.includeFilters) {
                    if (filter.match(reader, readerFactory)) {
                        exclude = true;
                        break;
                    }
                }
            }
        }
        return include && !exclude;
    }

    public void addIncludeFilter(TypeFilter typeFilter) {
        includeFilters.add(typeFilter);
    }

    public void addExcludeFilter(TypeFilter typeFilter) {
        excludeFilters.add(typeFilter);
    }
}
