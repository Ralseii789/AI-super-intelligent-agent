package com.sdj.aiagent.common;

import lombok.Data;

/**
 * @author 沈德俊2022217204
 * 通用的分页请求类
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序(默认升序)
     */
    private String sortOrder = "desceng";
}
