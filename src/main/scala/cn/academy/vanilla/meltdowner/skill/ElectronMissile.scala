/**
  * Copyright (c) Lambda Innovation, 2013-2016
  * This file is part of the AcademyCraft mod.
  * https://github.com/LambdaInnovation/AcademyCraft
  * Licensed under GPLv3, see project root for more information.
  */
package cn.academy.vanilla.meltdowner.skill

import cn.academy.ability.api.Skill
import cn.academy.ability.api.context.{ClientContext, ClientRuntime, Context, RegClientContext}
import cn.academy.core.client.ACRenderingHelper
import cn.academy.vanilla.meltdowner.client.render.MdParticleFactory
import cn.academy.vanilla.meltdowner.entity.{EntityMdBall, EntityMdRaySmall}
import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.s11n.network.NetworkMessage.Listener
import cn.lambdalib.util.generic.{MathUtils, VecUtils}
import cn.lambdalib.util.generic.MathUtils._
import cn.lambdalib.util.mc.{EntitySelectors, WorldUtils}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3

/**
  * @author WeAthFolD, KSkun
  */
object ElectronMissile extends Skill("electron_missile", 5) {

  @SideOnly(Side.CLIENT)
  override def activate(rt: ClientRuntime, keyID: Int) = activateSingleKey(rt, keyID, p => new EMContext(p))
  
}

object EMContext {

  final val MSG_EFFECT_SPAWN = "effect_spawn"
  final val MSG_EFFECT_UPDATE = "effect_update"

}

import cn.academy.ability.api.AbilityAPIExt._
import cn.lambdalib.util.generic.RandUtils._
import scala.collection.JavaConversions._
import EMContext._

class EMContext(p: EntityPlayer) extends Context(p, ElectronMissile) {

  private val MAX_HOLD: Int = 5
  
  private var active: java.util.LinkedList[EntityMdBall] = _
  private var ticks: Int = 0

  private val exp: Float = ctx.getSkillExp

  private val overload: Float = lerpf(2, 1.5f, exp)
  private val consumption: Float = lerpf(20, 18, exp)
  private val overload_attacked: Float = lerpf(9, 4, exp)
  private val consumption_attacked: Float = lerpf(60, 25, exp)
  private val overload_keep = lerpf(200, 120, exp)

  private var overloadKeep = 0f

  @Listener(channel=MSG_KEYUP, side=Array(Side.CLIENT))
  private def l_onEnd() = {
    terminate()
  }

  @Listener(channel=MSG_KEYABORT, side=Array(Side.CLIENT))
  private def l_onAbort() = {
    terminate()
  }

  @Listener(channel=MSG_MADEALIVE, side=Array(Side.SERVER))
  private def s_madeAlive() = {
    ctx.consume(overload_keep, 0)
    overloadKeep = ctx.cpData.getOverload
    active = new java.util.LinkedList[EntityMdBall]()
  }

  @Listener(channel=MSG_TICK, side=Array(Side.SERVER))
  private def s_onTick() = {
    if(ctx.cpData.getOverload < overloadKeep) ctx.cpData.setOverload(overloadKeep)
    if (!ctx.consume(overload, consumption)) terminate()
    else {
      val timeLimit: Int = lerpf(80, 200, exp).toInt
      if (ticks <= timeLimit) {
        if (ticks % 10 == 0) if (active.size < MAX_HOLD) {
          val ball: EntityMdBall = new EntityMdBall(player)
          player.worldObj.spawnEntityInWorld(ball)
          active.add(ball)
        }
        if (ticks != 0 && ticks % 8 == 0) {
          val range: Float = lerpf(5, 13, exp)
          val list: java.util.List[Entity] = WorldUtils.getEntities(player, range, EntitySelectors.exclude(player)
            .and(EntitySelectors.living))
          if (!active.isEmpty && !list.isEmpty && ctx.consume(overload_attacked, consumption_attacked)) {
            var min: Double = Double.MaxValue
            var result: Entity = null
            import scala.collection.JavaConversions._
            for (e <- list) {
              val dist: Double = e.getDistanceToEntity(player)
              if (dist < min) {
                min = dist
                result = e
              }
            }
            // Find a random ball and destroy it
            var index: Int = 1 + nextInt(active.size)
            val iter: java.util.Iterator[EntityMdBall] = active.iterator
            var ball: EntityMdBall = null
            while ( {
              index -= 1; index + 1
            } > 0) ball = iter.next
            iter.remove()
            // client action
            sendToClient(MSG_EFFECT_SPAWN, VecUtils.entityPos(ball), VecUtils.add(VecUtils.entityPos(result),
              VecUtils.vec(0, result.getEyeHeight, 0)))
            // server action
            result.hurtResistantTime = -1
            val damage: Float = lerpf(10, 18, exp)
            MDDamageHelper.attack(ctx, result, damage)
            ctx.addSkillExp(0.001f)
            ball.setDead()
          }
        }
      }
      else {
        // ticks > timeLimit
        terminate()
      }
      sendToClient(MSG_EFFECT_UPDATE)
      ticks += 1
    }
  }

  @Listener(channel=MSG_TERMINATED, side=Array(Side.SERVER))
  private def s_onEnd() = {
    val cooldown: Int = MathUtils.clampi(700, 400, ticks)
    ctx.setCooldown(cooldown)

    for (ball <- active) {
      ball.setDead()
    }
  }
  
}

@Registrant
@SideOnly(Side.CLIENT)
@RegClientContext(classOf[EMContext])
class EMContextC(par: EMContext) extends ClientContext(par) {

  @Listener(channel=MSG_EFFECT_UPDATE, side=Array(Side.CLIENT))
  private def c_updateEffect() = {
    var count: Int = rangei(1, 3)
    while ( {
      count -= 1; count + 1
    } > 0) {
      val r: Double = ranged(0.5, 1)
      val theta: Double = ranged(0, Math.PI * 2)
      val h: Double = ranged(-1.2, 0)
      val pos: Vec3 = VecUtils.add(VecUtils.vec(player.posX, player.posY + ACRenderingHelper.getHeightFix(player),
        player.posZ), VecUtils.vec(r * Math.sin(theta), h, r * Math.cos(theta)))
      val vel: Vec3 = VecUtils.vec(ranged(-.02, .02), ranged(.01, .05), ranged(-.02, .02))
      player.worldObj.spawnEntityInWorld(MdParticleFactory.INSTANCE.next(player.worldObj, pos, vel))
    }
  }

  @Listener(channel=MSG_EFFECT_SPAWN, side=Array(Side.CLIENT))
  private def c_spawnRay(from: Vec3, to: Vec3) = {
    val ray: EntityMdRaySmall = new EntityMdRaySmall(world)
    ray.setFromTo(from, to)
    world.spawnEntityInWorld(ray)
  }

}
