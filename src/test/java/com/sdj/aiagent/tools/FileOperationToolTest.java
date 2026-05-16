package com.sdj.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 沈德俊2022217204
 */
class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "测试.txt";
        String s = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(s);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "测试.txt";
        String content = "这是一条工具类调用测试";
        String s = fileOperationTool.writeFile(fileName,content);
        Assertions.assertNotNull(s);
    }
}