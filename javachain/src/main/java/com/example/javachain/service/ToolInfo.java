package com.example.javachain.service;

import java.util.List;

/**
 * 工具信息记录类
 */
public record ToolInfo(String name, String description, List<String> parameters, String example) {

}
