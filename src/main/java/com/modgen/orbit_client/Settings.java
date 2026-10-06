package com.modgen.orbit_client;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class Settings {
    private Settings() {}
    public enum Category { COMBAT, RADAR, MOVEMENT, VISUAL, HUD }
    public enum Option {
        AIM(Category.COMBAT,"Aim assist",0,1,1,0),
        STRENGTH(Category.COMBAT,"Aim strength",0.5,10,0.5,2),
        RANGE(Category.COMBAT,"Aim range",2,32,1,16),
        FOV(Category.COMBAT,"Aim cone (degrees)",10,180,5,130),
        SMOOTH(Category.COMBAT,"Aim smoothing",0.05,1,0.05,0.3),
        HEIGHT(Category.COMBAT,"Below eyes (blocks)",0,1.5,0.05,0.18),
        VISIBLE(Category.COMBAT,"Require line of sight",0,1,1,1),
        INVISIBLE(Category.COMBAT,"Target invisible players",0,1,1,0),
        HOLD(Category.COMBAT,"Require Left Alt",0,1,1,1),
        STORAGE(Category.RADAR,"Storage radar",0,1,1,1),
        RADIUS(Category.RADAR,"Storage radius",8,32,1,24),
        VERTICAL(Category.RADAR,"Vertical radius",2,24,1,12),
        INTERVAL(Category.RADAR,"Scan interval (ticks)",10,100,5,20),
        CHESTS(Category.RADAR,"Chests and barrels",0,1,1,1),
        SHULKERS(Category.RADAR,"Shulker boxes",0,1,1,1),
        ENDER(Category.RADAR,"Ender chests",0,1,1,1),
        UTILITY(Category.RADAR,"Hoppers and furnaces",0,1,1,1),
        ROWS(Category.RADAR,"Storage list rows",0,8,1,5),
        SPRINT(Category.MOVEMENT,"Auto sprint",0,1,1,0),
        FOOD(Category.MOVEMENT,"Sprint minimum food",7,20,1,7),
        SWING_ENABLED(Category.VISUAL,"Custom swing animation",0,1,1,1),
        SWING(Category.VISUAL,"Swing speed multiplier",0.1,4,0.1,1),
        ZOOM(Category.VISUAL,"Hold C to zoom",0,1,1,0),
        ZOOM_FOV(Category.VISUAL,"Zoom FOV",10,60,1,30),
        HUD(Category.HUD,"Dashboard",0,1,1,1),
        COORDS(Category.HUD,"Coordinates",0,1,1,1),
        SPEED(Category.HUD,"Speed meter",0,1,1,1),
        COMPASS(Category.HUD,"Compass",0,1,1,1),
        PLAYERS(Category.HUD,"Nearby player radar",0,1,1,0),
        PLAYER_RANGE(Category.HUD,"Player radar range",8,128,4,48),
        PLAYER_ROWS(Category.HUD,"Player list rows",1,8,1,4),
        HEALTH(Category.HUD,"Low health alert",0,1,1,1),
        HEALTH_LIMIT(Category.HUD,"Alert health (hearts)",1,10,0.5,4),
        OPACITY(Category.HUD,"Panel opacity",0.2,1,0.05,0.85),
        MARGIN(Category.HUD,"Screen margin",4,32,1,8);

        public final Category category;
        public final String label;
        public final double min,max,step,initial;
        private double value;
        Option(Category category,String label,double min,double max,double step,double initial) {
            this.category=category; this.label=label; this.min=min; this.max=max; this.step=step; this.initial=initial; value=initial;
        }
        public boolean toggle() { return min==0 && max==1 && step==1; }
        public boolean on() { return value>=0.5; }
        public double get() { return value; }
        public int integer() { return (int)Math.round(value); }
        public void set(double v) {
            if (!Double.isFinite(v)) return;
            value=Math.clamp(min+Math.round((v-min)/step)*step,min,max);
        }
        public String formatted() {
            if(toggle()) return on()?"ON":"OFF";
            if(step>=1) return Integer.toString(integer());
            return String.format(Locale.ROOT,"%.2f",value);
        }
    }
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("donutclient.properties");
    public static void load() {
        Properties p=new Properties();
        Path source=Files.exists(FILE)?FILE:FILE.resolveSibling("orbit-client.properties");
        if(!Files.exists(source)) return;
        try(Reader reader=Files.newBufferedReader(source)) { p.load(reader); }
        catch(IOException e) { return; }
        for(Option option:Option.values()) {
            String value=p.getProperty(option.name().toLowerCase(Locale.ROOT));
            if(value==null) continue;
            try { option.set(option.toggle()? (Boolean.parseBoolean(value)?1:0):Double.parseDouble(value)); }
            catch(NumberFormatException ignored) {}
        }
    }
    public static void save() {
        Properties p=new Properties();
        for(Option o:Option.values()) p.setProperty(o.name().toLowerCase(Locale.ROOT),o.toggle()?Boolean.toString(o.on()):Double.toString(o.get()));
        try {
            Files.createDirectories(FILE.getParent());
            try(Writer writer=Files.newBufferedWriter(FILE)) { p.store(writer,"Donutclient preferences"); }
        } catch(IOException ignored) {}
    }
    public static void reset(Category category) {
        for(Option o:Option.values()) if(o.category==category) o.value=o.initial;
        save();
    }
}
