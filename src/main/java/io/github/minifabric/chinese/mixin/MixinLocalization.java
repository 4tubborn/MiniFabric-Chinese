package io.github.minifabric.chinese.mixin;

import minicraft.core.io.Localization;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Mixin(value = Localization.class, remap = false)
public class MixinLocalization {

    @Shadow @Final private static HashMap<Locale, ArrayList<String>> unloadedLocalization;
    @Shadow private static Locale selectedLocale;
    @Shadow @Final public static Locale DEFAULT_LOCALE;

    @Inject(method = "loadLanguage()V", at = @At("HEAD"))
    private static void beforeLoadLanguage(CallbackInfo ci) {
        // 核心防御逻辑：
        // 无论什么时候调用 loadLanguage，只要发现当前选择的语言没在 map 里占座，
        // 我们就强行给它塞个空列表，防止后面的 for 循环报 NPE。
        if (selectedLocale != null && !unloadedLocalization.containsKey(selectedLocale)) {
            unloadedLocalization.put(selectedLocale, new ArrayList<>());
        }

        // 顺便把默认语言也保护一下，防止万一
        if (!unloadedLocalization.containsKey(DEFAULT_LOCALE)) {
            unloadedLocalization.put(DEFAULT_LOCALE, new ArrayList<>());
        }
    }
}