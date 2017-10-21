package com.example;

public class NeedItem {
    private final String needId;
    private final String title;
    private final String message;

    public NeedItem(String needId, String title, String message) {
        this.needId = needId;
        this.title = title;
        this.message = message;
    }

    public String getNeedId() {
        return needId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
    public String getImagePath() {
        return "/api/image/" + needId;
    }
}
