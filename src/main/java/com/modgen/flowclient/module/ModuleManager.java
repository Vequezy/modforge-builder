package com.modgen.flowclient.module;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final List<String> CATEGORIES = List.of("Combat", "Render", "Misc", "Donut", "Client");
    public final List<FlowModule> modules = new ArrayList<>();
    public ModuleManager() {
        add("Combat", "Sword PVP", "Aim Assist", "Sword PVP", "AutoClicker", "TriggerBot", "Reach", "Hitbox");
        add("Combat", "CPVP", "Auto Crystal", "Auto Anchor");
        add("Combat", "Defense", "Auto Totem", "AutoDoubleHand");
        add("Render", "ESP", "Player ESP", "Mob ESP", "Item ESP", "Storage ESP", "Spawner ESP", "Block ESP", "Geode ESP", "Hole ESP", "Pearl ESP");
        add("Render", "Visual", "SwordEffects", "JumpCircles", "HitAnimation", "ChinaHat", "Breadcrumbs", "Fullbright", "NoRender", "RegionMap");
        add("Misc", "Camera", "Freecam", "Freelook");
        add("Misc", "Movement", "Auto Sprint", "Scaffold", "SafeWalk", "Fast Ladder", "ElytraFly");
        add("Misc", "Automation", "AutoArmor", "Auto Tool", "Auto Mine", "Auto Eat", "Auto Steal", "Auto Web", "AutoLog");
        add("Misc", "Utility", "Coordinates", "WeatherNotifier", "Fast Pearl", "Fast Place", "NameProtect", "SwingSpeed");
        add("Donut", "Finders", "Beluga Debug", "Stash Finder", "Netherite Finder", "SeedNethFinder", "Sus Chunk Finder", "LagChunkFinder", "Redstone ESP", "Seed Analyser");
        add("Donut", "Builders", "SchematicBuilder");
        add("Donut", "Exploits", "AntiTrap", "Shulker Dropper", "AutoSell");
        add("Donut", "Cosmetic", "Staff Detector", "OldDonutScoreboard", "Fake Roles", "Fake Stats");
        add("Client", "Interface", "Flow+", "HUD", "Target HUD", "Spotify HUD", "Spotify Lyric", "Friends & Configs");
    }
    private void add(String category, String section, String... names) {
        for (String name : names) {
            String description = switch (name) {
                case "Auto Sprint" -> "Automatically sprints while moving forward. Stops when disabled.";
                case "Fullbright" -> "Sets vanilla brightness to maximum; restores your previous value.";
                case "Coordinates" -> "Displays your current block coordinates in the in-game HUD.";
                case "HUD" -> "Displays the Flow watermark in-game.";
                case "Flow+" -> "Enables a subtle lavender glow on selected interface controls.";
                default -> "Interface preset only: saves its toggle state. No gameplay or external service integration is attached.";
            };
            modules.add(new FlowModule(name, category, section, description));
        }
    }
    public boolean enabled(String name) { return modules.stream().anyMatch(m -> m.name.equals(name) && m.enabled); }
    public long count(String category) { return modules.stream().filter(m -> m.category.equals(category)).count(); }
    public long enabledCount(String category) { return modules.stream().filter(m -> m.category.equals(category) && m.enabled).count(); }
}
