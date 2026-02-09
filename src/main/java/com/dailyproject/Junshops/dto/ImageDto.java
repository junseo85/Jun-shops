package com.dailyproject.Junshops.dto;

import lombok.Data;

@Data
public class ImageDto {
    private Long id;
    private String fileName;
    private String downloadUrl;

    public void setImageId(Long id) {
        this.id = id;
    }

    public void setImageName(String fileName) {
        this.fileName = fileName;
    }
}
