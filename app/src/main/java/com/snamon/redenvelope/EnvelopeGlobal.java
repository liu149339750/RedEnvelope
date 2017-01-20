package com.snamon.redenvelope;

import android.content.Context;
import android.support.annotation.NonNull;

import com.snamon.redenvelope.data.sp.EnvelopeSp;

/**
 * @author snamon 2017-01-19
 * 全局控制类(主要对数据的初始化操作 ) .
 */
public class EnvelopeGlobal {

    private static EnvelopeSp sEnvelopeSp;

    public static void init(@NonNull Context context){
        sEnvelopeSp = new EnvelopeSp(context);
    }

    @NonNull
    public static EnvelopeSp getSp() {
        if (sEnvelopeSp == null) {
            throw new IllegalStateException("must invoke method initPersistent() first");
        }
        return sEnvelopeSp;
    }

}
