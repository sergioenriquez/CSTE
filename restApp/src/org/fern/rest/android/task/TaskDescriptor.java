package org.fern.rest.android.task;

import java.io.Serializable;

public class TaskDescriptor implements Serializable {
    private String            key;
    private String            header;
    private String            content;
    /**
     * 
     */
    private static final long serialVersionUID = -8299239757791964947L;

    public TaskDescriptor(String key, String header, String content) {
        this.key = key;
        this.header = header;
        this.content = content;
    }

    public TaskDescriptor(String key, String content) {
        this.key = key;
        this.header = key;
        this.content = content;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
