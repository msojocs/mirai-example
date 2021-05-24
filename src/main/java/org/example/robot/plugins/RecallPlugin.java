package org.example.robot.plugins;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.MessageReceipt;
import org.example.robot.data.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author jiyec
 * @Date 2021/5/19 10:48
 * @Version 1.0
 **/
@Slf4j
public class RecallPlugin extends BasePluginImpl{

    private static final Map<String, Object> pluginData = new HashMap<>();

    // 二级指令
    private static final Map<String, String> subCmdList = new HashMap<String, String>(){{
    }};

    // 需要注册为一级指令的 指令
    private static final Map<String, String> registerCmd = new HashMap<String, String>(){{
        put(Storage.name + "撤回", "doRecall");
    }};

    // 本插件一级指令
    @Override
    public String getCmd() {
        return "测试指令";
    }

    @Override
    public String getHelp() {
        return "这是测试帮助信息";
    }

    @Override
    public Map<String, String> getSubCmdList() {
        return subCmdList;
    }

    @Override
    public Map<String, String> getRegisterCmd() {
        return registerCmd;
    }

    public boolean doRecall(){
        long id = event.getSubject().getId();
        MessageReceipt message = Storage.getMessage(id);
        if(message!=null) {
            try {
                message.recall();
            }catch (Exception e){
                e.printStackTrace();
                MessageReceipt receipt = event.getSubject().sendMessage("失败，没有可撤回的消息（本消息将在5秒后自动撤回）");
                new MessageRecall(receipt, 5000).start();
            }
        }
        return true;
    }

    // 初始化插件数据[从外部到内部]
    public static void initPluginData(Map<String, Object> config){
        pluginData.putAll(config);  // 置入
    }
}
