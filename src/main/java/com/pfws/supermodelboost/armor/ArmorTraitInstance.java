package com.pfws.supermodelboost.armor;

/**
 * 护甲特性实例
 * 
 * @param id 特性ID
 * @param level 特性等级
 * 
 * @author PFWs
 */
public record ArmorTraitInstance(String id, int level) {
    public String getId() { return id; }
    public int getLevel() { return level; }
}
