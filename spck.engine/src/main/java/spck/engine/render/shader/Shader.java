package spck.engine.render.shader;

import spck.engine.render.Material;

public interface Shader {
    void init();

    void startShader(Material material);

    void stopShader();
}
