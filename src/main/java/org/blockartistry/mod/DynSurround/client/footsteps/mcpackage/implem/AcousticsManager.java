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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.implem.AcousticsLibrary;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.game.system.Association;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IDefaultStepPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 * 
 * @author Hurry
 */
@SideOnly(Side.CLIENT)
public class AcousticsManager extends AcousticsLibrary implements ISoundPlayer, IDefaultStepPlayer {
	private IIsolator isolator;

	private final Random random = new Random();
	private List<PendingSound> pending = new ArrayList<PendingSound>();
	private long minimum;

	private boolean USING_LATENESS = true;
	private boolean USING_EARLYNESS = true;
	private float LATENESS_THRESHOLD_DIVIDER = 1.5f;
	private double EARLYNESS_THRESHOLD_POW = 0.75d;

	public AcousticsManager(final IIsolator isolator) {
		this.isolator = isolator;
	}

	@Override
	public void playStep(final EntityLivingBase entity, final Association assos) {
		Block block = assos.getBlock();
		if (!block.getMaterial().isLiquid() && block.stepSound != null) {
			Block.SoundType soundType = block.stepSound;

			if (Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(assos.x, assos.y + 1, assos.z)).getBlock() == Blocks.snow_layer) {
				soundType = Blocks.snow_layer.stepSound;
			}

			entity.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getFrequency());
		}
	}

	@Override
	public void playSound(final Object location, final String soundName, final float volume, final float pitch, final IOptions options) {
		if (!(location instanceof Entity))
			return;

		if (options != null) {
			if (options.hasOption("delay_min") && options.hasOption("delay_max")) {
				long delay = randAB(this.random, (Long) options.getOption("delay_min"),
						(Long) options.getOption("delay_max"));

				if (delay < minimum) {
					minimum = delay;
				}

				pending.add(
						new PendingSound(location, soundName, volume, pitch, null, System.currentTimeMillis() + delay,
								options.hasOption("skippable") ? -1 : (Long) options.getOption("delay_max")));
			} else {
				actuallyPlaySound((Entity) location, soundName, volume, pitch);
			}
		} else {
			actuallyPlaySound((Entity) location, soundName, volume, pitch);
		}
	}

	protected void actuallyPlaySound(final Entity location, final String soundName, final float volume, final float pitch) {
		ModLog.debug("    Playing sound " + soundName + " ("
				+ String.format(Locale.ENGLISH, "v%.2f, p%.2f", volume, pitch) + ")");
		location.playSound(soundName, volume, pitch);
	}

	private long randAB(final Random rng, final long a, final long b) {
		return a >= b ? a : a + rng.nextInt((int) b + 1);
	}

	@Override
	public Random getRNG() {
		return random;
	}

	@Override
	protected void onAcousticNotFound(final Object location, final String acousticName, final EventType event, final IOptions inputOptions) {
		ModLog.info("Tried to play a missing acoustic: " + acousticName);
	}

	@Override
	public void think() {
		if (pending.isEmpty() || System.currentTimeMillis() < minimum)
			return;

		long newMinimum = Long.MAX_VALUE;
		long time = System.currentTimeMillis();

		Iterator<PendingSound> iter = pending.iterator();
		while (iter.hasNext()) {
			PendingSound sound = iter.next();

			if (time >= sound.getTimeToPlay() || USING_EARLYNESS
					&& time >= sound.getTimeToPlay() - Math.pow(sound.getMaximumBase(), EARLYNESS_THRESHOLD_POW)) {
				if (USING_EARLYNESS && time < sound.getTimeToPlay()) {
					ModLog.debug("    Playing early sound (early by " + (sound.getTimeToPlay() - time)
							+ "ms, tolerence is " + Math.pow(sound.getMaximumBase(), EARLYNESS_THRESHOLD_POW));
				}

				long lateness = time - sound.getTimeToPlay();
				if (!USING_LATENESS || sound.getMaximumBase() < 0
						|| lateness <= sound.getMaximumBase() / LATENESS_THRESHOLD_DIVIDER) {
					sound.playSound(this);
				} else {
					ModLog.debug("    Skipped late sound (late by " + lateness + "ms, tolerence is "
							+ sound.getMaximumBase() / LATENESS_THRESHOLD_DIVIDER + "ms)");
				}
				iter.remove();
			} else {
				newMinimum = sound.getTimeToPlay();
			}
		}

		minimum = newMinimum;
	}

	@Override
	protected ISoundPlayer mySoundPlayer() {
		return isolator.getSoundPlayer();
	}
}