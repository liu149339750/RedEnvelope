package com.fcbox.rxbus;

/**
 * 事件处理线程模式 .
 */

public enum ThreadMode {

    /**
     * 事件处理线程将在事件发送的线程上 .
     */
    POSTING,

    /**
     * 不管事件发送为何线程，事件处理都将在UI线程处理 .
     */
    MAIN,

    /**
     * 如事件发送为UI线程则事件处理为子线程，否则在其发送线程订阅　．
     */
    BACKGROUND,

    /**
     * 不管事件发送为何线程，事件处理都将在子线程处理 .
     */
    ASYNC
}
