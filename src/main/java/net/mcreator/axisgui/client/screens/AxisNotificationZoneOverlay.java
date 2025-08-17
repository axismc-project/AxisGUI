package net.mcreator.axisgui.client.screens;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.axisgui.websocket.ZoneNotificationManager;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@EventBusSubscriber({Dist.CLIENT})
public class AxisNotificationZoneOverlay {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		
		// Mettre à jour l'animation
		ZoneNotificationManager.updateAnimation();
		
		// Vérifier si on doit afficher la notification
		if (!ZoneNotificationManager.shouldShowNotification()) {
			return;
		}
		
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		// Obtenir la position animée
		int animatedY = ZoneNotificationManager.getAnimatedY();
		String zoneName = ZoneNotificationManager.getCurrentZoneName();
		
		// Dessiner la texture de notification
		event.getGuiGraphics().blit(new ResourceLocation("axis_gui:textures/screens/axisnotif.png"), 
			w - 222, animatedY, 0, 0, 225, 44, 225, 44);
		
		// Dessiner "Welcome to" en haut
		event.getGuiGraphics().drawString(Minecraft.getInstance().font, 
			Component.literal("Welcome to"), 
			w - 160, animatedY + 8, 
			0xFFD700, false); // Couleur dorée
		
		// Dessiner le nom de la zone centré
		int textWidth = Minecraft.getInstance().font.width(zoneName);
		event.getGuiGraphics().drawString(Minecraft.getInstance().font, 
			Component.literal(zoneName), 
			w - 160 + (128 - textWidth) / 2, animatedY + 22, 
			0xFFFFFF, false); // Couleur blanche
		
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}