package com.mfw.atlas.provider.exceptions;

import com.mfw.atlas.client.constants.GlobalCodeEnum;

public class BusinessException extends RuntimeException {

    private Integer code;

    private String msg;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(GlobalCodeEnum globalCode) {
        super(globalCode.getDesc());
        this.code = globalCode.getCode();
        this.msg = globalCode.getDesc();
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
