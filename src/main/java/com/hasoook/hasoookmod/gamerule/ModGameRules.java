package com.hasoook.hasoookmod.gamerule;

import net.minecraft.world.level.GameRules;

public class ModGameRules {
    // 破坏方块随机掉落规则（默认关闭）
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_BLOCK_DROPS =
            GameRules.register("randomBlockDrops", GameRules.Category.MISC, GameRules.BooleanValue.create(false));

    public static void registerGameRules() {
    }
}