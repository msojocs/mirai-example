package net.mamoe.exampleKotlin.eventHandle

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.NudgeEvent

/**
 * @Author jiyec
 * @Date  2021/5/4 13:49
 * @Version 1.0
 **/
object CommonEvent {
    @JvmStatic
    fun main(bot: Bot){
        nudge(bot);
    }


    /**
     * 戳一戳
     * 触发条件：
     * 在好友对话界面或QQ群聊天界面双击机器人头像
     *
     * 出发效果：
     * 发起者被机器人“戳了一下”
     *
     */
    @JvmStatic
    fun nudge(bot: Bot){
        bot.eventChannel.subscribeAlways<NudgeEvent> {
            if(target == bot)
                from.nudge().sendTo(subject)
        }
    }
}