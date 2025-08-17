package net.mcreator.axisgui.websocket;

public class ZoneNotificationManager {
    private static boolean showNotification = false;
    private static String currentZoneName = "";
    private static long animationStartTime = 0;
    private static long lastNotificationTime = 0;
    private static final long COOLDOWN_MS = 10000; // 10 secondes
    private static final int NOTIFICATION_HEIGHT = 44;
    
    // Phases d'animation
    private static final long SLIDE_IN_DURATION = 500; // 0.5 seconde pour descendre
    private static final long SLIDE_OUT_DURATION = 500; // 0.5 seconde pour remonter
    
    public static void showZoneNotification(String zoneName) {
        long currentTime = System.currentTimeMillis();
        
        // Vérifier le cooldown
        if (currentTime - lastNotificationTime < COOLDOWN_MS) {
            return; // Ignorer si en cooldown
        }
        
        currentZoneName = zoneName;
        showNotification = true;
        animationStartTime = currentTime;
        lastNotificationTime = currentTime;
    }
    
    public static void updateAnimation() {
        if (!showNotification) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animationStartTime;
        
        // Utiliser la durée depuis les variables MCreator
        long showDurationMs = (long)(WebSocketManager.getNotificationDuration() * 1000);
        long totalAnimationMs = showDurationMs + SLIDE_IN_DURATION + SLIDE_OUT_DURATION;
        
        // Arrêter l'animation après la durée totale
        if (elapsed >= totalAnimationMs) {
            showNotification = false;
        }
    }
    
    public static boolean shouldShowNotification() {
        return showNotification;
    }
    
    public static int getAnimatedY() {
        if (!showNotification) {
            return -NOTIFICATION_HEIGHT; // Hors de l'écran
        }
        
        long elapsed = System.currentTimeMillis() - animationStartTime;
        long showDurationMs = (long)(WebSocketManager.getNotificationDuration() * 1000);
        long slideOutStart = SLIDE_IN_DURATION + showDurationMs;
        
        if (elapsed <= SLIDE_IN_DURATION) {
            // Phase 1: Descendre depuis le haut
            float progress = (float) elapsed / SLIDE_IN_DURATION;
            return (int) ((-NOTIFICATION_HEIGHT) + (progress * (NOTIFICATION_HEIGHT + 3)));
        } else if (elapsed <= slideOutStart) {
            // Phase 2: Rester en position
            return 3; // Position finale
        } else if (elapsed <= slideOutStart + SLIDE_OUT_DURATION) {
            // Phase 3: Remonter vers le haut
            float progress = (float) (elapsed - slideOutStart) / SLIDE_OUT_DURATION;
            return (int) (3 - (progress * (NOTIFICATION_HEIGHT + 3)));
        } else {
            // Animation terminée
            return -NOTIFICATION_HEIGHT;
        }
    }
    
    public static String getCurrentZoneName() {
        return currentZoneName;
    }
    
    // Méthode pour tester la notification (utilisable avec une commande)
    public static void testNotification() {
        showZoneNotification("Test Zone");
    }
}