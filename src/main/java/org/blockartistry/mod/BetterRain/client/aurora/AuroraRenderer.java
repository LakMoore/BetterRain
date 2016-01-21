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

package org.blockartistry.mod.BetterRain.client.aurora;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.blockartistry.mod.BetterRain.ModOptions;
import org.blockartistry.mod.BetterRain.client.AuroraEffectHandler;
import org.blockartistry.mod.BetterRain.client.IAtmosRenderer;
import org.blockartistry.mod.BetterRain.util.Color;
import org.blockartistry.mod.BetterRain.util.WorldUtils;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class AuroraRenderer implements IAtmosRenderer {

	private static final boolean ANIMATE = ModOptions.getAuroraAnimate();
	private static final boolean HEIGHT_PLAYER_RELATIVE = ModOptions.getAuroraHeightPlayerRelative();
	private static final float PLAYER_FIXED_HEIGHT = ModOptions.getPlayerFixedHeight();

	@Override
	public void render(final EntityRenderer renderer, final float partialTick) {
		if (AuroraEffectHandler.currentAurora != null) {
			renderAurora(partialTick, AuroraEffectHandler.currentAurora);
		}
	}

	public static float moonlightFactor(final World world) {
		final float moonFactor = 1.0F - WorldUtils.getMoonPhaseFactor(world) * 1.1F;
		if(moonFactor <= 0.0F)
			return 0.0F;
		return MathHelper.clamp_float(moonFactor * moonFactor, 0.0F, 1.0F);
	}

	public static void renderAurora(final float partialTick, final Aurora aurora) {

		final Minecraft mc = FMLClientHandler.instance().getClient();
		final float alpha = (aurora.getAlpha() * moonlightFactor(mc.theWorld)) / 255.0F;
		if (alpha <= 0.0F)
			return;

		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer renderer = tess.getWorldRenderer();
		final float tranY;
		if (HEIGHT_PLAYER_RELATIVE) {
			// Fix height above player
			tranY = PLAYER_FIXED_HEIGHT;
		} else {
			// Adjust to keep aurora at the same altitude
			tranY = WorldUtils.getCloudHeight(mc.theWorld) + 5 - (float) (mc.thePlayer.lastTickPosY
					+ (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTick);
		}

		final double tranX = aurora.posX
				- (mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTick);

		final double tranZ = aurora.posZ
				- (mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTick);

		if (ANIMATE)
			aurora.translate(partialTick);

		final Color base = aurora.getBaseColor();
		final Color fade = aurora.getFadeColor();
		final double zero = 0.0D;

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) tranX, tranY, (float) tranZ);
		GlStateManager.scale(0.5D, 8.0D, 0.5D);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);

		for (final Node[] array : aurora.getNodeList()) {
			for (int i = 0; i < array.length - 1; i++) {

				final Node node = array[i];

				final double posY = node.getModdedY();
				final double posX = node.tetX;
				final double posZ = node.tetZ;
				final double tetX = node.tetX2;
				final double tetZ = node.tetZ2;

				final double posX2;
				final double posZ2;
				final double tetX2;
				final double tetZ2;
				final double posY2;

				if (i < array.length - 2) {
					final Node nodePlus = array[i + 1];
					posX2 = nodePlus.tetX;
					posZ2 = nodePlus.tetZ;
					tetX2 = nodePlus.tetX2;
					tetZ2 = nodePlus.tetZ2;
					posY2 = nodePlus.getModdedY();
				} else {
					posX2 = tetX2 = node.posX;
					posZ2 = tetZ2 = node.getModdedZ();
					posY2 = 0.0D;
				}

				// Front
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, posY, posZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();

				// Bottom
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();

				// Back
				renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, posY, tetZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				tess.draw();
			}
		}

		GlStateManager.scale(3.5D, 25.0D, 3.5D);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
}
