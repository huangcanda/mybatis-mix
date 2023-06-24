package org.wanghailu.mybatismix.model;

import java.io.Serializable;

/**
 * 翻页对象
 */
@SuppressWarnings("serial")
public class Page implements Pageable, Serializable {

	/**
	 * 当前页面
	 */
	protected long currentPage = 1;
	/**
	 * 每页显示记录数
	 */
	protected long pageSize = 10;

	/**
	 * 查询结果总记录数
	 */
	protected long countSize = 0;
	
	/**
	 * 是否查询结果总记录数
	 */
	protected boolean selectCount = true;
	
	/**
	 * 分页参数将作为sql参数进行执行（预编译模式里的？占位符）
	 */
	protected boolean sqlParamMode = true;
	
	public Page() {
	}

	/**
	 * @param currentPage
	 *            当前页码
	 * @param pageSize
	 *            每页显示记录数
	 */
	public Page(int currentPage, int pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}
	
	@Override
	public long getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(long currentPage) {
		this.currentPage = currentPage;
	}
	
	@Override
	public long getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}
	
	public long getCountSize() {
		return countSize;
	}
	
	@Override
	public void setCountSize(long countSize) {
		this.countSize = countSize;
	}
	
	@Override
	public boolean isSelectCount() {
		return selectCount;
	}
	
	public void setSelectCount(boolean selectCount) {
		this.selectCount = selectCount;
	}
	
	@Override
	public boolean isSqlParamMode() {
		return sqlParamMode;
	}
	
	public void setSqlParamMode(boolean sqlParamMode) {
		this.sqlParamMode = sqlParamMode;
	}
	
	/**
	 * 总共页数
	 *
	 * @return the totalPage
	 */
	public long getTotalPage() {
		long totalPage = 0;
		totalPage = (long) Math.floor((this.countSize * 1.0d)
				/ this.pageSize);
		if (this.countSize % this.pageSize != 0) {
			totalPage++;
		}
		if (totalPage == 0) {
			return 1;
		}
		return totalPage;
	}

	
	
}
