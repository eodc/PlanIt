package io.eodc.planit.adapter;

public class License {
    private String name;
    private int year;
    private String author;
    private String licenseUrl;
    private String projectUrl;

    public License(String name, int year, String author, String licenseUrl, String projectUrl) {
        this.name = name;
        this.year = year;
        this.author = author;
        this.licenseUrl = licenseUrl;
        this.projectUrl = projectUrl;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public String getAuthor() {
        return author;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }
}
