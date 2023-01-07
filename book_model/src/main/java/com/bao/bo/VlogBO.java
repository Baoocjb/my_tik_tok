package com.bao.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {
    private String id;
    @NotBlank(message = "作者id不能为空!")
    private String vlogerId;
    @NotBlank(message = "视频地址不能为空!")
    private String url;
    @NotBlank(message = "视频封面地址不能为空!")
    private String cover;
    @NotBlank(message = "视频标题不能为空!")
    private String title;
    @NotNull(message = "视频宽度不能为空!")
    private Integer width;
    @NotNull(message = "视频高度不能为空!")
    private Integer height;

    private Integer likeCounts;
    private Integer commentsCounts;

}
