/*
 * This file is part of BetterRain, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.BetterRain.server;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;

import java.util.Random;

import org.blockartistry.mod.BetterRain.ModLog;
import org.blockartistry.mod.BetterRain.data.RainData;
import org.blockartistry.mod.BetterRain.network.Network;

public class RainHandler {

	private static final Random random = new Random();
	
	public static void initialize() {
		FMLCommonHandler.instance().bus().register(new RainHandler());
	}

	@SubscribeEvent
	public void tickEvent(final TickEvent.WorldTickEvent event) {

		final World world = event.world;

		// Only handle things that are classified as surface
		// worlds.
		if (!world.provider.isSurfaceWorld())
			return;

		final RainData data = RainData.get(world);
		if (world.getRainStrength(1.0F) > 0.0F) {
			if (data.getRainStrength() == 0.0F) {
				final float str = 0.05F + (0.90F * random.nextFloat());
				data.setRainStrength(str);
				ModLog.info(String.format("Rain strength set to %f", data.getRainStrength()));
			}
		} else if(data.getRainStrength() > 0.0F) {
			ModLog.info("Rain is stopping");
			data.setRainStrength(0.0F);
		}

		Network.sendRainStrength(data.getRainStrength(), world.provider.dimensionId);
	}
}
