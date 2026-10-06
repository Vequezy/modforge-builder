package com.modgen.flowclient.client;

import com.modgen.flowclient.config.FlowConfig;
import com.modgen.flowclient.gui.ClickGuiScreen;
import com.modgen.flowclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FlowClientClient implements ClientModInitializer {
    public static final ModuleManager MODULES = new ModuleManager();
    public static final FlowConfig CONFIG = new FlowConfig(MODULES);
    public static KeyBinding openKey;
    private boolean held;
    private Double previousGamma;
    private boolean sprintOwned;
    @Override public void onInitializeClient() {
        CONFIG.load("default");
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("flowclient", "controls"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.flowclient.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.addLast(Identifier.of("flowclient", "status"), (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden || client.currentScreen instanceof ClickGuiScreen) return;
            int y = 8;
            if (MODULES.enabled("HUD")) {
                context.fill(5, 5, 170, 24, 0xCF171323);
                context.fill(5, 5, 7, 24, 0xFFD1ADFF);
                context.drawText(client.textRenderer, "FLOW  /  Client 1.0.0", 12, y, 0xFFE6D7FF, false);
                y += 22;
            }
            if (MODULES.enabled("Coordinates")) {
                var pos = client.player.getBlockPos();
                context.drawText(client.textRenderer, "XYZ  " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ(), 10, y, 0xFFE6D7FF, true);
            }
        });
    }
    private void tick(MinecraftClient client) {
        // Edge detection works even when a screen owns keyboard input.
        InputUtil.Key key = InputUtil.fromTranslationKey(openKey.getBoundKeyTranslationKey());
        boolean down = false;
        long window = client.getWindow().getHandle();
        if (key.getCategory() == InputUtil.Type.KEYSYM && key.getCode() >= 0)
            down = GLFW.glfwGetKey(window, key.getCode()) == GLFW.GLFW_PRESS;
        else if (key.getCategory() == InputUtil.Type.MOUSE && key.getCode() >= 0)
            down = GLFW.glfwGetMouseButton(window, key.getCode()) == GLFW.GLFW_PRESS;
        if (down && !held && client.isWindowFocused()) {
            if (client.currentScreen instanceof ClickGuiScreen screen) screen.close();
            else if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
        }
        held = down;
        while (openKey.wasPressed()) { }
        if (MODULES.enabled("Fullbright")) {
            if (previousGamma == null) previousGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(1.0);
        } else if (previousGamma != null) {
            client.options.getGamma().setValue(previousGamma);
            previousGamma = null;
        }
        if (client.player != null) {
            if (MODULES.enabled("Auto Sprint") && client.currentScreen == null && client.options.forwardKey.isPressed() && !client.player.isSneaking() && client.player.getHungerManager().getFoodLevel() > 6) {
                client.player.setSprinting(true);
                sprintOwned = true;
            } else if (sprintOwned) {
                client.player.setSprinting(false);
                sprintOwned = false;
            }
        } else sprintOwned = false;
    }
}
