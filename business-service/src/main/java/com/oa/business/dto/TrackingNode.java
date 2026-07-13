package com.oa.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 物流轨迹节点（含坐标）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackingNode {
    private String time;
    private String desc;
    private String location;
    private Double lat;
    private Double lng;

    public TrackingNode() {
    }

    public TrackingNode(String time, String desc, String location) {
        this.time = time;
        this.desc = desc;
        this.location = location;
    }

    public TrackingNode(String time, String desc, String location, Double lat, Double lng) {
        this.time = time;
        this.desc = desc;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
