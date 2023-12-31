package org.wanghailu.mybatismix.page;


import org.wanghailu.mybatismix.model.Pageable;

/**
 * Mybatis分页帮助类
 */
public class PageHelper {
	
	private static final ThreadLocal<Pageable> pageThreadLocal = new ThreadLocal<>();
	
	/**
	 * 获取 Page 参数
	 * @return
	 */
	public static Pageable getLocalPage() {
		return pageThreadLocal.get();
	}

	/**
	 * 设置 Page 参数
	 * @param page
	 */
	public static void setLocalPage(Pageable page){
		pageThreadLocal.set(page);
	}

	/**
	 * 移除本地变量
	 */
	public static void clearPage(){
		pageThreadLocal.remove();
	}

}