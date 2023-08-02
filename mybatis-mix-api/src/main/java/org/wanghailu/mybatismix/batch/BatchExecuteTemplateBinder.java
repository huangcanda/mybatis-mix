package org.wanghailu.mybatismix.batch;

import org.wanghailu.mybatismix.exception.MybatisMixException;

/**
 * 绑定IBatchExecuteTemplate
 *
 * @author cdhuang
 * @date 2023/8/1
 */
public class BatchExecuteTemplateBinder {
    
    private static IBatchExecuteTemplate template;
    
    public static IBatchExecuteTemplate getTemplate() {
        if (template == null) {
            throw new MybatisMixException("未初始化BatchExecuteTemplate");
        }
        return template;
    }
    
    public static void setTemplate(IBatchExecuteTemplate batchExecuteTemplate) {
        BatchExecuteTemplateBinder.template = batchExecuteTemplate;
    }
}
