/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.client.fx.particle;

import org.blockartistry.mod.DynSurround.util.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Base for particle entities that are long lived and generate
 * other particles as a jet.  This entity does not render - just
 * serves as a particle factory.
 */
@SideOnly(Side.CLIENT)
public class EntityJetFX extends EntityFX {

	public static final int BUBBLE = 0;
	public static final int FIRE = 1;
	public static final int LAVA = 2;

	protected final int jetStrength;
	protected final IParticleFactory factory;

	public EntityJetFX(final int strength, final IParticleFactory factory, final World world, final double x,
			final double y, final double z) {
		super(world, x, y, z);

		this.setAlphaF(0.0F);
		this.jetStrength = strength;
		this.particleMaxAge = (XorShiftRandom.shared.nextInt(strength) + 2) * 20;
		this.factory = factory;
	}

	/*
	 * Nothing to render so optimize out
	 */
	@Override
	public void renderParticle(final WorldRenderer worldRendererIn, final Entity entityIn, final float partialTicks,
			final float p_180434_4_, final float p_180434_5_, final float p_180434_6_, final float p_180434_7_,
			final float p_180434_8_) {
	}

	/*
	 * During update see if a particle needs to be spawned so that it can rise
	 * up.
	 */
	@Override
	public void onUpdate() {

		// Check to see if a particle needs to be generated
		if (this.particleAge % 3 == 0) {
			final Minecraft mc = Minecraft.getMinecraft();
			final EntityFX effect = this.factory.getEntityFX(this.jetStrength, mc.theWorld, this.posX, this.posY,
					this.posZ, 0, 0, 0);
			mc.effectRenderer.addEffect(effect);
		}

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setDead();
		}
	}

	public static class Factory implements IParticleFactory {

		private static IParticleFactory getFactory(final int type) {
			switch (type) {
			case BUBBLE:
				return ParticleFactory.bubbleJet;
			case FIRE:
				return ParticleFactory.fireJet;
			case LAVA:
				return ParticleFactory.lavaJet;
			default:
				return ParticleFactory.bubbleJet;
			}
		}

		@Override
		public EntityFX getEntityFX(int particleID, World world, double x, double y, double z, double dX, double dY,
				double dZ, int... misc) {
			if (misc[0] == LAVA || misc[0] == FIRE)
				world.playSound(x, y, z, "minecraft:fire.fire", 1.0F + particleID / 10.0F, 1.0F, false);
			return new EntityJetFX(particleID, getFactory(misc[0]), world, x, y, z);
		}
	}
}
