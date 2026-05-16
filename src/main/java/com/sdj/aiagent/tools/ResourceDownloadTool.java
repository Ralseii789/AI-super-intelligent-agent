package com.sdj.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.sdj.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * @author 沈德俊2022217204
 * 资源下载工具
 */
public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downResource(@ToolParam(description = "URL of the resource to download") String url,
                               @ToolParam(description = "Name of the file to save the download resource") String filename){
        String fileDir = FileConstant.FILE_SAVE_DIR + "/" + "download";
        String filePath = fileDir + "/" + filename;
        try{
            //创建目录
            FileUtil.mkdir(fileDir);
            //使用Hutool的downloadFile方法下载资源
            HttpUtil.downloadFile(url,new File(filePath));
            return "Resouce downloaded successfully to: "+filePath;
        }catch (Exception e){
            return "Error downloading resource: " +e.getMessage();
        }
    }
}
