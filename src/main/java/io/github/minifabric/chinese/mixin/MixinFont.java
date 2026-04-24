package io.github.minifabric.chinese.mixin;

import io.github.minifabric.chinese.utils.ChineseRenderQueue;
import minicraft.core.Renderer;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Font.class, remap = false)
public class MixinFont {
    @Shadow private static String chars;
    private static final String CHINESE_MAP = "简体中文国大陆橡果气巫师刷怪笼铁砧苹箭粗制的药水斧烤马铃薯床黑色衣服羊毛蓝骨头书弓面包仙人掌苗箱子阔剑布料云矿石煤炭熟鱼猪排牛苦力怕青死亡泥土地牢空桶附魔台能量逃脱爆炸耕花熔炉宝盔甲钓竿玻璃瓶金锭灯草籽绿火硬岩迅捷治疗锄洞无限坠落钥匙骑士砖皮革光明织机天然效曜门栅栏之心偶墙橙装饰小径镐木板玩家手套紫生肉红普通再玫瑰沙鳞片种碎剪刀护盾锹骷髅史莱姆蛇速度向下楼梯上线游泳时间把图腾树麦原工作黄僵尸击败…？！基准测试一个。划船执照艘我鞠躬用射出支多彩任意颜破演示使守扇哦～亮晶找到并开采它去条热恋情在背后暗抵达最底层穴与类燃烧获得脏走方看着长秀更换你肤害完全存分钟升级具伐攻物或坏块打菜单品移动关正浏览器保按取消睡觉（隐藏）当前：剩余秒选项改键位绑定语言如启了件加，可会现视问题请确认是否继续警告资源回车操成就已和未这本没有字控柄标择目退页实、交互戏拾起丢弃整组右摇杆切搜索暂停输入屏幕盘为格快创造模式除调映不详细查其他结显化扩展内务截信息说耗拥奖励终解锁统计添将所重置默返要吗需序列新载世界检旧版该数据被清主永久闭教程跳过进失滚啊描述行状态系仅接受左法您经帮助于鸣谢剧指南直链始此处建名称来设母带便以而住复命删高撤销必须先挖只放座雕像苏醒房召唤听响低振农夫础收植路备好防武富研究者同强且自难困极缩音漠森林狱平形型盒规则岛屿山悉女孩男罗披风至少从万端持";

    @Inject(method = "draw(Ljava/lang/String;Lminicraft/gfx/Screen;III)V", at = @At("HEAD"), cancellable = true)
    private static void onDraw(String msg, Screen screen, int x, int y, int whiteTint, CallbackInfo ci) {
        if (msg == null) return;

        int runningX = x;
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            int cIndex = CHINESE_MAP.indexOf(c);

            if (cIndex >= 0) {
                // 记录坐标和颜色。此时 runningX 如果是居中调用，则已经是原版计算好的偏移起点
                ChineseRenderQueue.TASKS.add(
                        new ChineseRenderQueue.Task(runningX, y, cIndex, whiteTint)
                );
            } else {
                int ix = chars.indexOf(c);
                if (ix >= 0) {
                    screen.render(runningX, y, ix % 32, ix / 32, 0,
                            Renderer.spriteLinker.getSheet(SpriteType.Gui, "font"),
                            whiteTint);
                }
            }
            // 严格步进 8，与 textWidth 保持一致
            runningX += 8;
        }
        ci.cancel();
    }

    @Inject(method = "textWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private static void onTextWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (text == null) {
            cir.setReturnValue(0);
            return;
        }
        // 既然你统一了宽度，这里必须返回 length * 8
        // 这样原版 Renderer 里的 (Screen.w - textWidth) / 2 才能算出正确的起始居中坐标
        cir.setReturnValue(text.length() * 8);
    }
}