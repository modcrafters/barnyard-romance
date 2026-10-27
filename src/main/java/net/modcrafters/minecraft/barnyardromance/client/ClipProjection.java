package net.modcrafters.minecraft.barnyardromance.client;

import java.util.Optional;

public final class ClipProjection {
    private ClipProjection() {
    }

    public static Optional<ScreenPoint> toScreen(float clipX, float clipY, float clipW, int width, int height) {
        if (!(clipW > 0.0f) || width <= 0 || height <= 0) {
            return Optional.empty();
        }
        float ndcX = clipX / clipW;
        float ndcY = clipY / clipW;
        if (ndcX < -1.0f || ndcX > 1.0f || ndcY < -1.0f || ndcY > 1.0f) {
            return Optional.empty();
        }
        int x = Math.round((ndcX * 0.5f + 0.5f) * width);
        int y = Math.round((1.0f - (ndcY * 0.5f + 0.5f)) * height);
        return Optional.of(new ScreenPoint(x, y));
    }
}
