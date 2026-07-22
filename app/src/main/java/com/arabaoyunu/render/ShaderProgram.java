package com.arabaoyunu.render;

import android.opengl.GLES20;

public final class ShaderProgram {

    public int program;
    public int aPosition;
    public int uMvp;
    public int uColor;

    public void create() {
        String vertex =
                "uniform mat4 uMVP;" +
                "attribute vec3 aPosition;" +
                "void main(){" +
                "  gl_Position = uMVP * vec4(aPosition, 1.0);" +
                "}";
        String fragment =
                "precision mediump float;" +
                "uniform vec4 uColor;" +
                "void main(){" +
                "  gl_FragColor = uColor;" +
                "}";
        int vs = compile(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);
        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Shader link hatasi: " + log);
        }
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        uMvp = GLES20.glGetUniformLocation(program, "uMVP");
        uColor = GLES20.glGetUniformLocation(program, "uColor");
        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
    }

    private int compile(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile hatasi: " + log);
        }
        return shader;
    }
}
