package org.example.robot;

import org.example.robot.plugins.EventPlugin;
import org.example.robot.plugins.EventPluginImpl;
import org.example.robot.plugins.msg.MessagePlugin;
import org.example.robot.plugins.msg.MessagePluginImpl;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;

import java.io.File;
import java.util.*;

/**
 * @Author jiyec
 * @Date 2021/6/15 19:04
 * @Version 1.0
 **/
@Slf4j
public class PluginHandler {
    // 插件列表确保首字母大写
    private static final List<String> pluginList = new LinkedList<String>(){{
        String plugins = MainHandleJava.class.getResource("plugins/msg").getPath();
        String[] list = new File(plugins).list((dir, name) -> !name.contains("$") && !name.contains("Message") && name.endsWith("Plugin.class"));

        for (String s : list) {
            add("msg." + s.substring(0, s.indexOf("Plugin")));
        }
        plugins = MainHandleJava.class.getResource("plugins").getPath();
        list = new File(plugins).list((dir, name) -> !name.contains("$") && !name.contains("Event") && name.endsWith("Plugin.class"));

        for (String s : list) {
            add(s.substring(0, s.indexOf("Plugin")));
        }
    }};

    // 插件主指令  [指令-类]
    public static final Map<String, Class<? extends MessagePluginImpl>> cmd2plugin1 = new HashMap<>();
    // 插件的次级指令注册为主指令  [指令 - Object[]{clazz, method}]
    public static final Map<String, Object[]> cmd2plugin2 = new HashMap<>();
    // 全局调用插件 (在指令未匹配到插件时调用)   [Object[]{clazz, method}]
    public static final List<Object[]> cmd2plugin3 = new ArrayList<>();

    // 其它事件   [事件 - clazz]
    private static final Map<String, Class<? extends EventPluginImpl>> otherEvent = new HashMap<>();

    public static void register(){
        log.info("注册插件指令");

        pluginList.forEach(p->{
            log.info("注册插件：{}", p);
            try {
                Class<?> clazz = Class.forName(MainHandleJava.class.getPackage().getName() + ".plugins." + p + "Plugin");

                Class<?> superclass = clazz.getSuperclass();

                if("MessagePluginImpl".equals(superclass.getSimpleName())){
                    registerMsgPlugin((Class<? extends MessagePluginImpl>) clazz);
                }else{
                    registerEventPlugin((Class<? extends EventPluginImpl> )clazz);
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        });

        log.info("插件指令注册完毕");
    }
    private static void registerEventPlugin(Class<? extends EventPluginImpl> clazz) throws InstantiationException, IllegalAccessException {

        EventPlugin plugin = clazz.newInstance();
        otherEvent.put(plugin.event().name(), clazz);
    }
    private static void registerMsgPlugin(Class<? extends MessagePluginImpl> clazz) throws InstantiationException, IllegalAccessException, NoSuchMethodException {
        // 插件实例
        MessagePlugin plugin = clazz.newInstance();

        // 增加  [指令 ---> 对象] 关联
        cmd2plugin1.put(plugin.getMainCmd(), clazz);

        // 注册"插件中需要注册为一级指令"的指令
        Map<String, String> registerCmd = plugin.getRegisterAsFirstCmd();
        if(registerCmd != null)
            registerCmd.forEach((k, v)-> {
                try {
                    cmd2plugin2.put(k, new Object[]{clazz, clazz.getMethod(v)});
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            });

        // 注册全局指令
        List<String> globalCmd = plugin.getGlobalCmd();
        if(null != globalCmd)
            for (String s : globalCmd) {
                cmd2plugin3.add(new Object[]{clazz, clazz.getMethod(s)});
            }

    }

    public static void event(Event e){
        /*
        e.getClass().getName()       --- net.mamoe.mirai.event.events.MemberMuteEvent
        e.getClass().getSimpleName() --- MemberMuteEvent
         */
        // System.out.println(e.getClass().getSimpleName());

        try {
            String eventType = e.getClass().getSimpleName();
            Class<? extends EventPluginImpl> clazz = otherEvent.get(eventType);
            if(clazz!=null){
                EventPlugin plugin = clazz.newInstance();

                plugin.init(e);
                plugin.handle();
            }else{
                log.info("EventType: {}", eventType);
            }

        } catch (InstantiationException instantiationException) {
            instantiationException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
    }

}
