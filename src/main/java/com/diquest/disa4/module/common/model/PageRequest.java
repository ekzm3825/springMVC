package com.diquest.disa4.module.common.model;

import java.io.Serializable;

/**
 * 페이지네이션
 */
public class PageRequest implements Serializable {

    protected Integer pageNo;

    protected Integer pageSize;

    protected Integer limit;

    protected Integer offset;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getLimit() {
        return pageSize;
    }

    public Integer getOffset() {
        return pageNo == null || pageSize == null ? null : (pageNo - 1) * pageSize;
    }

}
