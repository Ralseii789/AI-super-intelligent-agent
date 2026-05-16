package com.sdj.aiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 沈德俊2022217204
 */
@Slf4j
class ResourceDownloadToolTest {

    @Test
    void downResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://www.codefather.cn/logo.png";
        String fileName = "logo.png";
        String result = resourceDownloadTool.downResource(url, fileName);
        Assertions.assertNotNull(result);
        log.info(result);
    }
}