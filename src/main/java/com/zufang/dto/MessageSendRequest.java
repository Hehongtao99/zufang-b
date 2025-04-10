package com.zufang.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel(description = "发送消息请求")
public class MessageSendRequest {

    @NotBlank(message = "消息标题不能为空")
    @Size(max = 100, message = "消息标题最多100个字符")
    @ApiModelProperty(value = "消息标题", required = true)
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @ApiModelProperty(value = "消息内容", required = true)
    private String content;

    @ApiModelProperty(value = "接收者类型：ALL-所有用户, TENANT-租客, LANDLORD-房东", required = true)
    private String receiverType = "ALL";
} 