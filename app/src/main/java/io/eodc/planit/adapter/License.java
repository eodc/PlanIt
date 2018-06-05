package io.eodc.planit.adapter;

public class License {
    private int     mYear;
    private String  mName;
    private String  mAuthor;
    private String  mLicenseUrl;
    private String  mProjectUrl;

    public License(String name, int year, String author, String licenseUrl, String projectUrl) {
        this.mName = name;
        this.mYear = year;
        this.mAuthor = author;
        this.mLicenseUrl = licenseUrl;
        this.mProjectUrl = projectUrl;
    }

    public String getName() {
        return mName;
    }

    public int getYear() {
        return mYear;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getLicenseUrl() {
        return mLicenseUrl;
    }

    public String getProjectUrl() {
        return mProjectUrl;
    }
}
