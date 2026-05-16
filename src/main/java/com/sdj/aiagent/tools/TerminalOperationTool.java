package com.sdj.aiagent.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author 沈德俊2022217204
 * 终端操作工具
 */
public class TerminalOperationTool {

    public String executeTerminalCommand(String command){
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;
                while((line = reader.readLine()) != null){
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if(exitCode != 0){
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        }catch (IOException e){
            output.append("Error executing command: ").append(e.getMessage());
        } catch (InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }

}
