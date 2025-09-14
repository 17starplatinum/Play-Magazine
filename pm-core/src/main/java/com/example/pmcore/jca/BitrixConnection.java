package com.example.pmcore.jca;

public interface BitrixConnection {
    String callApi(String endpoint, String jsonBody) throws Exception;
    void close();
    boolean isClosed();
}