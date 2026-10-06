package com.modgen.orbit_client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import static com.modgen.orbit_client.Settings.Option.*;

public final class OrbitClient implements ClientModInitializer {
    private record Storage(BlockPos pos,String name,int color) {}
    private static final List<Storage> storages=new ArrayList<>();
    private static boolean menuDown;
    private static int ticks;
    private static Object lastWorld;
    private static String target="";
    private static Integer originalFov;
    private static Vec3d previousPosition;
    private static double speed;
    private static final int ACCENT=0xFFFF8BC7, TEXT=0xFFE5E9F5, MUTED=0xFF98A6C0;

    @Override public void onInitializeClient() {
        Settings.load();
        ClientTickEvents.END_CLIENT_TICK.register(OrbitClient::tick);
        HudRenderCallback.EVENT.register((context,counter)->hud(context));
    }
    private static boolean key(MinecraftClient c,int code) {
        return GLFW.glfwGetKey(c.getWindow().getHandle(),code)==GLFW.GLFW_PRESS;
    }
    private static void tick(MinecraftClient c) {
        boolean down=key(c,GLFW.GLFW_KEY_RIGHT_SHIFT);
        if(down&&!menuDown&&c.currentScreen==null&&c.isWindowFocused()) c.setScreen(new OrbitScreen());
        menuDown=down;
        if(lastWorld!=c.world) {
            lastWorld=c.world; storages.clear(); ticks=0; previousPosition=null; speed=0;
        }
        boolean playing=c.player!=null&&c.world!=null&&c.currentScreen==null&&c.isWindowFocused();
        if(playing&&ZOOM.on()&&key(c,GLFW.GLFW_KEY_C)) {
            if(originalFov==null) originalFov=c.options.getFov().getValue();
            c.options.getFov().setValue(ZOOM_FOV.integer());
        } else if(originalFov!=null) {
            c.options.getFov().setValue(originalFov); originalFov=null;
        }
        target="";
        if(c.player==null||c.world==null) return;
        Vec3d position=new Vec3d(c.player.getX(),c.player.getY(),c.player.getZ());
        if(previousPosition!=null) {
            double dx=position.x-previousPosition.x,dz=position.z-previousPosition.z;
            double measured=Math.sqrt(dx*dx+dz*dz)*20;
            speed=measured>100?0:speed*0.6+measured*0.4;
        }
        previousPosition=position;
        if(!STORAGE.on()) storages.clear();
        else if(ticks++%INTERVAL.integer()==0) scan(c);
        if(!playing) return;
        if(SPRINT.on()&&c.options.forwardKey.isPressed()&&!c.options.backKey.isPressed()
                &&!c.player.isSneaking()&&!c.player.isUsingItem()&&!c.player.horizontalCollision
                &&(c.player.getHungerManager().getFoodLevel()>=FOOD.integer()||c.player.getAbilities().allowFlying)) c.player.setSprinting(true);
        if(AIM.on()&&(!HOLD.on()||key(c,GLFW.GLFW_KEY_LEFT_ALT))) assist(c);
    }
    private static void assist(MinecraftClient c) {
        PlayerEntity self=c.player,nearest=null;
        double best=RANGE.get()*RANGE.get();
        for(PlayerEntity other:c.world.getPlayers()) {
            if(other==self||!other.isAlive()||other.isSpectator()||(!INVISIBLE.on()&&other.isInvisible())) continue;
            double distance=self.squaredDistanceTo(other);
            if(distance>=best||(VISIBLE.on()&&!self.canSee(other))) continue;
            Vec3d d=other.getEyePos().subtract(self.getEyePos());
            float yaw=(float)Math.toDegrees(Math.atan2(d.z,d.x))-90;
            if(Math.abs(MathHelper.wrapDegrees(yaw-self.getYaw()))>FOV.get()/2) continue;
            best=distance; nearest=other;
        }
        if(nearest==null) return;
        Vec3d d=nearest.getEyePos().add(0,-HEIGHT.get(),0).subtract(self.getEyePos());
        float yaw=(float)Math.toDegrees(Math.atan2(d.z,d.x))-90;
        float pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z)));
        float step=(float)(STRENGTH.get()*1.5),smooth=(float)SMOOTH.get();
        self.setYaw(self.getYaw()+MathHelper.clamp(MathHelper.wrapDegrees(yaw-self.getYaw())*smooth,-step,step));
        self.setPitch(MathHelper.clamp(self.getPitch()+MathHelper.clamp((pitch-self.getPitch())*smooth,-step,step),-90,90));
        target=nearest.getName().getString();
    }
    private static void scan(MinecraftClient c) {
        storages.clear();
        BlockPos center=c.player.getBlockPos();
        BlockPos.Mutable cursor=new BlockPos.Mutable();
        int radius=RADIUS.integer(),vertical=VERTICAL.integer();
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            int wx=center.getX()+x,wz=center.getZ()+z;
            if(!c.world.isChunkLoaded(wx>>4,wz>>4)) continue;
            for(int y=-vertical;y<=vertical;y++) {
                cursor.set(wx,center.getY()+y,wz);
                Block b=c.world.getBlockState(cursor).getBlock();
                String name; int color;
                if(CHESTS.on()&&b instanceof ChestBlock) {name="Chest";color=0xFFFFCA72;}
                else if(CHESTS.on()&&b instanceof BarrelBlock) {name="Barrel";color=0xFFDDA977;}
                else if(SHULKERS.on()&&b instanceof ShulkerBoxBlock) {name="Shulker";color=0xFFE5A1FF;}
                else if(ENDER.on()&&b instanceof EnderChestBlock) {name="Ender chest";color=0xFF9F8FFF;}
                else if(UTILITY.on()&&b instanceof HopperBlock) {name="Hopper";color=0xFFB4C6D9;}
                else if(UTILITY.on()&&b instanceof AbstractFurnaceBlock) {name="Furnace";color=0xFFFF966E;}
                else if(UTILITY.on()&&b instanceof DispenserBlock) {name="Dispenser/dropper";color=0xFFB4C6D9;}
                else continue;
                storages.add(new Storage(cursor.toImmutable(),name,color));
            }
        }
        storages.sort(Comparator.comparingDouble(s->c.player.squaredDistanceTo(Vec3d.ofCenter(s.pos()))));
    }
    private static void text(DrawContext ctx,String s,int x,int y,int color) {
        ctx.drawText(MinecraftClient.getInstance().textRenderer,s,x,y,color,false);
    }
    private static void panel(DrawContext ctx,int x,int y,int width,int height) {
        int alpha=(int)Math.round(OPACITY.get()*255);
        ctx.fill(x,y,x+width,y+height,(alpha<<24)|0x121522);
        ctx.fill(x,y,x+width,y+2,ACCENT);
    }
    private static void hud(DrawContext ctx) {
        MinecraftClient c=MinecraftClient.getInstance();
        if(c.player==null||c.world==null||c.options.hudHidden||c.currentScreen!=null) return;
        int margin=MARGIN.integer(),y=margin;
        if(HUD.on()) {
            List<String> lines=new ArrayList<>();
            lines.add("Right Shift / settings");
            lines.add("Aim "+(AIM.on()?"ON":"OFF")+" | Swing "+(SWING_ENABLED.on()?SWING.formatted():"1.00")+"x");
            if(COORDS.on()) lines.add("XYZ "+c.player.getBlockX()+" / "+c.player.getBlockY()+" / "+c.player.getBlockZ());
            if(SPEED.on()) lines.add(String.format(Locale.ROOT,"Speed %.1f blocks/s",speed));
            if(COMPASS.on()) {
                String[] names={"S","SW","W","NW","N","NE","E","SE"};
                int index=Math.floorMod(Math.round(c.player.getYaw()/45),8);
                lines.add("Heading "+names[index]+" / "+Math.round(MathHelper.wrapDegrees(c.player.getYaw()))+" deg");
            }
            if(SPRINT.on()) lines.add("Auto sprint / enabled");
            if(!target.isEmpty()) lines.add("Target: "+target);
            panel(ctx,margin,y,204,30+lines.size()*13);
            text(ctx,"DONUTCLIENT",margin+10,y+10,ACCENT);
            for(int i=0;i<lines.size();i++) text(ctx,c.textRenderer.trimToWidth(lines.get(i),184),margin+10,y+27+i*13,TEXT);
            y+=38+lines.size()*13;
        }
        if(PLAYERS.on()) {
            List<PlayerEntity> players=new ArrayList<>();
            for(PlayerEntity p:c.world.getPlayers()) if(p!=c.player&&p.isAlive()&&!p.isSpectator()&&c.player.squaredDistanceTo(p)<=PLAYER_RANGE.get()*PLAYER_RANGE.get()) players.add(p);
            players.sort(Comparator.comparingDouble(p->c.player.squaredDistanceTo(p)));
            int count=Math.min(PLAYER_ROWS.integer(),players.size());
            panel(ctx,margin,y,204,32+Math.max(1,count)*13);
            text(ctx,"PLAYERS / "+players.size(),margin+10,y+10,ACCENT);
            if(count==0) text(ctx,"No nearby players",margin+10,y+27,MUTED);
            for(int i=0;i<count;i++) {
                PlayerEntity p=players.get(i);
                double dx=p.getX()-c.player.getX(),dz=p.getZ()-c.player.getZ();
                String direction=Math.abs(dx)>Math.abs(dz)?(dx>0?"E":"W"):(dz>0?"S":"N");
                String line=p.getName().getString()+" / "+(int)Math.sqrt(c.player.squaredDistanceTo(p))+"m "+direction;
                text(ctx,c.textRenderer.trimToWidth(line,184),margin+10,y+27+i*13,TEXT);
            }
        }
        if(STORAGE.on()) storageHud(ctx,c,margin);
        if(HEALTH.on()&&c.player.isAlive()&&c.player.getHealth()<=HEALTH_LIMIT.get()*2) {
            int w=c.getWindow().getScaledWidth();
            String alert=String.format(Locale.ROOT,"LOW HEALTH / %.1f hearts",c.player.getHealth()/2);
            int tw=c.textRenderer.getWidth(alert);
            ctx.fill(w/2-tw/2-10,margin,w/2+tw/2+10,margin+25,0xDD4B182B);
            text(ctx,alert,w/2-tw/2,margin+9,0xFFFFA4B7);
        }
    }
    private static void storageHud(DrawContext ctx,MinecraftClient c,int margin) {
        int left=c.getWindow().getScaledWidth()-margin-152,top=margin;
        int count=Math.min(ROWS.integer(),storages.size());
        panel(ctx,left,top,152,158+count*13);
        text(ctx,"STORAGE / N UP",left+10,top+10,ACCENT);
        int cx=left+76,cz=top+80;
        ctx.fill(left+10,top+25,left+142,top+135,0xFF1B2031);
        ctx.fill(cx,top+25,cx+1,top+135,0xFF363E54);
        ctx.fill(left+10,cz,left+142,cz+1,0xFF363E54);
        double scale=49.0/RADIUS.get();
        for(Storage s:storages) {
            int px=cx+(int)Math.round((s.pos().getX()+0.5-c.player.getX())*scale);
            int pz=cz+(int)Math.round((s.pos().getZ()+0.5-c.player.getZ())*scale);
            if(px<left+12||px>left+139||pz<top+27||pz>top+132) continue;
            ctx.fill(px-1,pz-1,px+2,pz+2,s.color());
        }
        ctx.fill(cx-2,cz-2,cx+3,cz+3,0xFFFFFFFF);
        double heading=Math.toRadians(c.player.getYaw());
        int hx=cx+(int)(-Math.sin(heading)*7),hz=cz+(int)(Math.cos(heading)*7);
        ctx.fill(hx-1,hz-1,hx+2,hz+2,ACCENT);
        text(ctx,storages.size()+" / "+RADIUS.integer()+"m / +/-"+VERTICAL.integer()+"Y",left+8,top+140,MUTED);
        for(int i=0;i<count;i++) {
            Storage s=storages.get(i);
            int distance=(int)Math.sqrt(c.player.squaredDistanceTo(Vec3d.ofCenter(s.pos())));
            int dy=s.pos().getY()-c.player.getBlockY();
            String line=s.name()+" "+distance+"m "+(dy>=0?"+":"")+dy+"Y";
            text(ctx,c.textRenderer.trimToWidth(line,136),left+8,top+154+i*13,s.color());
        }
    }
}
