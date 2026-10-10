package com.xraz.donutauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AuctionScreen extends Screen {
    private TextFieldWidget search, name, startPrice, marketValue, minimum, quantity, duration, notes;
    private boolean picker = false, settingsPage = false, aboutPage = false, historyPage = false, presetPage = false;
    private List<Item> matches = new ArrayList<>();
    private int scroll = 0;
    private int panelX, panelY, panelW, panelH;

    public AuctionScreen() { super(Text.literal("Donut Auction")); }

    @Override
    protected void init() {
        clearChildren();
        panelW = Math.min(860, width - 24);
        panelH = Math.min(440, height - 24);
        panelX = (width - panelW) / 2; panelY = (height - panelH) / 2;
        int left = panelX + 18, top = panelY + 40;
        if (picker) {
            search = new TextFieldWidget(textRenderer, left, top + 8, panelW - 36, 22, Text.literal("Search items"));
            search.setPlaceholder(Text.literal("Search item name or minecraft:registry_id..."));
            search.setChangedListener(s -> { scroll = 0; updateMatches(); });
            addDrawableChild(search);
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> { picker = false; init(); }).dimensions(left, panelY + panelH - 36, 90, 22).build());
            updateMatches();
            return;
        }
        if (settingsPage) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Overlay: " + (AuctionStore.settings.overlayVisible ? "ON" : "OFF")), b -> { AuctionStore.settings.overlayVisible = !AuctionStore.settings.overlayVisible; AuctionStore.save(); init(); }).dimensions(left, top, 160, 24).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Transparent: " + (AuctionStore.settings.transparentOverlay ? "ON" : "OFF")), b -> { AuctionStore.settings.transparentOverlay = !AuctionStore.settings.transparentOverlay; AuctionStore.save(); init(); }).dimensions(left + 170, top, 170, 24).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Compact: " + (AuctionStore.settings.compactOverlay ? "ON" : "OFF")), b -> { AuctionStore.settings.compactOverlay = !AuctionStore.settings.compactOverlay; AuctionStore.save(); init(); }).dimensions(left + 350, top, 140, 24).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"), b -> { AuctionStore.settings.overlayScale = Math.max(50, AuctionStore.settings.overlayScale - 10); AuctionStore.save(); init(); }).dimensions(left, top + 38, 80, 22).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"), b -> { AuctionStore.settings.overlayScale = Math.min(150, AuctionStore.settings.overlayScale + 10); AuctionStore.save(); init(); }).dimensions(left + 88, top + 38, 80, 22).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> { settingsPage = false; init(); }).dimensions(left, panelY + panelH - 36, 90, 22).build());
            return;
        }
        if (aboutPage || historyPage || presetPage) {
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> { aboutPage = historyPage = presetPage = false; init(); }).dimensions(left, panelY + panelH - 36, 90, 22).build());
            if (historyPage) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Export CSV"), b -> AuctionStore.exportCsv()).dimensions(left + 100, panelY + panelH - 36, 100, 22).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("Clear history"), b -> { AuctionStore.history.clear(); AuctionStore.save(); init(); }).dimensions(left + 208, panelY + panelH - 36, 110, 22).build());
            }
            if (presetPage) addDrawableChild(ButtonWidget.builder(Text.literal("Save current as preset"), b -> { AuctionStore.savePreset(); init(); }).dimensions(left + 100, panelY + panelH - 36, 170, 22).build());
            return;
        }

        int colW = (panelW - 52) / 3;
        name = field(left + 2, top + 100, colW - 8, AuctionStore.active.displayName, "Display name");
        startPrice = field(left + colW + 10, top + 100, colW - 8, Long.toString(AuctionStore.active.startingPrice), "Starting price");
        marketValue = field(left + 2, top + 148, colW - 8, Long.toString(AuctionStore.active.estimatedValue), "Estimated value");
        minimum = field(left + colW + 10, top + 148, colW - 8, Long.toString(AuctionStore.active.minimumPrice), "Minimum acceptable");
        quantity = field(left + 2, top + 196, 70, Integer.toString(AuctionStore.active.quantity), "Qty");
        duration = field(left + 82, top + 196, 100, Integer.toString(AuctionStore.active.durationSeconds), "Seconds");
        notes = field(left + colW * 2 + 16, top + 100, colW - 20, AuctionStore.active.notes, "Auction notes");
        addDrawableChild(ButtonWidget.builder(Text.literal("Choose item"), b -> { syncFields(); picker = true; init(); }).dimensions(left + 2, top + 40, 120, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Start"), b -> { syncFields(); AuctionStore.start(); init(); }).dimensions(left + 2, top + 250, 72, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(AuctionStore.running ? "Pause" : "Resume"), b -> { syncFields(); AuctionStore.toggleTimer(); init(); }).dimensions(left + 80, top + 250, 82, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> { AuctionStore.cancel(); init(); }).dimensions(left + 168, top + 250, 78, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> { syncFields(); AuctionStore.resetActive(); init(); }).dimensions(left + 252, top + 250, 78, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save preset"), b -> { syncFields(); AuctionStore.savePreset(); }).dimensions(left + 340, top + 250, 100, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("History"), b -> { syncFields(); historyPage = true; init(); }).dimensions(left + panelW - 250, top + 250, 76, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Presets"), b -> { syncFields(); presetPage = true; init(); }).dimensions(left + panelW - 168, top + 250, 76, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Settings"), b -> { syncFields(); settingsPage = true; init(); }).dimensions(panelX + panelW - 100, panelY + 10, 82, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("About"), b -> { syncFields(); aboutPage = true; init(); }).dimensions(panelX + panelW - 190, panelY + 10, 80, 22).build());
    }

    private TextFieldWidget field(int x, int y, int w, String value, String hint) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, Math.max(50, w), 22, Text.literal(hint));
        f.setText(value == null ? "" : value); f.setPlaceholder(Text.literal(hint)); addDrawableChild(f); return f;
    }

    private void syncFields() {
        try {
            if (name != null) AuctionStore.active.displayName = name.getText().isBlank() ? itemName(AuctionStore.active.itemId) : name.getText().trim();
            if (startPrice != null) AuctionStore.active.startingPrice = Math.max(0, Long.parseLong(startPrice.getText().replace(",", "").replace("$", "").trim()));
            if (marketValue != null) AuctionStore.active.estimatedValue = Math.max(0, Long.parseLong(marketValue.getText().replace(",", "").replace("$", "").trim()));
            if (minimum != null) AuctionStore.active.minimumPrice = Math.max(0, Long.parseLong(minimum.getText().replace(",", "").replace("$", "").trim()));
            if (quantity != null) AuctionStore.active.quantity = Math.max(1, Math.min(999, Integer.parseInt(quantity.getText())));
            if (duration != null) AuctionStore.active.durationSeconds = Math.max(1, Math.min(86400, Integer.parseInt(duration.getText())));
            if (notes != null) AuctionStore.active.notes = notes.getText();
            if (!AuctionStore.running) AuctionStore.remainingMillis = AuctionStore.active.durationSeconds * 1000L;
            AuctionStore.save();
        } catch (NumberFormatException e) { DonutAuctionClient.toast("Check price, quantity and duration fields"); }
    }

    private void updateMatches() {
        String q = search == null ? "" : search.getText().toLowerCase(Locale.ROOT).trim();
        matches = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            String label = item.getName().getString().toLowerCase(Locale.ROOT);
            String registry = id.toString().toLowerCase(Locale.ROOT);
            if (q.isEmpty() || label.contains(q) || registry.contains(q)) matches.add(item);
        }
        matches.sort(Comparator.comparing(i -> i.getName().getString().toLowerCase(Locale.ROOT)));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0xB8000000);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF0181A27);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 3, 0xFF9B6BFF);
        ctx.drawText(textRenderer, Text.literal("DONUT AUCTION"), panelX + 18, panelY + 14, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, Text.literal("MADE BY XRAZ"), panelX + 145, panelY + 15, 0xFF67E8F9, false);
        if (picker) {
            ctx.drawText(textRenderer, Text.literal("ITEM PICKER  •  " + matches.size() + " matches"), panelX + 18, panelY + 43, 0xFFB7A4FF, true);
            int y0 = panelY + 100, cols = Math.max(1, (panelW - 36) / 44);
            for (int i = scroll * cols; i < Math.min(matches.size(), scroll * cols + cols * 5); i++) {
                int col = (i - scroll * cols) % cols, row = (i - scroll * cols) / cols;
                int x = panelX + 18 + col * 44, y = y0 + row * 50;
                ctx.fill(x, y, x + 40, y + 46, 0xFF25283A);
                ctx.drawItem(new ItemStack(matches.get(i)), x + 12, y + 4);
                if (mouseX >= x && mouseX <= x + 40 && mouseY >= y && mouseY <= y + 46) {
                    ctx.drawTooltip(textRenderer, Text.literal(matches.get(i).getName().getString() + "\n" + Registries.ITEM.getId(matches.get(i))), mouseX, mouseY);
                }
            }
        } else if (settingsPage) {
            ctx.drawText(textRenderer, Text.literal("STREAMER OVERLAY SETTINGS"), panelX + 18, panelY + 56, 0xFFB7A4FF, true);
            ctx.drawText(textRenderer, Text.literal("Position: X " + AuctionStore.settings.overlayX + " / Y " + AuctionStore.settings.overlayY + " (defaults top-left)"), panelX + 18, panelY + 125, 0xFFDAD8E8, false);
            ctx.drawText(textRenderer, Text.literal("Scale: " + AuctionStore.settings.overlayScale + "%"), panelX + 18, panelY + 145, 0xFFDAD8E8, false);
        } else if (aboutPage) {
            ctx.drawText(textRenderer, Text.literal("Donut Auction  •  Version 1.0.0"), panelX + 18, panelY + 70, 0xFFFFFFFF, true);
            ctx.drawText(textRenderer, Text.literal("A local auction timer, item picker and stream overlay for Donut SMP."), panelX + 18, panelY + 95, 0xFFDAD8E8, false);
            ctx.drawText(textRenderer, Text.literal("Made by XRAZ  •  Free to use  •  No telemetry or account required"), panelX + 18, panelY + 117, 0xFF67E8F9, false);
        } else if (historyPage || presetPage) {
            List<AuctionRecord> list = historyPage ? AuctionStore.history : AuctionStore.presets;
            ctx.drawText(textRenderer, Text.literal(historyPage ? "AUCTION HISTORY" : "SAVED PRESETS"), panelX + 18, panelY + 48, 0xFFB7A4FF, true);
            int y = panelY + 72;
            for (int i = 0; i < Math.min(12, list.size()); i++) {
                AuctionRecord r = list.get(i);
                String line = r.displayName + "  •  " + AuctionStore.formatMoney(r.startingPrice) + "  •  " + r.durationSeconds + "s  •  " + r.result;
                ctx.drawText(textRenderer, Text.literal(line), panelX + 18, y, 0xFFDAD8E8, false); y += 22;
                if (presetPage) {
                    // Presets can be loaded by clicking the row.
                    if (mouseX >= panelX + 12 && mouseX <= panelX + panelW - 12 && mouseY >= y - 22 && mouseY <= y) ctx.fill(panelX + 12, y - 22, panelX + panelW - 12, y, 0x333F4C7A);
                }
            }
            if (list.isEmpty()) ctx.drawText(textRenderer, Text.literal("Nothing saved here yet."), panelX + 18, y, 0xFF9692A8, false);
        } else {
            int left = panelX + 18, top = panelY + 40, colW = (panelW - 52) / 3;
            ctx.drawText(textRenderer, Text.literal("ACTIVE AUCTION"), left, top, 0xFFB7A4FF, true);
            ctx.drawItem(new ItemStack(AuctionStore.item()), left + 4, top + 18);
            ctx.drawText(textRenderer, Text.literal(AuctionStore.active.displayName == null ? "Diamond" : AuctionStore.active.displayName), left + 30, top + 22, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.literal(Registries.ITEM.getId(AuctionStore.item()).toString()), left + 30, top + 36, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Name"), left + 2, top + 82, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Starting price"), left + colW + 10, top + 82, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Estimated market value"), left + 2, top + 130, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Minimum acceptable"), left + colW + 10, top + 130, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Quantity"), left + 2, top + 178, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Duration (seconds)"), left + 82, top + 178, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Notes"), left + colW * 2 + 16, top + 82, 0xFF9692A8, false);
            int cx = panelX + panelW - colW - 24;
            ctx.fill(cx, top + 14, panelX + panelW - 18, top + 76, 0xFF222638);
            ctx.drawText(textRenderer, Text.literal(AuctionStore.ended ? "AUCTION ENDED" : AuctionStore.running ? "● LIVE" : AuctionStore.paused ? "PAUSED" : "READY"), cx + 12, top + 22, AuctionStore.ended ? 0xFFFF7777 : 0xFF67E8F9, true);
            ctx.drawText(textRenderer, Text.literal(AuctionStore.time()), cx + 12, top + 38, AuctionStore.remainingMillis <= 10_000 ? 0xFFFF7777 : 0xFFFFFFFF, true);
            ctx.drawText(textRenderer, Text.literal(AuctionStore.formatMoney(AuctionStore.active.startingPrice)), cx + 12, top + 56, 0xFFFFFFFF, true);
            ctx.drawText(textRenderer, Text.literal("Value difference: " + AuctionStore.formatMoney(AuctionStore.active.estimatedValue - AuctionStore.active.startingPrice)), left + 2, top + 228, 0xFF67E8F9, false);
            ctx.drawText(textRenderer, Text.literal("Prices are your estimates, not verified live server market data."), left + 2, panelY + panelH - 52, 0xFF9692A8, false);
            ctx.drawText(textRenderer, Text.literal("Right Shift: dashboard   •   Right Ctrl: start/pause   •   Controls > Key Binds to customize"), left + 2, panelY + panelH - 20, 0xFF9692A8, false);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private static String itemName(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null || !Registries.ITEM.containsId(identifier)) return "Diamond";
        return Registries.ITEM.get(identifier).getName().getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (picker && button == 0) {
            int y0 = panelY + 100, cols = Math.max(1, (panelW - 36) / 44);
            for (int i = scroll * cols; i < Math.min(matches.size(), scroll * cols + cols * 5); i++) {
                int col = (i - scroll * cols) % cols, row = (i - scroll * cols) / cols;
                int x = panelX + 18 + col * 44, y = y0 + row * 50;
                if (mouseX >= x && mouseX <= x + 40 && mouseY >= y && mouseY <= y + 46) {
                    AuctionStore.active.itemId = Registries.ITEM.getId(matches.get(i)).toString();
                    AuctionStore.active.displayName = matches.get(i).getName().getString();
                    AuctionStore.save(); picker = false; init(); return true;
                }
            }
        }
        if (presetPage && button == 0) {
            int y = panelY + 72;
            for (int i = 0; i < Math.min(12, AuctionStore.presets.size()); i++, y += 22) {
                if (mouseY >= y && mouseY <= y + 20) {
                    AuctionStore.active = new AuctionRecord(AuctionStore.presets.get(i));
                    AuctionStore.resetActive(); presetPage = false; init(); return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (picker) {
            int cols = Math.max(1, (panelW - 36) / 44);
            int maxScroll = Math.max(0, (matches.size() - 1) / cols - 4);
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int)Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public boolean shouldPause() { return false; }
}
