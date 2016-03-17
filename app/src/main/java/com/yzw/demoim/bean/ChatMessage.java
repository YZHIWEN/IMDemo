package com.yzw.demoim.bean;

/**
 * Created by yzw on 2016/3/17 0017.
 */
public class ChatMessage {

    private Type type;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", body='" + body + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

    private String body;
    private String from;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public enum Type {
        SEND, RECEIVE
    }
}
