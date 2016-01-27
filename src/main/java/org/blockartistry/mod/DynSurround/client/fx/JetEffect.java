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

package org.blockartistry.mod.DynSurround.client.fx;

import java.util.Random;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleFactory;

import org.blockartistry.mod.DynSurround.client.fx.particle.EntityJetFX;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public abstract class JetEffect extends BlockEffect {

	private static final int MAX_STRENGTH = 10;

	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	protected int countBlocks(final World world, final int x, final int y, final int z, final Block block,
			final int dir) {
		int count = 0;
		int idx = y;
		while (count < MAX_STRENGTH) {
			if (world.getBlockState(pos.set(x, idx, z)).getBlock() != block)
				return count;
			count++;
			idx += dir;
		}
		return count;
	}

	public JetEffect(final int chance) {
		super(chance);
	}

	public static class Fire extends JetEffect {
		public Fire() {
			super(ModOptions.getFireJetsSpawnChance());
		}

		@Override
		public boolean trigger(final Block block, final World world, final int x, final int y, final int z,
				final Random random) {
			return super.trigger(block, world, x, y, z, random) && world.isAirBlock(pos.set(x, y + 1, z));
		}

		public void doEffect(final Block block, final World world, final int x, final int y, final int z,
				final Random random) {
			final int lavaBlocks = countBlocks(world, x, y, z, block, -1);
			final int jetType = random.nextInt(3) == 0 ? EntityJetFX.LAVA : EntityJetFX.FIRE;
			final EntityFX effect = ParticleFactory.jet.getEntityFX(lavaBlocks, world, x + 0.5D, y + 1.1D, z + 0.5D, 0,
					0, 0, jetType);
			Minecraft.getMinecraft().effectRenderer.addEffect(effect);
		}
	}

	public static class Bubble extends JetEffect {
		public Bubble() {
			super(ModOptions.getBubbleJetSpawnChance());
		}

		@Override
		public boolean trigger(final Block block, final World world, final int x, final int y, final int z,
				final Random random) {
			return super.trigger(block, world, x, y, z, random)
					&& world.getBlockState(pos.set(x, y - 1, z)).getBlock().getMaterial().isSolid();
		}

		public void doEffect(final Block block, final World world, final int x, final int y, final int z,
				final Random random) {
			final int waterBlocks = countBlocks(world, x, y, z, block, 1);
			final EntityFX effect = ParticleFactory.jet.getEntityFX(waterBlocks, world, x + 0.5D, y + 1.1D, z + 0.5D, 0,
					0, 0, EntityJetFX.BUBBLE);
			Minecraft.getMinecraft().effectRenderer.addEffect(effect);
		}
	}
}
