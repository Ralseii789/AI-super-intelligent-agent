package com.sdj.aiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.model.Media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 沈德俊2022217204
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static {
        // 关闭强制注册，允许序列化任意类
        kryo.setRegistrationRequired(false);
        // 实例化策略：当对象没有无参构造时，使用 objenesis 创建实例
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        // 使用 CompatibleFieldSerializer，它按字段名和类名读写，兼容性极好
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        // 可选：关闭警告，因为未注册类会被自动处理，不再提示
        kryo.setWarnUnregisteredClasses(false);

        // 以下注册仍可保留，以提高序列化效率（有注册的类会使用紧凑ID，未注册的用类名）
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(byte[].class);
        kryo.register(UserMessage.class);
        kryo.register(AssistantMessage.class);
        kryo.register(SystemMessage.class);
        kryo.register(ToolResponseMessage.class);
        kryo.register(Media.class);
        kryo.register(Media.Builder.class);
        kryo.register(MessageType.class);
    }

    //构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir){
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }
    }
    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId,new ArrayList<>(List.of(message)));

    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        messageList.addAll(new ArrayList<>(messages));
        saveConversation(conversationId,messageList);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        return messageList.stream().
                skip(Math.max(0,messageList.size()-lastN)).
                toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 保存会话信息
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId,List<Message> messages){
        File file = getConversationFile(conversationId);
        try(Output output = new Output(new FileOutputStream(file))){
            kryo.writeObject(output,messages);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 获取或创建会话消息的列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId){
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if(file.exists()){
            try(Input input = new Input(new FileInputStream(file))){
                messages = kryo.readObject(input,ArrayList.class);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 每个会话文件单独保存（获取文件）
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId){
        return new File(BASE_DIR,conversationId+".kryo");
    }
}
