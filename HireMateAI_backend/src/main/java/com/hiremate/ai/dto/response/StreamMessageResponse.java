package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamMessageResponse {

    private String content;

    private Boolean done;
}
