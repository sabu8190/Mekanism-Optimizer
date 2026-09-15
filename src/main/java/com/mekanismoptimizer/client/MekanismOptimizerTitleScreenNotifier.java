package com.mekanismoptimizer.client;

import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import com.mekanismoptimizer.core.MekanismOptimizerUpdateNotifier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.net.URI;

/**
 * タイトル画面上に CurseForge と GitHub の両方を選んで開けるスタイリッシュな
 * Mekanism Optimizer アップデート通知バナーを描画＆クリック対応。
 */
@OnlyIn(Dist.CLIENT)
public class MekanismOptimizerTitleScreenNotifier {
    public static final String CURSEFORGE_URL = MekanismOptimizerUpdateNotifier.CURSEFORGE_PAGE_URL;
    public static final String GITHUB_URL = MekanismOptimizerUpdateNotifier.GITHUB_RELEASE_URL;

    // バナーのベース描画位置とサイズ
    private static final int BANNER_X = 10;
    private static final int BANNER_W = 340;
    private static final int BANNER_H = 24;

    private int getBannerY() {
        // FastLaunch のアップデートバナーが表示されている場合は下にずらして配置 (重なり防止)
        try {
            Class<?> flUpdateClass = Class.forName("com.fastlaunch.core.FastLaunchUpdateNotifier");
            java.lang.reflect.Method isUpdateMethod = flUpdateClass.getMethod("isUpdateAvailable");
            if (Boolean.TRUE.equals(isUpdateMethod.invoke(null))) {
                return 38; // FastLaunch (10~34) の直下に配置
            }
        } catch (Throwable ignored) {}
        return 10;
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (!MekanismOptimizerUpdateNotifier.isUpdateAvailable()) {
            return;
        }

        // タイトル画面または FancyMenu 画面でのみ描画
        if (event.getScreen() instanceof TitleScreen || event.getScreen().getClass().getName().contains("TitleScreen") || event.getScreen().getClass().getName().contains("FancyMenu")) {
            GuiGraphics graphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();
            int mouseX = event.getMouseX();
            int mouseY = event.getMouseY();

            int bannerY = getBannerY();

            int btnCfX = BANNER_X + 185;
            int btnCfY = bannerY + 4;
            int btnCfW = 70;
            int btnCfH = 16;

            int btnGhX = BANNER_X + 260;
            int btnGhY = bannerY + 4;
            int btnGhW = 70;
            int btnGhH = 16;

            boolean cfHovered = mouseX >= btnCfX && mouseX <= btnCfX + btnCfW && mouseY >= btnCfY && mouseY <= btnCfY + btnCfH;
            boolean ghHovered = mouseX >= btnGhX && mouseX <= btnGhX + btnGhW && mouseY >= btnGhY && mouseY <= btnGhY + btnGhH;

            // 背景ボックス (ダーク半透明 + メカニズムシアン枠線)
            graphics.fill(BANNER_X, bannerY, BANNER_X + BANNER_W, bannerY + BANNER_H, 0xEE0A1826);
            graphics.renderOutline(BANNER_X, bannerY, BANNER_W, BANNER_H, 0xFF00AAFF);

            // バナーテキスト
            String latestVer = MekanismOptimizerUpdateNotifier.getLatestVersion();
            String label = "⚡ Mekanism (v" + latestVer + ") 更新可能:";
            graphics.drawString(mc.font, label, BANNER_X + 8, bannerY + 8, 0xFF55FFFF, false);

            // [CurseForge] ボタン
            int cfBg = cfHovered ? 0xFFE04E22 : 0xAA802810; // CurseForge オレンジ
            int cfBorder = cfHovered ? 0xFFFFAA00 : 0xFF888888;
            int cfText = cfHovered ? 0xFFFFFFFF : 0xFFFFAA88;
            graphics.fill(btnCfX, btnCfY, btnCfX + btnCfW, btnCfY + btnCfH, cfBg);
            graphics.renderOutline(btnCfX, btnCfY, btnCfW, btnCfH, cfBorder);
            graphics.drawCenteredString(mc.font, "CurseForge", btnCfX + (btnCfW / 2), btnCfY + 4, cfText);

            // [GitHub] ボタン
            int ghBg = ghHovered ? 0xFF238636 : 0xAA10441C; // GitHub グリーン
            int ghBorder = ghHovered ? 0xFF55FF55 : 0xFF888888;
            int ghText = ghHovered ? 0xFFFFFFFF : 0xFF88FFAA;
            graphics.fill(btnGhX, btnGhY, btnGhX + btnGhW, btnGhY + btnGhH, ghBg);
            graphics.renderOutline(btnGhX, btnGhY, btnGhW, btnGhH, ghBorder);
            graphics.drawCenteredString(mc.font, "GitHub", btnGhX + (btnGhW / 2), btnGhY + 4, ghText);
        }
    }

    @SubscribeEvent
    public void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!MekanismOptimizerUpdateNotifier.isUpdateAvailable()) {
            return;
        }

        if (event.getScreen() instanceof TitleScreen || event.getScreen().getClass().getName().contains("TitleScreen") || event.getScreen().getClass().getName().contains("FancyMenu")) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            int bannerY = getBannerY();

            int btnCfX = BANNER_X + 185;
            int btnCfY = bannerY + 4;
            int btnCfW = 70;
            int btnCfH = 16;

            int btnGhX = BANNER_X + 260;
            int btnGhY = bannerY + 4;
            int btnGhW = 70;
            int btnGhH = 16;

            // CurseForge クリック判定
            if (mouseX >= btnCfX && mouseX <= btnCfX + btnCfW && mouseY >= btnCfY && mouseY <= btnCfY + btnCfH) {
                try {
                    Util.getPlatform().openUri(new URI(CURSEFORGE_URL));
                    event.setCanceled(true);
                    MekanismOptimizerLogger.info("[TitleNotifier] Opened CurseForge release page.");
                } catch (Throwable t) {
                    MekanismOptimizerLogger.error("Failed to open CurseForge URL: " + t.getMessage(), t);
                }
                return;
            }

            // GitHub クリック判定
            if (mouseX >= btnGhX && mouseX <= btnGhX + btnGhW && mouseY >= btnGhY && mouseY <= btnGhY + btnGhH) {
                try {
                    Util.getPlatform().openUri(new URI(GITHUB_URL));
                    event.setCanceled(true);
                    MekanismOptimizerLogger.info("[TitleNotifier] Opened GitHub release page.");
                } catch (Throwable t) {
                    MekanismOptimizerLogger.error("Failed to open GitHub URL: " + t.getMessage(), t);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        MekanismOptimizerUpdateNotifier.notifyPlayerOnWorldJoin();
    }
}
