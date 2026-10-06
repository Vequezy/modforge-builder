package com.modgen.orbit_client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.Arrays;
import java.util.List;

public final class OrbitScreen extends Screen {
    private Settings.Category category=Settings.Category.COMBAT;
    private int page;
    private int left,top,panelWidth,panelHeight,perPage;
    public OrbitScreen() { super(Text.literal("Donutclient")); }
    @Override protected void init() {
        panelWidth=Math.min(520,width-16);
        panelHeight=Math.min(350,height-16);
        left=(width-panelWidth)/2; top=(height-panelHeight)/2;
        perPage=Math.max(1,(panelHeight-132)/27);
        int side=108;
        int i=0;
        for(Settings.Category c:Settings.Category.values()) {
            addDrawableChild(ButtonWidget.builder(Text.literal((c==category?"> ":"")+title(c)),b->{category=c;page=0;clearAndInit();})
                .dimensions(left+10,top+65+i++*28,side-18,22).build());
        }
        List<Settings.Option> options=Arrays.stream(Settings.Option.values()).filter(o->o.category==category).toList();
        int pages=Math.max(1,(options.size()+perPage-1)/perPage);
        page=Math.clamp(page,0,pages-1);
        int x=left+side+10;
        int w=panelWidth-side-22;
        for(int row=0;row<perPage;row++) {
            int index=page*perPage+row;
            if(index>=options.size()) break;
            Settings.Option o=options.get(index);
            int y=top+64+row*27;
            if(o.toggle()) {
                addDrawableChild(ButtonWidget.builder(label(o),b->{o.set(o.on()?0:1);b.setMessage(label(o));Settings.save();}).dimensions(x,y,w,22).build());
            } else addDrawableChild(new SettingSlider(x,y,w,o));
        }
        int bottom=top+panelHeight-54;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"),b->{page=(page+pages-1)%pages;clearAndInit();}).dimensions(x,bottom,24,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal((page+1)+" / "+pages),b->{}).dimensions(x+28,bottom,48,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"),b->{page=(page+1)%pages;clearAndInit();}).dimensions(x+80,bottom,24,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset tab"),b->{Settings.reset(category);clearAndInit();}).dimensions(x+w-82,bottom,82,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(left+10,top+panelHeight-54,88,20).build());
    }
    private static String title(Settings.Category c) {
        String s=c.name().toLowerCase(java.util.Locale.ROOT); return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }
    private static Text label(Settings.Option o) { return Text.literal(o.label+"  "+o.formatted()); }
    private static final class SettingSlider extends SliderWidget {
        private final Settings.Option option;
        SettingSlider(int x,int y,int width,Settings.Option option) {
            super(x,y,width,22,label(option),(option.get()-option.min)/(option.max-option.min));
            this.option=option;
        }
        @Override protected void updateMessage() { if(option!=null) setMessage(label(option)); }
        @Override protected void applyValue() {
            if(option==null) return;
            option.set(option.min+value*(option.max-option.min));
            updateMessage(); Settings.save();
        }
    }
    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta) {
        c.fill(0,0,width,height,0xCE070910);
        c.fill(left-1,top-1,left+panelWidth+1,top+panelHeight+1,0xFF303348);
        c.fill(left,top,left+panelWidth,top+panelHeight,0xFF121522);
        c.fill(left,top,left+panelWidth,top+3,0xFFFF8BC7);
        c.fill(left,top+52,left+108,top+panelHeight,0xFF0D101A);
        c.drawText(textRenderer,"DONUTCLIENT",left+14,top+14,0xFFFF8BC7,false);
        c.drawText(textRenderer,"DonutSMP / control center",left+14,top+31,0xFF98A6C0,false);
        c.drawText(textRenderer,title(category)+" / SETTINGS",left+118,top+48,0xFFE5E9F5,false);
        String hint=switch(category) {
            case COMBAT -> "Left Alt: aim / cone is full width";
            case RADAR -> "Loaded blocks only / north is up";
            case MOVEMENT -> "Sprint while moving forward";
            case VISUAL -> "0.10x slower / 4.00x faster / C: zoom";
            case HUD -> "Panels hide while menus are open";
        };
        c.drawText(textRenderer,textRenderer.trimToWidth(hint,panelWidth-24),left+12,top+panelHeight-23,0xFF98A6C0,false);
        super.render(c,mouseX,mouseY,delta);
    }
    @Override public void close() { Settings.save(); super.close(); }
    @Override public boolean shouldPause() { return false; }
}
