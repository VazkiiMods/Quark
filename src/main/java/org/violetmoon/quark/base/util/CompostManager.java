package org.violetmoon.quark.base.util;

import net.minecraft.world.item.Item;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZAddReloadListener;

import java.util.HashMap;

public class CompostManager {

    private static HashMap<Item, Float> compostChances = new HashMap<>();

    public static void addChance(Item item, float chance){
        Quark.LOG.info("added " + item.getDefaultInstance().getDisplayName().getString() + " to CompostManager with chance " + chance);
        compostChances.put(item, chance);
    }

    public static float getChance(Item item){
        return compostChances.get(item);
    }

    public static boolean doesItemHaveChance(Item item){
        return compostChances.containsKey(item);
    }

    public static void flush(){
        compostChances.clear();
    }
}
