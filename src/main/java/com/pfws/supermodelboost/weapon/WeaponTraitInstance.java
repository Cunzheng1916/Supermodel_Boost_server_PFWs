package com.pfws.supermodelboost.weapon;

/**
 * 武器特性实例 - 包含特性ID和当前等级
 * 
 * @param id 特性ID
 * @param level 特性等级
 * 
 * @author PFWs
 */
public record WeaponTraitInstance(String id, int level) {
    public String getId() { return id; }
    public int getLevel() { return level; }
}
