package com.lingxi.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 批量新增敏感词请求。
 */
@Data
public class SensitiveWordBatchCreateRequest {

    @NotEmpty(message = "请至少输入一个敏感词")
    @Size(max = 500, message = "单次最多添加 500 个敏感词")
    private List<@NotBlank(message = "敏感词不能为空") @Size(max = 64, message = "单个敏感词不能超过 64 个字符") String> words;

    @NotBlank(message = "请选择分类")
    @Size(max = 32, message = "分类长度不能超过 32 个字符")
    private String category;

    @NotBlank(message = "请选择级别")
    @Size(max = 8, message = "级别长度不能超过 8 个字符")
    private String level;
}
