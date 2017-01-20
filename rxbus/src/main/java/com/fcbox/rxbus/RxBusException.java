package com.fcbox.rxbus;

/**
 * RxBus异常类 .
 */

public class RxBusException extends RuntimeException{
    private static final long serialVersionUID = -2912559384646531479L;

    public RxBusException(String detailMessage) {
        super(detailMessage);
    }

}
