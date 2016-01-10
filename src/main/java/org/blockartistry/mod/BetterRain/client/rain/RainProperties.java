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

package org.blockartistry.mod.BetterRain.client.rain;

import org.blockartistry.mod.BetterRain.BetterRain;
import org.blockartistry.mod.BetterRain.ModOptions;
import org.blockartistry.mod.BetterRain.data.DimensionEffectData;
import org.blockartistry.mod.BetterRain.data.RainPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public enum RainProperties {

	VANILLA, NONE(0.0F, "calm"), CALM(0.1F, "calm"), LIGHT(0.33F, "light"), NORMAL(0.66F, "normal"), HEAVY(1.0F,
			"heavy");

	private static final float SOUND_LEVEL = ModOptions.getSoundLevel();

	private static float intensityLevel = 0.0F;
	private static RainProperties intensity = VANILLA;
	private static float fogDensity = 0.0F;
	private static RainPhase rainPhase = RainPhase.NOT_RAINING;

	private final float level;
	private final ResourceLocation rainTexture;
	private final ResourceLocation snowTexture;
	private final ResourceLocation dustTexture;
	private final String rainSound;
	private final String dustSound;

	private RainProperties() {
		this.level = -10.0F;
		this.rainTexture = EntityRenderer.locationRainPng;
		this.snowTexture = EntityRenderer.locationSnowPng;
		this.dustTexture = new ResourceLocation(BetterRain.MOD_ID, "textures/environment/dust_calm.png");
		this.rainSound = String.format("%s:%s", BetterRain.MOD_ID, "rain");
		this.dustSound = String.format("%s:%s", BetterRain.MOD_ID, "dust");
	}

	private RainProperties(final float level, final String intensity) {
		this.level = level;
		this.rainTexture = new ResourceLocation(BetterRain.MOD_ID,
				String.format("textures/environment/rain_%s.png", intensity));
		this.snowTexture = new ResourceLocation(BetterRain.MOD_ID,
				String.format("textures/environment/snow_%s.png", intensity));
		this.dustTexture = new ResourceLocation(BetterRain.MOD_ID,
				String.format("textures/environment/dust_%s.png", intensity));
		this.rainSound = String.format("%s:%s", BetterRain.MOD_ID, "rain");
		this.dustSound = String.format("%s:%s", BetterRain.MOD_ID, "dust");
	}

	public static RainProperties getIntensity() {
		return intensity;
	}

	public static float getIntensityLevel() {
		return intensityLevel;
	}

	public static float getFogDensity() {
		return fogDensity;
	}

	public String getRainSound() {
		return this.rainSound;
	}

	public String getDustSound() {
		return this.dustSound;
	}

	public static float getCurrentRainVolume() {
		return (doVanillaRain() ? 0.66F : intensityLevel) * SOUND_LEVEL;
	}

	public static ResourceLocation getCurrentRainSound() {
		return new ResourceLocation(intensity.rainSound);
	}

	public static ResourceLocation getCurrentDustSound() {
		return new ResourceLocation(intensity.dustSound);
	}

	public static boolean doVanillaRain() {
		return intensity == VANILLA;
	}

	/**
	 * Sets the rain intensity based on the intensityLevel level provided. This
	 * is called by the packet handler when the server wants to set the
	 * intensity level on the client.
	 */
	public static void setIntensity(float level) {

		// If the level is Vanilla it means that
		// the rainfall in the dimension is to be
		// that of Vanilla.
		if (level == VANILLA.level) {
			intensity = VANILLA;
			intensityLevel = 0.0F;
			fogDensity = 0.0F;
			setTextures();
			return;
		}

		level = MathHelper.clamp_float(level, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);

		if (intensityLevel != level) {
			intensityLevel = level;
			level += 0.01;
			fogDensity = level * level * 0.13F;
			if (intensityLevel <= NONE.level)
				intensity = NONE;
			else if (intensityLevel < CALM.level)
				intensity = CALM;
			else if (intensityLevel < LIGHT.level)
				intensity = LIGHT;
			else if (intensityLevel < NORMAL.level)
				intensity = NORMAL;
			else
				intensity = HEAVY;
		}
	}

	/**
	 * Sets the phase in the current rain cycle to the specified
	 * value.
	 */
	public static void setRainPhase(final int phase) {
		final RainPhase newCycle = RainPhase.values()[phase];
		if (newCycle != rainPhase) {
			String msg = null;
			if (newCycle == RainPhase.STARTING)
				msg = StatCollector.translateToLocal("msg.RainStarting");
			else if (newCycle == RainPhase.STOPPING)
				msg = StatCollector.translateToLocal("msg.RainStopping");
			if (msg != null)
				Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(msg));
		}
		rainPhase = newCycle;
	}

	public static RainPhase getRainPhase() {
		return rainPhase;
	}

	/**
	 * Set precipitation textures based on the currentAurora intensity. This is
	 * invoked before rendering takes place.
	 */
	public static void setTextures() {
		// AT transform removed final and made public.
		RainSnowRenderer.locationRainPng = intensity.rainTexture;
		RainSnowRenderer.locationSnowPng = intensity.snowTexture;
		RainSnowRenderer.locationDustPng = intensity.dustTexture;
	}
}
