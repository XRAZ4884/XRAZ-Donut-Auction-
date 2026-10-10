package com.xraz.donutauction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class AuctionStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/donut-auction");
    private static final Path FILE = DIR.resolve("data.json");
    private static final Path CSV = DIR.resolve("auction-history.csv");
    public static AuctionRecord active = new AuctionRecord();
    public static final List<AuctionRecord> history = new ArrayList<>();
    public static final List<AuctionRecord> presets = new ArrayList<>();
    public static final Settings settings = new Settings();
    public static long remainingMillis = 60_000;
    public static long lastTick = System.currentTimeMillis();
    public static boolean running = false;
    public static boolean ended = false;
    public static boolean paused = false;

    public static class Settings {
        public boolean overlayVisible = true;
        public boolean transparentOverlay = false;
        public boolean compactOverlay = false;
        public int overlayX = 12, overlayY = 12, overlayScale = 100;
        public boolean playEndSound = true;
    }

    private AuctionStore() {}

    public static void load() {
        try {
            Files.createDirectories(DIR);
            if (Files.exists(FILE)) {
                Data d = GSON.fromJson(Files.readString(FILE), Data.class);
                if (d != null) {
                    if (d.active != null) active = d.active;
                    if (d.history != null) history.addAll(d.history);
                    if (d.presets != null) presets.addAll(d.presets);
                    if (d.settings != null) copySettings(d.settings);
                    remainingMillis = Math.max(0, d.remainingMillis);
                    ended = d.ended;
                    paused = d.paused;
                    running = d.running;
                    // A running timer is deliberately restored paused after restart.
                    if (running) { running = false; paused = true; }
                }
            }
        } catch (Exception e) {
            System.err.println("[Donut Auction] Config load failed; using defaults: " + e.getMessage());
        }
        lastTick = System.currentTimeMillis();
        if (remainingMillis <= 0) remainingMillis = Math.max(1, active.durationSeconds) * 1000L;
    }

    private static void copySettings(Settings s) {
        settings.overlayVisible = s.overlayVisible;
        settings.transparentOverlay = s.transparentOverlay;
        settings.compactOverlay = s.compactOverlay;
        settings.overlayX = s.overlayX; settings.overlayY = s.overlayY;
        settings.overlayScale = Math.max(50, Math.min(150, s.overlayScale));
        settings.playEndSound = s.playEndSound;
    }

    public static void save() {
        try {
            Files.createDirectories(DIR);
            Data d = new Data();
            d.active = active; d.history = history; d.presets = presets; d.settings = settings;
            d.remainingMillis = remainingMillis; d.ended = ended; d.paused = paused; d.running = running;
            Files.writeString(FILE, GSON.toJson(d), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[Donut Auction] Config save failed: " + e.getMessage());
        }
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        if (running && !ended) {
            remainingMillis -= Math.max(0, now - lastTick);
            if (remainingMillis <= 0) {
                remainingMillis = 0; running = false; ended = true;
                record("COMPLETED");
                if (settings.playEndSound) {
                    MinecraftClient c = MinecraftClient.getInstance();
                    if (c.player != null) c.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.9f, 0.8f);
                }
                DonutAuctionClient.toast("AUCTION ENDED");
                save();
            }
        }
        lastTick = now;
    }

    public static void toggleTimer() {
        if (ended) return;
        if (running) { running = false; paused = true; }
        else {
            if (remainingMillis <= 0) remainingMillis = Math.max(1, active.durationSeconds) * 1000L;
            running = true; paused = false;
        }
        lastTick = System.currentTimeMillis(); save();
    }

    public static void start() {
        if (ended) resetActive();
        if (remainingMillis <= 0) remainingMillis = Math.max(1, active.durationSeconds) * 1000L;
        running = true; paused = false; lastTick = System.currentTimeMillis(); save();
    }

    public static void cancel() {
        if (running || paused) record("CANCELLED");
        running = false; paused = false; ended = false;
        remainingMillis = Math.max(1, active.durationSeconds) * 1000L;
        save();
    }

    public static void resetActive() {
        running = false; paused = false; ended = false;
        remainingMillis = Math.max(1, active.durationSeconds) * 1000L;
        lastTick = System.currentTimeMillis(); save();
    }

    public static void record(String result) {
        AuctionRecord r = new AuctionRecord(active);
        r.timestamp = System.currentTimeMillis(); r.result = result;
        history.add(0, r);
        while (history.size() > 1000) history.remove(history.size() - 1);
    }

    public static void savePreset() {
        AuctionRecord p = new AuctionRecord(active);
        p.result = "PRESET"; presets.add(0, p); save();
    }

    public static void exportCsv() {
        try {
            Files.createDirectories(DIR);
            StringBuilder b = new StringBuilder("date,item_id,display_name,starting_price,estimated_value,minimum_price,quantity,duration_seconds,status,notes\n");
            for (AuctionRecord r : history) {
                b.append(csv(Instant.ofEpochMilli(r.timestamp).toString())).append(',')
                 .append(csv(r.itemId)).append(',').append(csv(r.displayName)).append(',')
                 .append(r.startingPrice).append(',').append(r.estimatedValue).append(',')
                 .append(r.minimumPrice).append(',').append(r.quantity).append(',')
                 .append(r.durationSeconds).append(',').append(csv(r.result)).append(',')
                 .append(csv(r.notes)).append('\n');
            }
            Files.writeString(CSV, b.toString(), StandardCharsets.UTF_8);
            DonutAuctionClient.toast("Exported CSV to config/donut-auction/auction-history.csv");
        } catch (IOException e) { DonutAuctionClient.toast("CSV export failed"); }
    }

    private static String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }
    public static Item item() {
        Identifier id = Identifier.tryParse(active.itemId == null ? "minecraft:diamond" : active.itemId);
        return id != null && Registries.ITEM.containsId(id) ? Registries.ITEM.get(id) : net.minecraft.item.Items.DIAMOND;
    }
    public static String formatMoney(long n) { return "$" + String.format(Locale.US, "%,d", Math.max(0, n)); }
    public static String time() {
        long sec = Math.max(0, remainingMillis + 999) / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
    private static class Data {
        AuctionRecord active; List<AuctionRecord> history; List<AuctionRecord> presets;
        Settings settings; long remainingMillis; boolean ended; boolean paused; boolean running;
    }
}
