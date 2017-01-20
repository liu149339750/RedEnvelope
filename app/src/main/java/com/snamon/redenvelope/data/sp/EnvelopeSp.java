package com.snamon.redenvelope.data.sp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.snamon.redenvelope.common.Constants;

/**
 * sp数据操作类.
 */
public class EnvelopeSp extends AbstractSp {

    //是否第一次进入 .
    public final String IS_FIRST_ACCESS = "first_access";

    public EnvelopeSp(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getSpName() {
        return Constants.SP_NAME;
    }

    public void setFirstAccess(boolean isFirstAccess){
        syncPut(IS_FIRST_ACCESS , isFirstAccess);
    }

    public boolean getFirstAccess(){
        return (boolean) get(IS_FIRST_ACCESS , true);
    }
}
