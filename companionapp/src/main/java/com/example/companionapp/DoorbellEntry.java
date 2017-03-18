package com.example.companionapp;

import java.util.Map;

/**
 * Created by onuchinx on 18/03/2017.
 */

public class DoorbellEntry {
    Long timestamp;
    String image;
    Map<String, Float> annotations;

    public DoorbellEntry() {
    }

    public DoorbellEntry(Long timestamp, String image, Map<String, Float> annotations) {
        this.timestamp = timestamp;
        this.image = image;
        this.annotations = annotations;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public Map<String, Float> getAnnotations() {
        return annotations;
    }

}
