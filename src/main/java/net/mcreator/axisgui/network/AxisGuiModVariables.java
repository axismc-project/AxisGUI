package net.mcreator.axisgui.network;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.mcreator.axisgui.AxisGuiMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AxisGuiModVariables {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AxisGuiMod.MODID);
	public static String websocketUrl = "\"wss://api.neant.world/ws/zones?api_key=mk_live_admin_bootstrap_key_12345678901234567890123456789012\"";
	public static double notificationDuration = 5.0;

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
	}
}
