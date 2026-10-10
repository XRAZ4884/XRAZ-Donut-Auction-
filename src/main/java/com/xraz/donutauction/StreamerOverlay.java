package com.xraz.donutauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class StreamerOverlay {
    private StreamerOverlay() {}
    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.currentScreen instanceof AuctionScreen) return;
        int scale = AuctionStore.settings.overlayScale;
        int x = Math.max(0, AuctionStore.settings.overlayX);
        int y = Math.max(0, AuctionStore.settings.overlayY);
        int w = AuctionStore.settings.compactOverlay ? 170 : 235;
        int h = AuctionStore.settings.compactOverlay ? 48 : 70;
        w = w * scale / 100; h = h * scale / 100;
        if (!AuctionStore.settings.transparentOverlay) ctx.fill(x, y, x + w, y + h, 0xE9161925);
        ctx.fill(x, y, x + 3, y + h, 0xFF9B6BFF);
        ctx.drawItem(new ItemStack(AuctionStore.item()), x + 8, y + 8);
        int sx = x + 32;
        ctx.drawText(mc.textRenderer, Text.literal(AuctionStore.active.displayName == null ? "Auction item" : AuctionStore.active.displayName),
                sx, y + 7, 0xFFFFFFFF, false);
        ctx.drawText(mc.textRenderer, Text.literal(AuctionStore.formatMoney(AuctionStore.active.startingPrice)),
                sx, y + 21, 0xFF67E8F9, false);
        if (!AuctionStore.settings.compactOverlay) {
            String state = AuctionStore.ended ? "AUCTION ENDED" : AuctionStore.running ? "LIVE" : AuctionStore.paused ? "PAUSED" : "READY";
            ctx.drawText(mc.textRenderer, Text.literal(state + "  •  " + AuctionStore.time()),
                    sx, y + 36, AuctionStore.ended ? 0xFFFF7777 : 0xFFCAC8D8, false);
            ctx.drawText(mc.textRenderer, Text.literal("Made by XRAZ"), sx, y + 51, 0xFF9692A8, false);
        } else {
            ctx.drawText(mc.textRenderer, Text.literal(AuctionStore.time()), sx, y + 34, 0xFFFFFFFF, false);
        }
    }
}
