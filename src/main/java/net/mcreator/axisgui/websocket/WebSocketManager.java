package net.mcreator.axisgui.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mcreator.axisgui.AxisGuiMod;
import net.mcreator.axisgui.network.AxisGuiModVariables;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static WebSocketClient client;
    private static boolean connected = false;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static void initialize() {
        // Initialiser l'URL par défaut si vide
        if (AxisGuiModVariables.websocketUrl == null || AxisGuiModVariables.websocketUrl.isEmpty()) {
            AxisGuiModVariables.websocketUrl = "wss://api.neant.world/ws/zones?api_key=mk_live_admin_bootstrap";
        }
        
        // Initialiser la durée par défaut si 0
        if (AxisGuiModVariables.notificationDuration == 0) {
            AxisGuiModVariables.notificationDuration = 5; // 5 secondes par défaut
        }
        
        // Initialiser l'UUID override si vide (optionnel)
        if (AxisGuiModVariables.playerUuidOverride == null) {
            AxisGuiModVariables.playerUuidOverride = "";
        }
        
        connect();
        
        // Reconnexion automatique toutes les 30 secondes si déconnecté
        scheduler.scheduleAtFixedRate(() -> {
            if (!isConnected()) {
                AxisGuiMod.LOGGER.info("WebSocket déconnecté, tentative de reconnexion...");
                connect();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private static void connect() {
        try {
            String url = AxisGuiModVariables.websocketUrl;
            URI serverUri = new URI(url);
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    AxisGuiMod.LOGGER.info("WebSocket connecté au serveur: " + url);
                    
                    // Afficher les UUIDs pour debug
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        String mojangUuid = mc.player.getUUID().toString();
                        String serverUuid = getEffectivePlayerUuid();
                        AxisGuiMod.LOGGER.info("UUID Mojang: " + mojangUuid);
                        AxisGuiMod.LOGGER.info("UUID utilisé pour matching: " + serverUuid);
                    }
                }
                
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    AxisGuiMod.LOGGER.warn("WebSocket fermé: " + reason);
                }
                
                @Override
                public void onError(Exception ex) {
                    connected = false;
                    AxisGuiMod.LOGGER.error("Erreur WebSocket: ", ex);
                }
            };
            
            client.connect();
        } catch (Exception e) {
            connected = false;
            AxisGuiMod.LOGGER.error("Erreur lors de la connexion WebSocket: ", e);
        }
    }
    
    private static String getEffectivePlayerUuid() {
        // Si un UUID override est défini, l'utiliser, sinon utiliser l'UUID Mojang
        if (AxisGuiModVariables.playerUuidOverride != null && !AxisGuiModVariables.playerUuidOverride.isEmpty()) {
            return AxisGuiModVariables.playerUuidOverride;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return mc.player.getUUID().toString();
        }
        
        return "";
    }
    
    private static void handleMessage(String message) {
        try {
            AxisGuiMod.LOGGER.info("Message WebSocket reçu: " + message);
            
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            
            if (json.has("type") && "zone_event".equals(json.get("type").getAsString())) {
                JsonObject data = json.getAsJsonObject("data");
                String action = data.get("action").getAsString();
                String zoneName = data.get("zoneName").getAsString();
                String messageUuid = data.get("playerUuid").getAsString();
                
                AxisGuiMod.LOGGER.info("Zone event - Action: " + action + ", Zone: " + zoneName + ", UUID: " + messageUuid);
                
                // Utiliser l'UUID effectif pour la comparaison
                String effectiveUuid = getEffectivePlayerUuid();
                
                AxisGuiMod.LOGGER.info("Comparaison UUID - Message: " + messageUuid + " vs Effectif: " + effectiveUuid);
                
                if (effectiveUuid.equals(messageUuid)) {
                    // Exécuter sur le thread principal de Minecraft
                    Minecraft.getInstance().execute(() -> {
                        if ("enter".equals(action)) {
                            ZoneNotificationManager.showZoneNotification(zoneName);
                            AxisGuiMod.LOGGER.info("✓ Notification affichée pour l'entrée dans la zone: " + zoneName);
                        } else if ("leave".equals(action)) {
                            AxisGuiMod.LOGGER.info("✓ Sortie de la zone: " + zoneName);
                            // On peut ignorer les "leave" pour l'instant ou ajouter une notification de sortie
                        }
                    });
                } else {
                    AxisGuiMod.LOGGER.info("✗ UUID ne correspond pas - Notification ignorée");
                }
            }
        } catch (Exception e) {
            AxisGuiMod.LOGGER.error("Erreur lors du parsing du message WebSocket: ", e);
        }
    }
    
    public static boolean isConnected() {
        return connected && client != null && !client.isClosed();
    }
    
    public static void disconnect() {
        if (client != null) {
            client.close();
        }
        connected = false;
        scheduler.shutdown();
    }
    
    public static double getNotificationDuration() {
        return AxisGuiModVariables.notificationDuration;
    }
    
    public static String getWebSocketUrl() {
        return AxisGuiModVariables.websocketUrl;
    }
    
    public static String getPlayerUuidOverride() {
        return AxisGuiModVariables.playerUuidOverride;
    }
}