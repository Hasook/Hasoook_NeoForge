package com.hasoook.hasoookmod.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityUtils {

    /**
     * 判断实体的位置是否改变
     *
     * @param entity 实体对象
     * @param oldPos 之前的位置
     * @return 如果实体位置发生改变，则返回 true；否则返回 false
     */
    public static boolean hasEntityMoved(Entity entity, Vec3 oldPos) {
        Vec3 currentPos = entity.position(); // 获取当前实体位置
        return !currentPos.equals(oldPos); // 比较当前位置与旧位置
    }
}
