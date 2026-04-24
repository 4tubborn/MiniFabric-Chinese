package io.github.minifabric.chinese.mixin;

import minicraft.saveload.Load;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(remap = false, value = Load.class)
public class MixinLoad {
    @Redirect(
            method = "loadPrefs",
            at = @At(value = "INVOKE", target = "Lorg/json/JSONObject;getInt(Ljava/lang/String;)I")
    )
    private int safeGetInt(JSONObject json, String key) {
        if ("skinIdx".equals(key)) {
            return json.optInt("skinIdx", 0); // 如果没有 skinIdx，默认给 0
        }
        if ("fps".equals(key)) {
            return json.optInt("fps", 60); // fps 建议也顺手防一手
        }
        return json.getInt(key);
    }
}