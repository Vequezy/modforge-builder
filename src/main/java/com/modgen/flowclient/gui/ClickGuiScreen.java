package com.modgen.flowclient.gui;

import com.modgen.flowclient.client.FlowClientClient;
import com.modgen.flowclient.module.FlowModule;
import com.modgen.flowclient.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

public final class ClickGuiScreen extends Screen {
    private final ModuleManager manager = FlowClientClient.MODULES;
    private String tab = FlowClientClient.CONFIG.category;
    private String query = "";
    private String status = "Right Shift opens Flow • Esc closes";
    private int page;
    private int left, top, side, content, right, bottom, bodyTop, rowHeight, maxPage;
    private TextFieldWidget search;
    private boolean rebuild;
    private final List<Heading> headings = new ArrayList<>();
    private record Heading(String text, int y) { }
    public ClickGuiScreen() { super(Text.literal("Flow Client")); }
    private int accent() {
        return switch (FlowClientClient.CONFIG.theme) { case 1 -> 0xFF7EBEEB; case 2 -> 0xFF8ED6B0; default -> 0xFFBC8CEA; };
    }
    @Override protected void init() { build(); }
    private void build() {
        clearChildren();
        headings.clear();
        left = Math.max(5, width / 65);
        top = Math.max(5, height / 65);
        right = width - left;
        bottom = height - top;
        side = Math.min(210, Math.max(100, (right - left) * 22 / 100));
        content = left + side + 9;
        rowHeight = FlowClientClient.CONFIG.compact ? 19 : 24;
        bodyTop = top + 61;
        int navStep = Math.max(17, Math.min(28, (bottom - top - 117) / 10));
        int navY = top + 58;
        for (String category : ModuleManager.CATEGORIES) {
            button(left + 7, navY, side - 14, navStep - 3, category + "   " + manager.count(category), () -> {
                tab = category; page = 0; FlowClientClient.CONFIG.category = category; rebuild = true;
            }, () -> tab.equals(category), false);
            navY += navStep;
        }
        headings.add(new Heading("GENERAL", navY + 5));
        navY += 19;
        for (String general : List.of("Settings", "Theme", "Configs", "Keybinds")) {
            button(left + 7, navY, side - 14, navStep - 3, general, () -> { tab = general; page = 0; rebuild = true; }, () -> tab.equals(general), false);
            navY += navStep;
        }
        int searchWidth = Math.min(220, Math.max(90, (right - content) / 2));
        search = new TextFieldWidget(textRenderer, right - searchWidth - 10, top + 10, searchWidth, 17, Text.literal("Search modules"));
        search.setMaxLength(80);
        search.setText(query);
        search.setSuggestion(query.isEmpty() ? "Search modules..." : null);
        search.setChangedListener(value -> {
            query = value; page = 0;
            search.setSuggestion(value.isEmpty() ? "Search modules..." : null);
            rebuild = true;
        });
        addDrawableChild(search);
        button(right - 31, bottom - 24, 22, 17, ">", () -> { if (page < maxPage) { page++; rebuild = true; } }, () -> false, false);
        button(right - 57, bottom - 24, 22, 17, "<", () -> { if (page > 0) { page--; rebuild = true; } }, () -> false, false);
        if (ModuleManager.CATEGORIES.contains(tab)) buildModules(); else buildGeneral();
        rebuild = false;
    }
    private void buildModules() {
        List<FlowModule> filtered = manager.modules.stream().filter(m -> m.category.equals(tab) && (m.name + " " + m.section).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).toList();
        int columns = right - content >= 265 ? 2 : 1;
        int availableRows = Math.max(1, (bottom - bodyTop - 43) / (rowHeight + 15));
        int perPage = availableRows * columns;
        maxPage = Math.max(0, (filtered.size() - 1) / perPage);
        page = Math.min(page, maxPage);
        int cellWidth = (right - content - 22 - (columns - 1) * 8) / columns;
        int y = bodyTop;
        String previous = "";
        for (int i = page * perPage; i < Math.min(filtered.size(), (page + 1) * perPage);) {
            FlowModule first = filtered.get(i);
            if (!previous.equals(first.section)) { headings.add(new Heading(first.section, y)); y += 15; previous = first.section; }
            for (int col = 0; col < columns && i < Math.min(filtered.size(), (page + 1) * perPage); col++) {
                FlowModule module = filtered.get(i);
                if (!module.section.equals(previous)) break;
                FlowButton widget = button(content + 9 + col * (cellWidth + 8), y, cellWidth, rowHeight - 3, module.name, () -> {
                    module.enabled = !module.enabled;
                    FlowClientClient.CONFIG.save("default");
                    status = module.name + (module.enabled ? " enabled" : " disabled");
                }, () -> module.enabled, true);
                widget.setTooltip(Tooltip.of(Text.literal(module.description)));
                i++;
            }
            y += rowHeight;
        }
        if (filtered.isEmpty()) headings.add(new Heading("No matching modules", bodyTop + 15));
    }
    private void buildGeneral() {
        maxPage = 0;
        int x = content + 12, y = bodyTop + 15, w = Math.max(70, right - x - 15);
        switch (tab) {
            case "Settings" -> {
                action(x, y, w, "Density: " + (FlowClientClient.CONFIG.compact ? "Compact" : "Comfortable"), () -> { FlowClientClient.CONFIG.compact = !FlowClientClient.CONFIG.compact; persist(); });
                action(x, y + 31, w, "Disable all modules", () -> { manager.modules.forEach(m -> m.enabled = false); persist(); });
                action(x, y + 62, w, "Save preferences", () -> { status = FlowClientClient.CONFIG.save("default") ? "Preferences saved" : "Save failed — see log"; });
            }
            case "Theme" -> {
                String[] themes = {"Lavender / Original", "Glacier / Blue", "Mint / Green"};
                for (int i = 0; i < themes.length; i++) { final int index = i; button(x, y + i * 31, w, 24, themes[i], () -> { FlowClientClient.CONFIG.theme = index; persist(); }, () -> FlowClientClient.CONFIG.theme == index, false); }
            }
            case "Configs" -> {
                for (int i = 0; i < 3; i++) {
                    String profile = "profile_" + (i + 1);
                    int half = (w - 8) / 2;
                    action(x, y + i * 31, half, "Save slot " + (i + 1), () -> status = FlowClientClient.CONFIG.save(profile) ? "Saved " + profile : "Save failed — see log");
                    action(x + half + 8, y + i * 31, half, "Load slot " + (i + 1), () -> { status = FlowClientClient.CONFIG.load(profile) ? "Loaded " + profile : "Slot empty or unreadable"; persist(); });
                }
            }
            case "Keybinds" -> {
                action(x, y, w, "Open Minecraft controls", () -> client.setScreen(new ControlsOptionsScreen(this, client.options)));
                headings.add(new Heading("Binding: " + FlowClientClient.openKey.getBoundKeyLocalizedText().getString(), y + 43));
                headings.add(new Heading("Controls > Key Binds > Flow Client", y + 61));
            }
            default -> { }
        }
    }
    private void persist() { FlowClientClient.CONFIG.save("default"); rebuild = true; }
    private void action(int x, int y, int w, String label, Runnable action) { button(x, y, w, 24, label, action, () -> false, false); }
    private FlowButton button(int x, int y, int w, int h, String label, Runnable action, BooleanSupplier selected, boolean toggle) {
        return addDrawableChild(new FlowButton(x, y, Math.max(12, w), Math.max(12, h), label, action, selected, this::accent, toggle));
    }
    @Override public void tick() {
        if (rebuild) {
            boolean focused = search != null && search.isFocused();
            int cursor = search == null ? 0 : search.getCursor();
            build();
            if (focused) { setFocused(search); search.setCursor(Math.min(cursor, query.length()), false); }
        }
    }
    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xAC0D0915);
        FlowButton.round(context, left - 1, top - 1, right - left + 2, bottom - top + 2, 0xFF65506F);
        FlowButton.round(context, left, top, right - left, bottom - top, 0xFF292236);
        FlowButton.round(context, left + 2, top + 2, side - 3, bottom - top - 4, 0xFF201B2D);
        context.fill(left + side, top + 9, left + side + 1, bottom - 9, 0xFF493957);
        FlowButton.round(context, content, top + 47, right - content - 6, bottom - top - 77, 0xFF4D395F);
        FlowButton.round(context, content + 1, top + 48, right - content - 8, bottom - top - 79, 0xFF201A2E);
        text(context, "FL", left + 12, top + 13, 0xFFF8F4FF);
        text(context, "Flow Client", left + 34, top + 12, 0xFFE8E0F1);
        text(context, "v1.0.0", left + 34, top + 24, 0xFF8C83A9);
        text(context, "MODULES", left + 10, top + 44, 0xFF8C83A9);
        int searchLeft = search.getX();
        String title = tab + (ModuleManager.CATEGORIES.contains(tab) ? " Modules" : "");
        text(context, textRenderer.trimToWidth(title, Math.max(20, searchLeft - content - 10)), content + 1, top + 11, 0xFFE1D6EC);
        String subtitle = ModuleManager.CATEGORIES.contains(tab) ? manager.count(tab) + " modules · " + manager.enabledCount(tab) + " enabled" : "Personalize your Flow experience";
        text(context, textRenderer.trimToWidth(subtitle, Math.max(20, right - content - 20)), content + 1, top + 31, 0xFF9584B2);
        for (Heading heading : headings) {
            if (heading.text.equals("GENERAL")) { text(context, heading.text, left + 10, heading.y, 0xFF8C83A9); continue; }
            int center = (content + right - 6) / 2;
            String label = textRenderer.trimToWidth(heading.text, Math.max(20, right - content - 30));
            int textWidth = textRenderer.getWidth(label);
            context.fill(content + 10, heading.y + 4, Math.max(content + 10, center - textWidth / 2 - 7), heading.y + 5, 0xFF493658);
            context.fill(Math.min(right - 16, center + textWidth / 2 + 7), heading.y + 4, right - 16, heading.y + 5, 0xFF493658);
            text(context, label, center - textWidth / 2, heading.y, 0xFFB49BCD);
        }
        FlowButton.round(context, left + 6, bottom - 31, side - 12, 25, 0xFF11111E);
        FlowButton.round(context, left + 12, bottom - 26, 15, 15, accent());
        text(context, "F", left + 17, bottom - 22, 0xFF21172E);
        String username = client.getSession().getUsername();
        text(context, textRenderer.trimToWidth(username, side - 45), left + 34, bottom - 24, 0xFFE2D6F2);
        text(context, "LOCAL CLIENT", left + 34, bottom - 14, 0xFF847599);
        text(context, textRenderer.trimToWidth(status, Math.max(10, right - content - 75)), content + 3, bottom - 19, 0xFF9A8AAC);
        super.render(context, mouseX, mouseY, delta);
    }
    private void text(DrawContext context, String value, int x, int y, int color) { context.drawText(textRenderer, value, x, y, color, false); }
    @Override public boolean shouldPause() { return false; }
    @Override public void close() { FlowClientClient.CONFIG.save("default"); super.close(); }
    @Override public void removed() { FlowClientClient.CONFIG.save("default"); }
}
