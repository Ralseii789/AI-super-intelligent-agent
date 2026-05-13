package com.sdj.aiagent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 沈德俊2022217204
 * 通用的删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serivalVersionUID = 1L;
}
