package com.xraz.donutauction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class DonutAuctionClient implements ClientModInitializer {
    private static KeyBinding dashboardKey;
    private static KeyBinding toggleKey;
    private static KeyBinding resetKey;

    @Override
    public void onInitializeClient() {
        AuctionStore.load();
        dashboardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.dashboard", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.donutauction"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.start_pause", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL,
                "category.donutauction"));
        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.reset", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN,
                "category.donutauction"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AuctionStore.tick();
            while (dashboardKey.wasPressed()) {
                if (client.currentScreen instanceof AuctionScreen) client.setScreen(null);
                else if (client.currentScreen == null) client.setScreen(new AuctionScreen());
            }
            while (toggleKey.wasPressed()) {
                if (client.currentScreen == null || !(client.currentScreen instanceof AuctionScreen)) {
                    AuctionStore.toggleTimer();
                }
            }
            while (resetKey.wasPressed()) AuctionStore.resetActive();
        });
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (AuctionStore.settings.overlayVisible) StreamerOverlay.render(context);
        });
    }

    public static void toast(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("[Donut Auction] " + message), true);
    }
}
