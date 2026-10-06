# Ruby Flame

A Fabric mod for Minecraft 1.21.11, Java 21.

## Ruby Sword
- 8 attack damage and 1.6 attack speed.
- Diamond-tier durability and normal sword behavior.
- Fully charged attacks ignite living targets for 6 seconds. Fire-immune mobs still resist fire normally.
- Available in the Combat creative tab.

## Crafting
Use a crafting table:

```text
Redstone  Diamond  Redstone
Redstone  Diamond  Redstone
          Stick
```

Command: `/give @s ruby_flame:ruby_sword`

The ignition effect runs on the server; ordinary melee combat is preserved. No client initializer or mixins are required.
