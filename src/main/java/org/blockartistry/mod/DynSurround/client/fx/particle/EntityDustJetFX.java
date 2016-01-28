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

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EntityBlockDustFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityDustJetFX extends EntityJetFX {

	protected static final class EntityDustFX extends EntityBlockDustFX {

		public EntityDustFX(final World world, final double x, final double y, final double z, final IBlockState block) {
			super(world, x + RANDOM.nextGaussian() * 0.2D, y, z + RANDOM.nextGaussian() * 0.2D, 0, 0, 0, block);
			this.multipleParticleScaleBy(0.2F);
			this.setPosition(this.posX, this.posY, this.posZ);
		}

	}

	protected final IBlockState blockState;

	public EntityDustJetFX(final int strength, final World world, final double x, final double y, final double z,
			final IBlockState state) {
		super(strength, world, x, y, z, 2);
		this.blockState = state;
	}

	@Override
	protected EntityFX getJetParticle() {
		return new EntityDustFX(this.worldObj, this.posX, this.posY, this.posZ, this.blockState).func_174845_l();
	}

}
