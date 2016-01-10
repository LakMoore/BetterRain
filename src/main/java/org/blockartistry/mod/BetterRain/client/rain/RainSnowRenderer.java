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
import org.blockartistry.mod.BetterRain.client.IAtmosRenderer;
import org.blockartistry.mod.BetterRain.client.WeatherUtils;
import org.blockartistry.mod.BetterRain.data.EffectType;
import org.blockartistry.mod.BetterRain.util.XorShiftRandom;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class RainSnowRenderer implements IAtmosRenderer {

	private static final XorShiftRandom random = new XorShiftRandom();

	public static ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
	public static ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
	public static ResourceLocation locationDustPng = new ResourceLocation(BetterRain.MOD_ID,
			"textures/environment/dust.png");

	private static final float[] RAIN_X_COORDS = new float[1024];
	private static final float[] RAIN_Y_COORDS = new float[1024];

	static {
		for (int i = 0; i < 32; ++i) {
			for (int j = 0; j < 32; ++j) {
				final float f2 = (float) (j - 16);
				final float f3 = (float) (i - 16);
				final float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
				RAIN_X_COORDS[i << 5 | j] = -f3 / f4;
				RAIN_Y_COORDS[i << 5 | j] = f2 / f4;
			}
		}
	}

	/**
	 * Render rain and snow
	 */
	public void render(final EntityRenderer renderer, final float partialTicks) {
		
		RainProperties.setTextures();
		
		IRenderHandler r = renderer.mc.theWorld.provider.getWeatherRenderer();
		if (r != null) {
			r.render(partialTicks, renderer.mc.theWorld, renderer.mc);
			return;
		}

		final float rainStrength = renderer.mc.theWorld.getRainStrength(partialTicks);
		if (rainStrength <= 0.0F)
			return;

		renderer.enableLightmap();

		final Entity entity = renderer.mc.getRenderViewEntity();
		final World world = renderer.mc.theWorld;
		final int playerX = MathHelper.floor_double(entity.posX);
		final int playerY = MathHelper.floor_double(entity.posY);
		final int playerZ = MathHelper.floor_double(entity.posZ);
		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tess.getWorldRenderer();

		GlStateManager.disableCull();
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.alphaFunc(516, 0.1F);

		final double spawnX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
		final double spawnY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
		final double spawnZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

		final int locY = MathHelper.floor_double(spawnY);
		final int b0 = renderer.mc.gameSettings.fancyGraphics ? 10 : 5;

		int j1 = -1;
		float f1 = (float) renderer.rendererUpdateCount + partialTicks;
		worldrenderer.setTranslation(-spawnX, -spawnY, -spawnZ);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int k1 = playerZ - b0; k1 <= playerZ + b0; ++k1) {
			for (int l1 = playerX - b0; l1 <= playerX + b0; ++l1) {
				int i2 = (k1 - playerZ + 16) * 32 + l1 - playerX + 16;
				double d3 = (double) RAIN_X_COORDS[i2] * 0.5D;
				double d4 = (double) RAIN_Y_COORDS[i2] * 0.5D;
				mutable.set(l1, 0, k1);
				BiomeGenBase biome = world.getBiomeGenForCoords(mutable);
				final boolean hasDust = WeatherUtils.biomeHasDust(biome);

				if (hasDust || EffectType.hasPrecipitation(biome)) {
					int j2 = world.getPrecipitationHeight(mutable).getY();
					int k2 = playerY - b0;
					int l2 = playerY + b0;

					if (k2 < j2) {
						k2 = j2;
					}

					if (l2 < j2) {
						l2 = j2;
					}

					int i3 = j2;

					if (j2 < locY) {
						i3 = locY;
					}

					if (k2 != l2) {
						random.setSeed((long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));
						mutable.set(l1, k2, k1);
						float f2 = biome.getFloatTemperature(mutable);
						final float heightTemp = world.getWorldChunkManager().getTemperatureAtHeight(f2, j2);

						if (!hasDust && heightTemp >= 0.15F) {
							if (j1 != 0) {
								if (j1 >= 0) {
									tess.draw();
								}

								j1 = 0;
								renderer.mc.getTextureManager().bindTexture(locationRainPng);
								worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
							}

							double d5 = ((double) (renderer.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971
									+ k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D
									* (3.0D + random.nextDouble());
							double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
							double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
							float f3 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float) b0;
							float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * rainStrength;
							mutable.set(l1, i3, k1);
							int j3 = world.getCombinedLight(mutable, 0);
							int k3 = j3 >> 16 & 65535;
							int l3 = j3 & 65535;
							worldrenderer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D)
									.tex(0.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D)
									.tex(1.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D)
									.tex(1.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D)
									.tex(0.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
						} else {
							if (j1 != 1) {
								if (j1 >= 0) {
									tess.draw();
								}

								// If cold enough the dust texture will be
								// snow that blows sideways
								ResourceLocation texture = locationSnowPng;
								if (hasDust && heightTemp >= 0.15F)
									texture = locationDustPng;

								j1 = 1;
								renderer.mc.getTextureManager().bindTexture(texture);
								worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
							}

							double d8 = (double) (((float) (renderer.rendererUpdateCount & 511) + partialTicks)
									/ 512.0F);
							// The 0.2F factor was originally 0.01F. It
							// affects the horizontal
							// movement of particles, which works well for
							// dust.
							final float factor = hasDust ? 0.2F : 0.01F;
							double d9 = random.nextDouble()
									+ (double) f1 * factor * (double) ((float) random.nextGaussian());
							double d10 = random.nextDouble() + (double) (f1 * (float) random.nextGaussian()) * 0.001D;
							double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
							double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
							float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float) b0;
							float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * rainStrength;
							mutable.set(l1, i3, k1);
							int i4 = (world.getCombinedLight(mutable, 0) * 3 + 15728880) / 4;
							int j4 = i4 >> 16 & 65535;
							int k4 = i4 & 65535;
							worldrenderer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D)
									.tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
									.lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D)
									.tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
									.lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D)
									.tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
									.lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D)
									.tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5)
									.lightmap(j4, k4).endVertex();
						}
					}
				}
			}
		}

		if (j1 >= 0) {
			tess.draw();
		}

		worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		renderer.disableLightmap();
	}
}