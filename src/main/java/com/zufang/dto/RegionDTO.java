package com.zufang.dto;

import lombok.Data;

import java.util.List;

/**
 * 地区DTO，用于前端显示和传输
 */
@Data
public class RegionDTO {
    private Long id; // ID
    private String name; // 名称
    private String code; // 代码
    private Long parentId; // 父级ID（省份的父级ID为null）
    private List<RegionDTO> children; // 子地区列表（省份的子地区为城市列表）
} 