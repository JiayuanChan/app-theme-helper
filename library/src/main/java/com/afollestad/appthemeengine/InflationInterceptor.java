package com.afollestad.appthemeengine;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.appthemeengine.processors.DrawerLayoutProcessor;
import com.afollestad.appthemeengine.processors.NavigationViewProcessor;
import com.afollestad.appthemeengine.processors.NestedScrollViewProcessor;
import com.afollestad.appthemeengine.processors.RecyclerViewProcessor;
import com.afollestad.appthemeengine.processors.SearchViewProcessor;
import com.afollestad.appthemeengine.processors.TabLayoutProcessor;
import com.afollestad.appthemeengine.processors.ToolbarProcessor;
import com.afollestad.appthemeengine.processors.ViewPagerProcessor;
import com.afollestad.appthemeengine.views.ATEActionMenuItemView;
import com.afollestad.appthemeengine.views.ATECheckBox;
import com.afollestad.appthemeengine.views.ATECheckedTextView;
import com.afollestad.appthemeengine.views.ATECoordinatorLayout;
import com.afollestad.appthemeengine.views.ATEDrawerLayout;
import com.afollestad.appthemeengine.views.ATEEditText;
import com.afollestad.appthemeengine.views.ATEListView;
import com.afollestad.appthemeengine.views.ATENavigationView;
import com.afollestad.appthemeengine.views.ATENestedScrollView;
import com.afollestad.appthemeengine.views.ATEProgressBar;
import com.afollestad.appthemeengine.views.ATERadioButton;
import com.afollestad.appthemeengine.views.ATERecyclerView;
import com.afollestad.appthemeengine.views.ATEScrollView;
import com.afollestad.appthemeengine.views.ATESearchView;
import com.afollestad.appthemeengine.views.ATESeekBar;
import com.afollestad.appthemeengine.views.ATEStockSwitch;
import com.afollestad.appthemeengine.views.ATESwitch;
import com.afollestad.appthemeengine.views.ATETabLayout;
import com.afollestad.appthemeengine.views.ATEToolbar;
import com.afollestad.appthemeengine.views.ATEViewPager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Aidan Follestad (afollestad)
 */
class InflationInterceptor implements LayoutInflaterFactory {

    private static final boolean LOGGING_ENABLED = true;

    private static void LOG(String msg, Object... args) {
        //noinspection PointlessBooleanExpression
        if (!LOGGING_ENABLED)
            return;
        if (args != null) {
            Log.d("InflationInterceptor", String.format(msg, args));
        } else {
            Log.d("InflationInterceptor", msg);
        }
    }

    @Nullable
    private final ATEActivity mKeyContext;
    @NonNull
    private final LayoutInflater mLi;
    @Nullable
    private AppCompatDelegate mDelegate;
    private static Method mOnCreateViewMethod;
    private static Method mCreateViewMethod;
    private static Field mConstructorArgsField;
    private static int[] ATTRS_THEME;

    public InflationInterceptor(@Nullable Activity keyContext, @NonNull LayoutInflater li, @Nullable AppCompatDelegate delegate) {
        if (keyContext instanceof ATEActivity)
            mKeyContext = (ATEActivity) keyContext;
        else mKeyContext = null;

        mLi = li;
        mDelegate = delegate;
        if (mOnCreateViewMethod == null) {
            try {
                mOnCreateViewMethod = LayoutInflater.class.getDeclaredMethod("onCreateView",
                        View.class, String.class, AttributeSet.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to retrieve the onCreateView method.", e);
            }
        }
        if (mCreateViewMethod == null) {
            try {
                mCreateViewMethod = LayoutInflater.class.getDeclaredMethod("createView",
                        String.class, String.class, AttributeSet.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to retrieve the createView method.", e);
            }
        }
        if (mConstructorArgsField == null) {
            try {
                mConstructorArgsField = LayoutInflater.class.getDeclaredField("mConstructorArgs");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Failed to retrieve the mConstructorArgs field.", e);
            }
        }
        if (ATTRS_THEME == null) {
            try {
                final Field attrsThemeField = LayoutInflater.class.getDeclaredField("ATTRS_THEME");
                attrsThemeField.setAccessible(true);
                ATTRS_THEME = (int[]) attrsThemeField.get(null);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get the value of static field ATTRS_THEME.", t);
            }
        }
        mOnCreateViewMethod.setAccessible(true);
        mCreateViewMethod.setAccessible(true);
        mConstructorArgsField.setAccessible(true);
    }

    private boolean isBlackListedForApply(String name) {
        return name.equals("android.support.design.internal.NavigationMenuItemView") ||
                name.equals("ViewStub");
    }

    @Override
    public View onCreateView(View parent, final String name, Context context, AttributeSet attrs) {
        View view;

        switch (name) {
            case "EditText":
                view = new ATEEditText(context, attrs, mKeyContext);
                break;
            case "CheckBox":
                view = new ATECheckBox(context, attrs, mKeyContext);
                break;
            case "RadioButton":
                view = new ATERadioButton(context, attrs, mKeyContext);
                break;
            case "Switch":
                view = new ATEStockSwitch(context, attrs, mKeyContext);
                break;
            case "android.support.v7.widget.SwitchCompat":
                view = new ATESwitch(context, attrs, mKeyContext);
                break;
            case "SeekBar":
                view = new ATESeekBar(context, attrs, mKeyContext);
                break;
            case "ProgressBar":
                view = new ATEProgressBar(context, attrs, mKeyContext);
                break;
            case ToolbarProcessor.MAIN_CLASS:
                ATEToolbar toolbar = new ATEToolbar(context, attrs, mKeyContext);
                ATE.addPostInflationView(toolbar);
                view = toolbar;
                break;
            case "ListView":
                view = new ATEListView(context, attrs, mKeyContext);
                break;
            case "ScrollView":
                view = new ATEScrollView(context, attrs, mKeyContext);
                break;
            case RecyclerViewProcessor.MAIN_CLASS:
                view = new ATERecyclerView(context, attrs, mKeyContext);
                break;
            case NestedScrollViewProcessor.MAIN_CLASS:
                view = new ATENestedScrollView(context, attrs, mKeyContext);
                break;
            case DrawerLayoutProcessor.MAIN_CLASS:
                view = new ATEDrawerLayout(context, attrs, mKeyContext);
                break;
            case NavigationViewProcessor.MAIN_CLASS:
                view = new ATENavigationView(context, attrs, mKeyContext);
                break;
            case TabLayoutProcessor.MAIN_CLASS:
                view = new ATETabLayout(context, attrs, mKeyContext);
                break;
            case ViewPagerProcessor.MAIN_CLASS:
                view = new ATEViewPager(context, attrs, mKeyContext);
                break;
            case "android.support.design.widget.CoordinatorLayout":
                view = new ATECoordinatorLayout(context, attrs, mKeyContext);
                break;
            case "android.support.v7.view.menu.ActionMenuItemView":
                view = new ATEActionMenuItemView(context, attrs, mKeyContext);
                break;
            case SearchViewProcessor.MAIN_CLASS:
                view = new ATESearchView(context, attrs, mKeyContext);
                break;
            case "CheckedTextView":
                view = new ATECheckedTextView(context, attrs, mKeyContext);
                break;
            default: {
                // First, check if the AppCompatDelegate will give us a view, usually (maybe always) null.
                if (mDelegate != null) {
                    view = mDelegate.createView(parent, name, context, attrs);
                    if (view == null && mKeyContext != null)
                        view = mKeyContext.onCreateView(parent, name, context, attrs);
                    else view = null;
                } else {
                    view = null;
                }

                if (isBlackListedForApply(name))
                    return view;

                // Mimic code of LayoutInflater using reflection tricks (this would normally be run when this factory returns null).
                // We need to intercept the default behavior rather than allowing the LayoutInflater to handle it after this method returns.
                if (view == null) {
                    Context viewContext;
                    final boolean inheritContext = false; // TODO will this ever need to be true?
                    //noinspection PointlessBooleanExpression,ConstantConditions
                    if (parent != null && inheritContext) {
                        viewContext = parent.getContext();
                    } else {
                        viewContext = mLi.getContext();
                    }
                    // Apply a theme wrapper, if requested.
                    final TypedArray ta = viewContext.obtainStyledAttributes(attrs, ATTRS_THEME);
                    final int themeResId = ta.getResourceId(0, 0);
                    if (themeResId != 0) {
                        viewContext = new ContextThemeWrapper(viewContext, themeResId);
                    }
                    ta.recycle();

                    Object[] mConstructorArgs;
                    try {
                        mConstructorArgs = (Object[]) mConstructorArgsField.get(mLi);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to retrieve the mConstructorArgsField field.", e);
                    }

                    final Object lastContext = mConstructorArgs[0];
                    mConstructorArgs[0] = viewContext;
                    try {
                        if (-1 == name.indexOf('.')) {
                            view = (View) mOnCreateViewMethod.invoke(mLi, parent, name, attrs);
                        } else {
                            view = (View) mCreateViewMethod.invoke(mLi, name, null, attrs);
                        }
                    } catch (Exception e) {
                        LOG("Failed to inflate %s: %s", name, e.getMessage());
                        e.printStackTrace();
                    } finally {
                        mConstructorArgs[0] = lastContext;
                    }
                }

                if (view != null) {
                    if (view.getClass().getSimpleName().startsWith("ATE"))
                        return view;
                    String key = null;
                    if (context instanceof ATEActivity)
                        key = ((ATEActivity) context).getATEKey();
                    ATE.apply(view, key);
                }

                break;
            }
        }

        LOG("%s inflated to -> %s", name, view != null ? view.getClass().getName() : "(null)");
        return view;
    }
}