package com.modgen.ruby_flame;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class RubyFlame implements ModInitializer {
    public static final String MOD_ID = "ruby_flame";
    public static final RegistryKey<Item> RUBY_SWORD_KEY = RegistryKey.of(
            RegistryKeys.ITEM, Identifier.of(MOD_ID, "ruby_sword"));
    public static final Item RUBY_SWORD = new Item(new Item.Settings()
            .registryKey(RUBY_SWORD_KEY)
            .sword(ToolMaterial.DIAMOND, 4.0F, -2.4F));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, RUBY_SWORD_KEY, RUBY_SWORD);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(entries -> entries.add(RUBY_SWORD));

        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!world.isClient()
                    && !player.isSpectator()
                    && player.getStackInHand(hand).isOf(RUBY_SWORD)
                    && player.getAttackCooldownProgress(0.5F) >= 0.9F
                    && target instanceof LivingEntity living
                    && living.isAlive()) {
                living.setOnFireFor(6.0F);
            }
            // Preserve the ordinary sword attack, damage, durability and knockback.
            return ActionResult.PASS;
        });
    }
}
