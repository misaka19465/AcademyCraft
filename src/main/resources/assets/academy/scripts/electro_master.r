# AcademyCraft Ripple Script file
# Electro Master
# 炮姐什么的最喜欢啦啦啦啦啦啦! >3<

ac {
	electro_master {
		arc_gen { # 电弧激发
		  damage(exp) { lerp(3, 7, exp) } # 伤害
		  consumption(exp) { lerp(60, 90, exp) } # CP消耗
		  overload(exp) { lerp(15, 10, exp) } # 过载
		  p_ignite(exp) { lerp(0, 0.6, exp) } # 点燃概率
		  
		  # 有效攻击时增加的熟练度
		  exp_incr_effective(exp) { 
		      0.00008 * lerp(60, 90, exp)
		  }
		  # 无效攻击时增加的熟练度
		  exp_incr_ineffective(exp) {
		      0.00003 * lerp(60, 90, exp)
		  }
		}
		
		charging { # 电流回充
		  speed(exp) { lerp(5, 15, exp) } # 充能速度，IF/tick
		  consumption(exp) { lerp(3, 7, exp) }
		  overload(exp) { lerp(65, 48, exp) }
		  
		  exp_incr_effective(exp) { lerp(3, 7, exp) * 0.0008 }
		  exp_incr_ineffective(exp) { lerp(3, 7, exp) * 0.0003 }
		}
		
		body_intensify { # 生物电强化, ct=蓄力时间 (range: [10, 40])
		  probability(ct) { (ct - 10.0) / 18.0 } # 总概率
		  time(exp, ct) { floor(4 * lerp(1.5, 2.5, exp) * range_double(1, 2) * ct) } # 每个buff持续时间
		  level(exp, ct) { floor( lerp(0.5, 1, exp) * (ct / 18.0) ) } # buff等级
		  hunger_time(ct) { ct * 5 / 4 } # 饥饿buff时间
		  
		  consumption(exp) { lerp(20, 15, exp) }
		  cooldown(exp) { lerp(45, 30, exp) }
		  overload(exp) { lerp(200, 120, exp) }
		}
	}
}