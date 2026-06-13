package com.pfws.supermodelboost.weapon.skill;

import com.pfws.supermodelboost.SupermodelBoostMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 技能抽取引擎 - 根据倾向概率随机选择技能
 * 
 * @author PFWs
 */
public final class SkillRollEngine {
    private final Random random = new Random();

    public Optional<ActiveSkillInstance> rollAndAssignSkill(ItemStack stack, String tendency) {
        if (!isApplicableWeapon(stack)) return Optional.empty();

        List<String> allSkills = ActiveSkillRegistry.getAllIds();
        if (allSkills.isEmpty()) return Optional.empty();

        String selectedId;
        if (tendency != null && !tendency.isEmpty()) {
            selectedId = rollWithTendency(allSkills, tendency);
        } else {
            selectedId = allSkills.get(random.nextInt(allSkills.size()));
        }

        if (selectedId == null) return Optional.empty();

        ActiveSkillInstance skill = new ActiveSkillInstance(selectedId, 1);
        ActiveSkillNbtHelper.setSkill(stack, skill);
        ActiveSkillNbtHelper.setSkillXp(stack, 0);
        ActiveSkillNbtHelper.setCooldownEnd(stack, 0);

        return Optional.of(skill);
    }

    private String rollWithTendency(List<String> allSkills, String tendency) {
        List<String> preferredSkills = ActiveSkillRegistry.getSkillsByFamily(tendency);
        List<String> otherSkills = new ArrayList<>(allSkills);
        otherSkills.removeAll(preferredSkills);

        double roll = random.nextDouble();
        if (roll < 0.80 && !preferredSkills.isEmpty()) {
            return preferredSkills.get(random.nextInt(preferredSkills.size()));
        } else if (!otherSkills.isEmpty()) {
            return otherSkills.get(random.nextInt(otherSkills.size()));
        } else {
            return preferredSkills.isEmpty() ? null : preferredSkills.get(random.nextInt(preferredSkills.size()));
        }
    }

    public static boolean isApplicableWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return itemId.contains("_sword") || itemId.contains("_axe");
    }
}
