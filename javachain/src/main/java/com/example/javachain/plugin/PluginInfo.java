package com.example.javachain.plugin;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 插件信息
 * 用于描述已加载的插件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfo {
    
    /** 插件名称 */
    private String name;
    
    /** 版本 */
    private String version;
    
    /** 描述 */
    private String description;
    
    /** 作者 */
    private String author;
    
    /** JAR 文件路径 */
    private String jarPath;
    
    /** 状态: loaded/unloaded */
    private String status;
    
    /** 加载时间 */
    private LocalDateTime loadedAt;
    
    /** 包含的 Server 列表 */
    private List<String> serverNames;
    
    /** 类加载器标识 */
    private String classLoaderId;
}