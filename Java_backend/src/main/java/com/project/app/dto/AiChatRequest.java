package com.project.app.dto;

import java.util.ArrayList;
import java.util.List;

public class AiChatRequest {
    private String message;
    private List<ChatMessageDto> prevMsg = new ArrayList<>();
    private Boolean isLoggedIn;
    private String currentPage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDto> getPrevMsg() {
        return prevMsg;
    }

    public void setPrevMsg(List<ChatMessageDto> prevMsg) {
        this.prevMsg = prevMsg;
    }

    public Boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}
