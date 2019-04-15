package com.diquest.disa4.module.common.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseModel implements Serializable {

    /** 생성자 ID */
    private Integer createId;

    /** 생성일 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /** 수정자 ID */
    private Integer modifyId;

    /** 수정일 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyDate;

}
