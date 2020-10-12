package com.raymond.imageanalayze.Model;

public class OpenCVImage {
    private String image;
    private String name;
    private String date;
    private String count;
    private String totalArea;
    private String avgSize;
    private String areaPercent;
    private int id;

    public OpenCVImage(String image, String name, String date, String count, String totalArea, String avgSize, String areaPercent) {
        this.image = image;
        this.name = name;
        this.date = date;
        this.count = count;
        this.totalArea = totalArea;
        this.avgSize = avgSize;
        this.areaPercent = areaPercent;
    }

    public OpenCVImage(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public OpenCVImage(String image, String name, String date) {
        this.image = image;
        this.name = name;
        this.date = date;
    }

    public OpenCVImage() {

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(String totalArea) {
        this.totalArea = totalArea;
    }

    public String getAvgSize() {
        return avgSize;
    }

    public void setAvgSize(String avgSize) {
        this.avgSize = avgSize;
    }

    public String getAreaPercent() {
        return areaPercent;
    }

    public void setAreaPercent(String areaPercent) {
        this.areaPercent = areaPercent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
