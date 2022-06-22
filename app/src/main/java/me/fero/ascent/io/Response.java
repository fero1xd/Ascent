package me.fero.ascent.io;

public class Response {
    private final String response;
    private final int code;

    public Response(int code , String response){
        this.response = response;
        this.code = code;
    }

    public String getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }
}
