package com.huizhuang.jianli.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/** 
* @ClassName: NoScrollListView 
* @Description:  不滚动ListView，全部显示数据，应用于少量数据
* @author lim 
* @mail limshare@gmail.com   
* @date 2014-11-4 下午1:09:14 
*  
*/
public class NoScrollListView extends ListView {   
   
    private boolean haveScrollbar = false;   
   
    public NoScrollListView(Context context) {   
        super(context);   
    }   
   
    public NoScrollListView(Context context, AttributeSet attrs) {   
        super(context, attrs);   
    }   
   
    public NoScrollListView(Context context, AttributeSet attrs, int defStyle) {   
        super(context, attrs, defStyle);   
    }

    /**  
     * 设置是否有ScrollBar，当要在ScollView中显示时，应当设置为false。 默认为 true  
     *   
     * @param haveScrollbar
     */   
    public void setHaveScrollbar(boolean haveScrollbar) {   
        this.haveScrollbar = haveScrollbar;   
    }   
   
    @Override   
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {   
        if (haveScrollbar == false) {   
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);   
            super.onMeasure(widthMeasureSpec, expandSpec);   
        } else {   
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);   
        }   
    }   
}   