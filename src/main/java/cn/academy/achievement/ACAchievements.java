package cn.academy.achievement;

import cn.academy.achievement.aches.ACAchievement;
import cn.academy.achievement.pages.*;
import cn.academy.AcademyCraft;
import cn.academy.item.ItemAchievement;
import cn.academy.misc.achievement.pages.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.AchievementPage;

/**
 * @author EAirPeter
 */
public final class ACAchievements {

    private static PageDefault pageDefault;
    private static PageCtElectromaster pageCtElectromaster;
    private static PageCtMeltdowner pageCtMeltdowner;
    private static PageCtTeleporter pageCtTeleporter;
    private static PageCtVecmanip pageCtVecmanip;
    
    @RegItem
    @RegItem.HasRender
    public static ItemAchievement DUMMY_ITEM;

    @RegInitCallback
    private static void init() {
        DispatcherAch.init();

        AchievementPage.registerAchievementPage(pageDefault = new PageDefault());
        
        AchievementPage.registerAchievementPage(pageCtElectromaster = new PageCtElectromaster());
        AchievementPage.registerAchievementPage(pageCtMeltdowner = new PageCtMeltdowner());
        AchievementPage.registerAchievementPage(pageCtTeleporter = new PageCtTeleporter());
        AchievementPage.registerAchievementPage(pageCtVecmanip = new PageCtVecmanip());
    }
    
    /**
     * Trigger an achievement
     * @param player The player
     * @param achid The id of the achievement
     * @return true if succeeded
     */
    public static boolean trigger(EntityPlayer player, String achid) {
        ACAchievement ach = ACAchievement.getById(achid);
        if (ach == null) {
            AcademyCraft.log.warn("AC Achievement '" + achid + "' does not exist");
            return false;
        }
        player.triggerAchievement(ach);
        return true;
    }
    
}