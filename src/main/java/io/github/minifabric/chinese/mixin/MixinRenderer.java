package io.github.minifabric.chinese.mixin;

import minicraft.core.Renderer;
import io.github.minifabric.chinese.utils.ChineseRenderQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = Renderer.class, remap = false)
public class MixinRenderer {
    @Unique private static BufferedImage chineseSheet;
    @Unique private static boolean loaded = false;

    @Unique
    private static void loadSheet() {
        if (loaded) return;
        try (var is = MixinRenderer.class.getResourceAsStream("/assets/chinese/textures/characters.png")) {
            if (is != null) chineseSheet = ImageIO.read(is);
            loaded = true;
        } catch (IOException e) { e.printStackTrace(); }
    }
    // 修正后的注入位置
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    // 每一帧结束前必调用的方法
                    target = "Ljava/awt/image/BufferStrategy;show()V",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private static void onAfterScreenDraw(CallbackInfo ci) {
        // 注入点：Screen 的像素阵列刚刚被画到画布上
        // 此时画汉字，汉字会浮在世界/普通菜单上方
        renderChinese();
    }


    @Unique
    private static void renderChinese() {
        if (ChineseRenderQueue.TASKS.isEmpty()) return;
        loadSheet();
        if (chineseSheet == null) return;

        try {
            Field canvasField = Renderer.class.getDeclaredField("canvas");
            canvasField.setAccessible(true);
            Canvas canvas = (Canvas) canvasField.get(null);
            if (canvas == null) return;

            BufferStrategy bs = canvas.getBufferStrategy();
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            if (g == null) return;

            Method getWindowSize = Renderer.class.getDeclaredMethod("getWindowSize");
            getWindowSize.setAccessible(true);
            Dimension winSize = (Dimension) getWindowSize.invoke(null);

            int xOffset = (canvas.getWidth() - winSize.width) / 2 + canvas.getParent().getInsets().left;
            int yOffset = (canvas.getHeight() - winSize.height) / 2 + canvas.getParent().getInsets().top;

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            float scale = 3.0f;

            for (ChineseRenderQueue.Task task : ChineseRenderQueue.TASKS) {
                int u = (task.charIdx % 32) * 16;
                int v = (task.charIdx / 32) * 16;

                int drawX = xOffset + (int)(task.x * scale);
                int drawY = yOffset + (int)(task.y * scale);

                // 强制指定目标区域为 24x24 (逻辑 8 * 3)
                int targetSize = 24;

                if (task.whiteTint != -1) {
                    float r = ((task.whiteTint >> 16) & 0xFF) / 255.0f;
                    float g_col = ((task.whiteTint >> 8) & 0xFF) / 255.0f;
                    float b = (task.whiteTint & 0xFF) / 255.0f;

                    float[] scales = {r, g_col, b, 1.0f};
                    float[] offsets = {0, 0, 0, 0};
                    RescaleOp rop = new RescaleOp(scales, offsets, null);

                    BufferedImage sub = chineseSheet.getSubimage(u, v, 16, 16);
                    // 先滤镜染色
                    BufferedImage tinted = rop.filter(sub, null);
                    // 再强制拉伸绘制到 24x24
                    g.drawImage(tinted, drawX, drawY, targetSize, targetSize, null);
                } else {
                    // 白色情况也强制 24x24
                    g.drawImage(chineseSheet,
                            drawX, drawY, drawX + targetSize, drawY + targetSize,
                            u, v, u + 16, v + 16,
                            null);
                }
            }

            g.dispose();
            ChineseRenderQueue.TASKS.clear();
        } catch (Exception e) {
            e.printStackTrace();
            ChineseRenderQueue.TASKS.clear();
        }
    }
}