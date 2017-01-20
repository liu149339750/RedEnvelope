package com.snamon.redenvelope.event;

/**
 * 打开服务状态事件.
 */

public class OpenStatusEvent {
    public boolean isStop =false ;

    public OpenStatusEvent(boolean isStop) {
        this.isStop = isStop;
    }
}
