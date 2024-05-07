package springboot;// Greeting.java

public class WSMessage {

    private String content;

    public WSMessage() {
    }

    public WSMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
