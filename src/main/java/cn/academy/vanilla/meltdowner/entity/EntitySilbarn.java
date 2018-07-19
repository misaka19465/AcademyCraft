package cn.academy.vanilla.meltdowner.entity;

import cn.academy.core.Resources;
import cn.academy.vanilla.ModuleSoundEvent;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.registry.mc.RegEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * @author WeathFolD
 */
@RegEntity
public class EntitySilbarn extends EntityAdvanced {
    
    @SideOnly(Side.CLIENT)
    public static RenderSibarn render;

    @SideOnly(Side.CLIENT)
    static ParticleFactory particles;

    @SideOnly(Side.CLIENT)
    @StateEventCallback
    public static void init(FMLInitializationEvent event) {
        Particle p = new Particle();
        p.texture = Resources.getTexture("entities/silbarn_frag");
        p.size = 0.1f;
        p.gravity = 0.03f;
        p.customRotation = true;
        
        particles = new ParticleFactory(p);
        particles.addDecorator(new ParticleDecorator() {
            
            double vx, vy;
            final double fac = 25;
            {
                double phi = RandUtils.nextDouble() * Math.PI * 2;
                vx = Math.sin(phi);
                vy = Math.cos(phi);
            }

            @SideOnly(Side.CLIENT)
            @Override
            public void decorate(Particle particle) {
                particle.addMotionHandler(new MotionHandler() {
                    @Override
                    public String getID() { return "Rotator"; }
                    @Override
                    public void onStart() {
                        particle.rotationYaw = RandUtils.nextFloat() * 360;
                        particle.rotationPitch = RandUtils.rangef(-90, 90);
                    }

                    @Override
                    public void onUpdate() {
                        particle.rotationYaw += vx * fac;
                        particle.rotationPitch += vy * fac;
                    }
                });
            }
            
        });
    }
    
    boolean hit;
    
    long createTime;
    
    Vec3 axis = Vec3.createVectorHelper(rand.nextInt(), rand.nextInt(), rand.nextInt());
    
    {
        final Rigidbody rigidbody = new Rigidbody();
        rigidbody.linearDrag = 0.8;
        rigidbody.entitySel = EntitySelectors.nothing();
        
        this.addMotionHandler(rigidbody);
        //this.addDaemonHandler(new GravityApply(this, 0.05));
        executeAfter(new EntityCallback<EntitySilbarn>() {
            @Override
            public void execute(EntitySilbarn ent) {
                rigidbody.gravity = 0.12;
            }
        }, 50);
        setSize(.4f, .4f);
    }

    public EntitySilbarn(EntityPlayer player) {
        super(player.worldObj);
        this.regEventHandler(new CollideHandler() {
            
            @Override
            public void onEvent(CollideEvent event) {
                if(!hit) {
                    hit = true;
                    if(event.result.entityHit instanceof EntitySilbarn)
                        worldObj.playSound(player, posX, posY, posZ, ModuleSoundEvent.silbarn_heavy, SoundCategory.AMBIENT, 0.5f, 1.0f, false);
                    else
                        worldObj.playSound(player, posX, posY, posZ, ModuleSoundEvent.silbarn_light, SoundCategory.AMBIENT, 0.5f, 1.0f, false);

                    executeAfter(new EntityCallback() {
                        @Override
                        public void execute(Entity ent) {
                            ent.setDead();
                        }
                    }, 10);
                }
            }
            
        });
        
        Motion3D mo = new Motion3D(player, true);
        mo.applyToEntity(this);
        
        this.rotationYaw = player.rotationYawHead;
        this.isAirBorne = true;
        this.onGround = false;
    }
    
    @SideOnly(Side.CLIENT)
    public EntitySilbarn(World world) {
        super(world);
        this.createTime = GameTimer.getTime();
        
        this.regEventHandler(new CollideHandler() {
            @Override
            public void onEvent(CollideEvent event) {
                if(!hit) {
                    MovingObjectPosition res = event.result;
                    ForgeDirection dir = ForgeDirection.getOrientation(res.sideHit);
                    final double mul = 0.1;
                    double tx = res.hitVec.xCoord + dir.offsetX * mul, 
                        ty = res.hitVec.yCoord + dir.offsetY * mul, 
                        tz = res.hitVec.zCoord + dir.offsetZ * mul;
                    spawnEffects(tx, ty, tz);
                    setDead();
                }
                hit = true;
            }
        });
        
        this.isAirBorne = true;
        this.onGround = false;
    }
    
    @Override
    public void entityInit() {
        dataWatcher.addObject(10, Byte.valueOf((byte) 0));
    }
    
    public boolean isHit() {
        return hit;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        sync();
    }
    
    private void sync() {
        if(worldObj.isRemote) {
            boolean b = dataWatcher.getWatchableObjectByte(10) != 0;
            if(!hit && b) {
                spawnEffects(posX, posY, posZ);
            }
            hit = b;
        } else {
            dataWatcher.updateObject(10, Byte.valueOf((byte) (hit ? 1 : 0)));
        }
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    private void spawnEffects(double tx, double ty, double tz) {
        int n = RandUtils.rangei(18, 27);
        for(int i = 0; i < n; ++i) {
            double vel = RandUtils.ranged(0.08, 0.18),
                vsq = vel * vel,
                vx = rand.nextDouble() * vel,
                vxsq = vx * vx,
                vy = rand.nextDouble() * Math.sqrt(vsq - vxsq),
                vz = Math.sqrt(vsq - vxsq - vy * vy);
            vx *= rand.nextBoolean() ? 1 : -1;
            vy *= rand.nextBoolean() ? 1 : -1;
            vz *= rand.nextBoolean() ? 1 : -1;
            vy += 0.2;
            
            particles.setPosition(posX, posY, posZ);
            particles.setVelocity(vx, vy, vz);
            worldObj.spawnEntityInWorld(particles.next(worldObj));
        }
        //TileMatrix
    }
    
    @SideOnly(Side.CLIENT)
    public static class RenderSibarn extends Render {
        
        private final IModelCustom model = Resources.getModel("silbarn");
        private final ResourceLocation tex = Resources.getTexture("models/silbarn");

        @Override
        public void doRender(Entity var1, double x, double y,
                double z, float var8, float var9) {
            EntitySilbarn sibarn = (EntitySilbarn) var1;
            if(sibarn.hit)
                return;
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            RenderUtils.loadTexture(tex);
            double scale = .05;
            GL11.glScaled(scale, scale, scale);
            GL11.glRotated(0.03 * (GameTimer.getTime() - sibarn.createTime), 
                    sibarn.axis.xCoord, sibarn.axis.yCoord, sibarn.axis.zCoord);
            GL11.glRotated(-var1.rotationYaw, 0, 1, 0);
            GL11.glRotated(90, 1, 0, 0);
            model.renderAll();
            GL11.glPopMatrix();
        }

        @Override
        protected ResourceLocation getEntityTexture(Entity var1) {
            return null;
        }
        
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {}

}