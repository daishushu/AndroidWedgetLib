/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huizhuang.jianli.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p/>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p/>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p/>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 */
public class SlidingTabLayout extends LinearLayout {



    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_PX = 1;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 2;
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFFF9690E;

    private static final int DEFAULT_DIVIDER_THICKNESS_DIPS = 0;
    private static final byte DEFAULT_DIVIDER_COLOR_ALPHA = 0x20;
    private static final float DEFAULT_DIVIDER_HEIGHT = 0f;
    private static final int LINE_OFFSET_PXS = 90;

    private final int mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    private final int mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;

    private final int mDefaultBottomBorderColor;

    private final Paint mDividerPaint;
    private final float mDividerHeight;

    private int mSelectedPosition;
    private float mSelectionOffset;

    private boolean mSmoothScroll = true;

    private SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;

    public void setSmoothScroll(boolean smoothScroll) {
        this.mSmoothScroll = smoothScroll;
    }

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 0;
    private static final int TAB_VIEW_PADDING_DIPS = 13;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 14;
    private static final int TAB_LINE_PADDING_LEFT = 10;
    private static final int TAB_LINE_PADDING_RIGHT = 10;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private int mBagdgeId;
    private ArrayList<String> mBadgeTxt;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
        final int themeForegroundColor = outValue.data;

        mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor,
                DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);
        mDefaultTabColorizer.setDividerColors(setColorAlpha(themeForegroundColor,
                DEFAULT_DIVIDER_COLOR_ALPHA));

        mBottomBorderThickness = DEFAULT_BOTTOM_BORDER_THICKNESS_PX;
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();

        mDividerHeight = DEFAULT_DIVIDER_HEIGHT;
        mDividerPaint = new Paint();
        mDividerPaint.setStrokeWidth((int) (DEFAULT_DIVIDER_THICKNESS_DIPS * density));
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     * <p/>
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        this.mCustomTabColorizer = tabColorizer;
        invalidate();
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setDividerColors(colors);
        invalidate();
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    public void setCustomTabViewWithBadge(int layoutResId, int textViewId, int badgeId, ArrayList<String> badgeTXT) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
        mBagdgeId = badgeId;
        mBadgeTxt = badgeTXT;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        removeAllViews();
        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private ColorStateList createSelector() {
        int stateSelected = android.R.attr.state_selected;
        int[][] state = {{stateSelected},{-stateSelected}};
        int[] color = {Color.parseColor("#f9690e"),Color.parseColor("#808080")};
        ColorStateList colorStateList = new ColorStateList(state,color);
        return colorStateList;
    }
    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(createSelector());
//        textView.setBackgroundColor(Color.WHITE);
//注释，不需要系统的点击变色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            // If we're running on Honeycomb or newer, then we can use the Theme's
//            // selectableItemBackground to ensure that the View has a pressed state
//            TypedValue outValue = new TypedValue();
//            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
//                    outValue, true);
//            textView.setBackgroundResource(outValue.resourceId);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = null;
            TextView tabTitleView = null;
            BadgeView mBadge =null;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, this,
                        false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
                if(mBagdgeId!=0) {
                    mBadge = (BadgeView) tabView.findViewById(mBagdgeId);
                    if (mBadge != null) {
                        mBadge.hide();
                    }
                }

            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }

            if(mBadge!=null&& mBadgeTxt!=null){
                if(mBadgeTxt.get(i)!=null && Double.valueOf(mBadgeTxt.get(i))>0) {
                    mBadge.setText(mBadgeTxt.get(i));
                    mBadge.show();
                }else{
                    mBadge.hide();
                }
            }

            tabTitleView.setText(adapter.getPageTitle(i));
            tabView.setOnClickListener(tabClickListener);

            addView(tabView,new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

//        if (mViewPager != null) {
//            scrollToTab(mViewPager.getCurrentItem(), 0);
//        }
    }

//    private void scrollToTab(int tabIndex, int positionOffset) {
//        final int tabStripChildCount = getChildCount();
//        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
//            return;
//        }
//
//        View selectedChild = getChildAt(tabIndex);
//        if (selectedChild != null) {
//            int targetScrollX = selectedChild.getLeft() + positionOffset;
//
//            if (tabIndex > 0 || positionOffset > 0) {
//                // If we're not at the first child and are mid-scroll, make sure we obey the offset
//                targetScrollX -= mTitleOffset;
//            }
//
//            scrollTo(targetScrollX, 0);
//        }
//    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = getChildAt(position);
//            int extraOffset = (selectedTitle != null)
//                    ? (int) (positionOffset * selectedTitle.getWidth())
//                    : 0;
//            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                onViewPagerPageChanged(position, 0f);
//                scrollToTab(position, 0);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }
    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < getChildCount(); i++) {
                if (v == getChildAt(i)) {
                    mViewPager.setCurrentItem(i,mSmoothScroll);
                    return;
                }
            }
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft() + LINE_OFFSET_PXS;
            int right = selectedTitle.getRight()- LINE_OFFSET_PXS;
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }

                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * (nextTitle.getLeft()+ LINE_OFFSET_PXS) +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * (nextTitle.getRight()- LINE_OFFSET_PXS) +
                        (1.0f - mSelectionOffset) * right);
            }

            mSelectedIndicatorPaint.setColor(color);

            canvas.drawRect(left, height - mSelectedIndicatorThickness, right,
                    height, mSelectedIndicatorPaint);
        }

        // Thin underline along the entire bottom edge
        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        // Vertical separators between the titles
        int separatorTop = (height - dividerHeightPx) / 2;
        for (int i = 0; i < childCount - 1; i++) {
            View child = getChildAt(i);
            mDividerPaint.setColor(tabColorizer.getDividerColor(i));
            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
                    separatorTop + dividerHeightPx, mDividerPaint);
        }
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if(i == mSelectedPosition){
                v.setSelected(true);
            }else{
                v.setSelected(false);
            }
        }
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;
        private int[] mDividerColors;

        @Override
        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        @Override
        public final int getDividerColor(int position) {
            return mDividerColors[position % mDividerColors.length];
        }

        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }

        void setDividerColors(int... colors) {
            mDividerColors = colors;
        }
    }

}
