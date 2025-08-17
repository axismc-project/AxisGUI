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
            AxisGuiModVariables.websocketUrl = "ws://localhost:8080";
        }
        
        // Initialiser la durée par défaut si 0
        if (AxisGuiModVariables.notificationDuration == 0) {
            AxisGuiModVariables.notificationDuration = 3; // 3 secondes par défaut
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
            URI serverUri = new URI(AxisGuiModVariables.websocketUrl);
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    AxisGuiMod.LOGGER.info("WebSocket connecté au serveur: " + AxisGuiModVariables.websocketUrl);
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
    
    private static void handleMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            
            if (json.has("type") && "zone_event".equals(json.get("type").getAsString())) {
                JsonObject data = json.getAsJsonObject("data");
                String action = data.get("action").getAsString();
                String zoneName = data.get("zoneName").getAsString();
                
                // Vérifier que c'est notre joueur
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    String playerUuid = mc.player.getUUID().toString();
                    String messageUuid = data.get("playerUuid").getAsString();
                    
                    if (playerUuid.equals(messageUuid)) {
                        // Exécuter sur le thread principal de Minecraft
                        mc.execute(() -> {
                            if ("enter".equals(action)) {
                                ZoneNotificationManager.showZoneNotification(zoneName);
                                AxisGuiMod.LOGGER.info("Entrée dans la zone: " + zoneName);
                            }
                            // On peut ignorer les "leave" pour l'instant
                        });
                    }
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
}