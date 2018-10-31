package spck.engine.render;

public interface Shader {
    void init();

    void startShader(Material material);

    void stopShader();
}
