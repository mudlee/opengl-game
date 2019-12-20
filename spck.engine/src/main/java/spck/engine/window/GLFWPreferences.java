package spck.engine.window;

public class GLFWPreferences {
    private Antialiasing antialiasing;
    private boolean fullscreen;
    private int width;
    private int height;
    private String title;
    private boolean vsync;
    private boolean limitFps;

    private GLFWPreferences() {
    }

    @Override
    public String toString() {
        return "GLFWPreferences{" +
                "antialiasing=" + antialiasing +
                ", fullscreen=" + fullscreen +
                ", width=" + width +
                ", height=" + height +
                ", title='" + title + '\'' +
                ", vsync=" + vsync +
                ", limitFps=" + limitFps +
                '}';
    }

    public Antialiasing getAntialiasing() {
        return antialiasing;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVsync() {
        return vsync;
    }

    public boolean isLimitFps() {
        return limitFps;
    }

    public static final class Builder {
        private Antialiasing antialiasing = Antialiasing.OFF;
        private boolean fullscreen;
        private int width = 1024;
        private int height = 768;
        private String title = "SPCK";
        private boolean vsync = false;
        private boolean limitFps = false;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder withAntialiasing(Antialiasing antialiasing) {
            this.antialiasing = antialiasing;
            return this;
        }

        public Builder withFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withVsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public Builder withLimitFps(boolean limitFps) {
            this.limitFps = limitFps;
            return this;
        }

        public GLFWPreferences build() {
            GLFWPreferences gLFWPreferences = new GLFWPreferences();
            gLFWPreferences.fullscreen = this.fullscreen;
            gLFWPreferences.title = this.title;
            gLFWPreferences.height = this.height;
            gLFWPreferences.width = this.width;
            gLFWPreferences.limitFps = this.limitFps;
            gLFWPreferences.vsync = this.vsync;
            gLFWPreferences.antialiasing = this.antialiasing;
            return gLFWPreferences;
        }
    }
}
