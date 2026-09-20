package app.mobilepilot.two;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/** Receives commands silently while ChatGPT stays in the foreground. */
public final class PilotMessagingService extends FirebaseMessagingService {
    @Override public void onMessageReceived(RemoteMessage message) {
        String command = message.getData().get("command");
        String requestId = message.getData().get("request_id");
        if (command == null || command.trim().isEmpty()) return;
        if (!CommandReplayGuard.accept(this, requestId)) return;
        CommandRouter.execute(this, command);
    }

    @Override public void onNewToken(String token) {
        getSharedPreferences("pilot", MODE_PRIVATE).edit()
            .putString("fcm_token", token)
            .putBoolean("token_pending_upload", true)
            .apply();
    }
}
