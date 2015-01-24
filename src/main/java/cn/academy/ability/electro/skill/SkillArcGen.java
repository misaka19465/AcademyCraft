/**
 * 
 */
package cn.academy.ability.electro.skill;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cn.academy.ability.electro.entity.EntityArcBase;
import cn.academy.ability.electro.entity.EntityAttackingArc;
import cn.academy.api.ability.SkillBase;
import cn.academy.api.ctrl.RawEventHandler;
import cn.academy.api.ctrl.SkillState;
import cn.academy.api.ctrl.pattern.PatternDown;
import cn.academy.api.data.AbilityData;
import cn.academy.api.data.AbilityDataMain;
import cn.academy.core.proxy.ACClientProps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 一般电弧攻击
 * @author WeathFolD
 *
 */
public final class SkillArcGen extends SkillBase {
	
	static final int MAX_HOLD_TIME = 200;
	
	public static SkillArcGen instance;

	public SkillArcGen() {
		instance = this;
	}
	
	@Override
	public void initPattern(RawEventHandler reh) {
		reh.addPattern(new PatternDown() {

			@Override
			public SkillState createSkill(EntityPlayer player) {
				return new StateArc(player);
			}
			
		});
	}
	
	public String getInternalName() {
		return "em_arc";
	}
	
	@SideOnly(Side.CLIENT)
	public ResourceLocation getLogo() {
		return ACClientProps.ELEC_ARC;
	}

	public static class StateArc extends SkillState {

		EntityArcBase arc;
		
		public StateArc(EntityPlayer player) {
			super(player);
		}

		@Override
		public void onStart() {
			if(!player.worldObj.isRemote) {
				AbilityData data = AbilityDataMain.getData(player);
				int id = data.getSkillID(instance), lv = data.getSkillLevel(id), clv = data.getLevelID() + 1;
				float need = 250 - lv * (21 - lv) + 10 * clv * (15 - clv);
				if(data.decreaseCP(0)){
					arc = new EntityAttackingArc(player, instance);
					player.worldObj.spawnEntityInWorld(arc);
				}
			}
		}

		@Override
		public void onFinish() {
			if(arc != null);
				//arc.setDead();
		}
		
	}
}
