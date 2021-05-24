package org.example.robot.plugins;

import lombok.Getter;
import org.example.robot.MainHandleJava;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author jiyec
 * @Date 2021/5/19 10:48
 * @Version 1.0
 **/
public class MenuPlugin extends BasePluginImpl{

    private static final List<String> pluginList;
    private static final StringBuilder menuStr = new StringBuilder();

    static{
        pluginList = new LinkedList<String>(){{
            String plugins = MainHandleJava.class.getResource("plugins").getPath();
            String[] list = new File(plugins).list((dir, name) -> !name.contains("$") && !name.contains("Base") && name.endsWith("Plugin.class"));

            for (String s : list) {
                add(s.substring(0, s.indexOf("Plugin")));
            }
        }};

        AtomicInteger i = new AtomicInteger();
        menuStr.append("详细说明在下面列表名称中加第二参数“？”，中间记得加空格哟~(>_<。)比如：「菜单系统 ?」\n--------------\n");
        pluginList.forEach(p->{
            try {
                Class<? extends BasePluginImpl> clazz = (Class<? extends BasePluginImpl>) Class.forName("org.example.robot.plugins." + p + "Plugin");

                // 获取 INSTANCE
                // Field instance = clazz.getField("INSTANCE");

                // 获取 Plugin对象
                // BasePlugin plugin = (BasePlugin)instance.get(p);
                BasePlugin plugin = clazz.newInstance();

                // 增加  [指令 ---> 对象] 关联
                if(i.getAndIncrement() % 2 == 0)
                    menuStr.append("--" + plugin.getCmd() + "--");
                else
                    menuStr.append("||--" + plugin.getCmd() + "--\n");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        });
    }

    // 二级指令
    @Getter
    private final Map<String, String> subCmdList = new HashMap<String, String>(){{
        put("菜单", "getMenu");
    }};
    // 需要注册为一级指令的 指令
    @Getter
    private final Map<String, String> registerCmd = new HashMap<String, String>(){{
        put("菜单", "getMenu");
    }};

    // 本插件一级指令
    @Override
    public String getCmd() {
        return "菜单系统";
    }

    @Override
    public String getHelp() {
        return "菜单提列举了当前系统所具备的功能";
    }

    public boolean getMenu(){
        event.getSubject().sendMessage(menuStr.toString());
        return true;
    }

}
