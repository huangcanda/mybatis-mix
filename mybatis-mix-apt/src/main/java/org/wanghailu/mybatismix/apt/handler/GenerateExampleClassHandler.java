package org.wanghailu.mybatismix.apt.handler;

import org.wanghailu.mybatismix.constant.SqlSymbolConstant;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.io.InputStream;
import java.util.List;

/**
 * 生成实体类对应的Example类
 * @author cdhuang
 * @date 2023/3/21
 */
public class GenerateExampleClassHandler{
    
    private String packageName;

    private String className;

    private String simpleClassName;

    private List<String> fieldList;

    public GenerateExampleClassHandler(String packageName, String className, String simpleClassName, List<String> fieldList) {
        this.packageName = packageName;
        this.className = className;
        this.simpleClassName = simpleClassName;
        this.fieldList = fieldList;
    }

    public String generateCriteriaCode(boolean isCreateMethod) {
        String noCreateMethodTemplate = "\n    public FieldCriteria<${simpleClassName}Criteria> ${fieldName} = new FieldCriteria(this,\"${fieldName}\");\n";
        String createMethodTemplate = "\n    protected FieldCriteria<${simpleClassName}Criteria> ${fieldName};\n";
        String methodTemplate = "\n" +
                "    public FieldCriteria<${simpleClassName}Criteria> ${fieldName}(){\n" +
                "        if(${fieldName} == null){\n" +
                "            ${fieldName} = new FieldCriteria(this,\"${fieldName}\");\n" +
                "        }\n" +
                "        return ${fieldName};\n" +
                "    }\n";

        StringBuilder fieldCriteriaBlock = new StringBuilder();
        StringBuilder fieldCriteriaMethodBlock = new StringBuilder();
        for (String fieldName : fieldList) {
            if(isCreateMethod){
                String str = createMethodTemplate.replace("${simpleClassName}",simpleClassName);
                str = str.replaceAll("\\$\\{fieldName\\}",fieldName);
                fieldCriteriaBlock.append(str);
                String str2 = methodTemplate.replace("${simpleClassName}",simpleClassName);
                str2 = str2.replaceAll("\\$\\{fieldName\\}",fieldName);
                fieldCriteriaMethodBlock.append(str2);
            }else{
                String str = noCreateMethodTemplate.replace("${simpleClassName}",simpleClassName);
                str = str.replaceAll("\\$\\{fieldName\\}",fieldName);
                fieldCriteriaBlock.append(str);
            }
        }
        byte[] data = TruckUtils.copyInputStreamToByteArray(this.getClass().getResourceAsStream("/apt-template/criteria.str"));
        String criteriaStr = new String(data);
        criteriaStr = criteriaStr.replace("${packageName}",packageName);
        criteriaStr = criteriaStr.replace("${className}",className);
        criteriaStr = criteriaStr.replaceAll("\\$\\{simpleClassName\\}",simpleClassName);
        criteriaStr = criteriaStr.replace("${fieldCriteriaBlock}",fieldCriteriaBlock.toString());
        criteriaStr = criteriaStr.replace("${fieldCriteriaMethodBlock}",fieldCriteriaMethodBlock.toString());
        return criteriaStr;
    }

    public String generateDeleteExampleCode() {
        InputStream inputStream = this.getClass().getResourceAsStream("/apt-template/deleteExample.str");
        String exampleStr = new String(TruckUtils.copyInputStreamToByteArray(inputStream));
        exampleStr = exampleStr.replace("${packageName}",packageName);
        exampleStr = exampleStr.replace("${className}",className);
        exampleStr = exampleStr.replaceAll("\\$\\{simpleClassName\\}",simpleClassName);
        return exampleStr;
    }

    public String generateUpdateExampleCode() {
        InputStream inputStream = this.getClass().getResourceAsStream("/apt-template/updateExample.str");
        String exampleStr = new String(TruckUtils.copyInputStreamToByteArray(inputStream));
        exampleStr = exampleStr.replace("${packageName}",packageName);
        exampleStr = exampleStr.replace("${className}",className);
        exampleStr = exampleStr.replaceAll("\\$\\{simpleClassName\\}",simpleClassName);
        return exampleStr;
    }

    public String generateQueryExampleTest() {
        String groupByTemplate = "\n" +
                "    public ${simpleClassName}QueryExample groupBy${upperFieldName}(){\n" +
                "        addGroupBy(\"${fieldName}\");\n" +
                "        return this;\n" +
                "    }\n";
        String orderByAscTemplate = "\n" +
                "    public ${simpleClassName}QueryExample orderBy${upperFieldName}Asc(){\n" +
                "        addOrderBy(\"${fieldName} asc\");\n" +
                "        return this;\n" +
                "    }\n";
        String orderByDescTemplate = "\n" +
                "    public ${simpleClassName}QueryExample orderBy${upperFieldName}Desc(){\n" +
                "        addOrderBy(\"${fieldName} desc\");\n" +
                "        return this;\n" +
                "    }\n";
        String selectTemplate = "\n" +
                "        public SelectFieldItem ${fieldName}(){\n" +
                "            addSelect(\"${fieldName}\");\n" +
                "            return this;\n" +
                "        }\n";
        String selectAsTemplate = "\n" +
                "        public SelectFieldItem ${fieldName}As(String alias){\n" +
                "            addSelect(\"${fieldName}\");\n" +
                "            return this;\n" +
                "        }\n";
        String funcTemplate = "\n" +
                "        public SelectFieldItem ${functionName}${upperFieldName}(){\n" +
                "            addSelect(\"${functionName}(${fieldName})\");\n" +
                "            return this;\n" +
                "        }\n";
        StringBuilder groupByBlock = new StringBuilder();
        StringBuilder orderByBlock = new StringBuilder();
        StringBuilder selectFieldItemBlock = new StringBuilder();
        for (String fieldName : fieldList) {
            String upperFieldName = TruckUtils.capitalize(fieldName);
            groupByBlock.append(replaceAll(groupByTemplate,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            orderByBlock.append(replaceAll(orderByAscTemplate,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            orderByBlock.append(replaceAll(orderByDescTemplate,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            selectFieldItemBlock.append(replaceAll(selectTemplate,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            selectFieldItemBlock.append(replaceAll(selectAsTemplate,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            for (String functionName : SqlSymbolConstant.COMMON_FUNCTION) {
                selectFieldItemBlock.append(replaceAll(funcTemplate,"\\$\\{functionName\\}",functionName,"\\$\\{simpleClassName\\}",simpleClassName,"\\$\\{fieldName\\}",fieldName,"\\$\\{upperFieldName\\}",upperFieldName));
            }
        }
        InputStream inputStream = this.getClass().getResourceAsStream("/apt-template/queryExample.str");
        String exampleStr = new String(TruckUtils.copyInputStreamToByteArray(inputStream));
        exampleStr = exampleStr.replace("${packageName}",packageName);
        exampleStr = exampleStr.replace("${className}",className);
        exampleStr = exampleStr.replaceAll("\\$\\{simpleClassName\\}",simpleClassName);
        exampleStr = exampleStr.replace("${groupByBlock}",groupByBlock.toString());
        exampleStr = exampleStr.replace("${orderByBlock}",orderByBlock.toString());
        exampleStr = exampleStr.replace("${selectFieldItemBlock}",selectFieldItemBlock.toString());
        return exampleStr;
    }
    private String replaceAll(String template,String... args ){
        if(args.length%2!=0){
            throw new IllegalArgumentException();
        }
        for (int index = 0; index < args.length; index = index + 2) {
            template = template.replaceAll(args[index],args[index + 1]);
        }
        return template;
    }
}
