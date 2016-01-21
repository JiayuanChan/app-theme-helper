package com.kabouzeid.appthemehelper.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.WindowDecorActionBar;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.kabouzeid.appthemehelper.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class ToolbarUtil {

    public static void setToolbarContentColor(@NonNull Context context, @Nullable Toolbar toolbar, final @ColorInt int color, final @ColorInt int primaryTextColor, final @ColorInt int secondaryTextColor, final @ColorInt int menuWidgetColor) {
        setToolbarContentColor(context, toolbar, null, color, primaryTextColor, secondaryTextColor, menuWidgetColor);
    }

    @SuppressWarnings("unchecked")
    public static void setToolbarContentColor(@NonNull Context context, Toolbar toolbar, @Nullable Menu menu, final @ColorInt int toolbarContentColor, final @ColorInt int titleTextColor, final @ColorInt int subtitleTextColor, final @ColorInt int menuWidgetColor) {
        if (toolbar == null) return;

        if (menu == null) {
            menu = toolbar.getMenu();
        }

        toolbar.setTitleTextColor(titleTextColor);
        toolbar.setSubtitleTextColor(subtitleTextColor);

        if (toolbar.getNavigationIcon() != null) {
            // Tint the toolbar navigation icon (e.g. back, drawer, etc.)
            toolbar.setNavigationIcon(TintHelper.tintDrawable(toolbar.getNavigationIcon(), toolbarContentColor));
        }

        tintMenu(toolbar, menu, toolbarContentColor);

        if (context instanceof Activity) {
            setOverflowButtonColor((Activity) context, toolbarContentColor);
            try {
                // Tint immediate overflow menu items
                final Field menuField = Toolbar.class.getDeclaredField("mMenuBuilderCallback");
                menuField.setAccessible(true);
                final Field presenterField = Toolbar.class.getDeclaredField("mActionMenuPresenterCallback");
                presenterField.setAccessible(true);
                final Field menuViewField = Toolbar.class.getDeclaredField("mMenuView");
                menuViewField.setAccessible(true);
                final MenuPresenter.Callback currentPresenterCb = (MenuPresenter.Callback) presenterField.get(toolbar);
                if (!(currentPresenterCb instanceof ATHMenuPresenterCallback)) {
                    final ATHMenuPresenterCallback newPresenterCb = new ATHMenuPresenterCallback(
                            (Activity) context, menuWidgetColor, currentPresenterCb, toolbar);
                    final MenuBuilder.Callback currentMenuCb = (MenuBuilder.Callback) menuField.get(toolbar);
                    toolbar.setMenuCallbacks(newPresenterCb, currentMenuCb);
                    ActionMenuView menuView = (ActionMenuView) menuViewField.get(toolbar);
                    if (menuView != null)
                        menuView.setMenuCallbacks(newPresenterCb, currentMenuCb);
                }

                // OnMenuItemClickListener to tint submenu items
                final Field menuItemClickListener = Toolbar.class.getDeclaredField("mOnMenuItemClickListener");
                menuItemClickListener.setAccessible(true);
                Toolbar.OnMenuItemClickListener currentClickListener = (Toolbar.OnMenuItemClickListener) menuItemClickListener.get(toolbar);
                if (!(currentClickListener instanceof ATHOnMenuItemClickListener)) {
                    final ATHOnMenuItemClickListener newClickListener = new ATHOnMenuItemClickListener(
                            (Activity) context, menuWidgetColor, currentClickListener, toolbar);
                    toolbar.setOnMenuItemClickListener(newClickListener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ATHMenuPresenterCallback implements MenuPresenter.Callback {

        public ATHMenuPresenterCallback(Activity context, final @ColorInt int color, MenuPresenter.Callback parentCb, Toolbar toolbar) {
            mContext = context;
            mColor = color;
            mParentCb = parentCb;
            mToolbar = toolbar;
        }

        private Activity mContext;
        private int mColor;
        private MenuPresenter.Callback mParentCb;
        private Toolbar mToolbar;

        @Override
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (mParentCb != null)
                mParentCb.onCloseMenu(menu, allMenusAreClosing);
        }

        @Override
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            applyOverflowMenuTint(mContext, mToolbar, mColor);
            return mParentCb != null && mParentCb.onOpenSubMenu(subMenu);
        }
    }

    private static class ATHOnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        private Activity mContext;
        private int mColor;
        private Toolbar.OnMenuItemClickListener mParentListener;
        private Toolbar mToolbar;

        public ATHOnMenuItemClickListener(Activity context, final @ColorInt int color, Toolbar.OnMenuItemClickListener parentCb, Toolbar toolbar) {
            mContext = context;
            mColor = color;
            mParentListener = parentCb;
            mToolbar = toolbar;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            applyOverflowMenuTint(mContext, mToolbar, mColor);
            return mParentListener != null && mParentListener.onMenuItemClick(item);
        }
    }

    @SuppressWarnings("unchecked")
    private static void tintMenu(@NonNull Toolbar toolbar, @Nullable Menu menu, final @ColorInt int color) {
        try {
            final Field field = Toolbar.class.getDeclaredField("mCollapseIcon");
            field.setAccessible(true);
            Drawable collapseIcon = (Drawable) field.get(toolbar);
            if (collapseIcon != null) {
                field.set(toolbar, TintHelper.tintDrawable(collapseIcon, color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (menu != null && menu.size() > 0) {
            for (int i = 0; i < menu.size(); i++) {
                final MenuItem item = menu.getItem(i);
                if (item.getIcon() != null) {
                    item.setIcon(TintHelper.tintDrawable(item.getIcon(), color));
                }
                // Search view theming
                if (item.getActionView() != null && (item.getActionView() instanceof android.widget.SearchView || item.getActionView() instanceof android.support.v7.widget.SearchView)) {
                    SearchViewUtil.setSearchViewContentColor(item.getActionView(), color);
                }
            }
        }
    }

    public static void applyOverflowMenuTint(final @NonNull Activity activity, final Toolbar toolbar, final @ColorInt int color) {
        if (toolbar == null) return;
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                    f1.setAccessible(true);
                    ActionMenuView actionMenuView = (ActionMenuView) f1.get(toolbar);
                    Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                    f2.setAccessible(true);

                    // Actually ActionMenuPresenter
                    BaseMenuPresenter presenter = (BaseMenuPresenter) f2.get(actionMenuView);
                    Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                    f3.setAccessible(true);
                    MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                    setTintForMenuPopupHelper(activity, overflowMenuPopupHelper, color);

                    Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                    f4.setAccessible(true);
                    MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                    setTintForMenuPopupHelper(activity, subMenuPopupHelper, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void setTintForMenuPopupHelper(final @NonNull Activity context, @Nullable MenuPopupHelper menuPopupHelper, final @ColorInt int color) {
        if (menuPopupHelper != null) {
            final ListView listView = menuPopupHelper.getPopup().getListView();
            listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        Field checkboxField = ListMenuItemView.class.getDeclaredField("mCheckBox");
                        checkboxField.setAccessible(true);
                        Field radioButtonField = ListMenuItemView.class.getDeclaredField("mRadioButton");
                        radioButtonField.setAccessible(true);

                        final boolean isDark = !ColorUtil.isColorLight(ATHUtil.resolveColor(context, android.R.attr.windowBackground));

                        for (int i = 0; i < listView.getChildCount(); i++) {
                            View v = listView.getChildAt(i);
                            if (!(v instanceof ListMenuItemView)) continue;
                            ListMenuItemView iv = (ListMenuItemView) v;

                            CheckBox check = (CheckBox) checkboxField.get(iv);
                            if (check != null) {
                                TintHelper.setTint(check, color, isDark);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    check.setBackground(null);
                            }

                            RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                            if (radioButton != null) {
                                TintHelper.setTint(radioButton, color, isDark);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    radioButton.setBackground(null);
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        //noinspection deprecation
                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    @Nullable
    public static Toolbar getSupportActionBarView(@Nullable ActionBar ab) {
        if (ab == null || !(ab instanceof WindowDecorActionBar)) return null;
        try {
            WindowDecorActionBar decorAb = (WindowDecorActionBar) ab;
            Field field = WindowDecorActionBar.class.getDeclaredField("mDecorToolbar");
            field.setAccessible(true);
            ToolbarWidgetWrapper wrapper = (ToolbarWidgetWrapper) field.get(decorAb);
            field = ToolbarWidgetWrapper.class.getDeclaredField("mToolbar");
            field.setAccessible(true);
            return (Toolbar) field.get(wrapper);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to retrieve Toolbar from AppCompat support ActionBar: " + t.getMessage(), t);
        }
    }

    public static void setOverflowButtonColor(@NonNull Activity activity, final @ColorInt int color) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) return;
                final AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                TintHelper.tintDrawable(overflow.getDrawable(), color);
                ViewUtil.removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }

    public static final class SearchViewUtil {
        private static void tintImageView(Object target, Field field, final @ColorInt int color) throws Exception {
            field.setAccessible(true);
            final ImageView imageView = (ImageView) field.get(target);
            if (imageView.getDrawable() != null)
                imageView.setImageDrawable(TintHelper.tintDrawable(imageView.getDrawable(), color));
        }

        public static void setSearchViewContentColor(View searchView, final @ColorInt int color) {
            if (searchView == null) return;
            final Class<?> cls = searchView.getClass();
            try {
                final Field mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView");
                mSearchSrcTextViewField.setAccessible(true);
                final EditText mSearchSrcTextView = (EditText) mSearchSrcTextViewField.get(searchView);
                mSearchSrcTextView.setTextColor(color);
                mSearchSrcTextView.setHintTextColor(ColorUtil.adjustAlpha(color, 0.5f));
                TintHelper.setCursorTint(mSearchSrcTextView, color);

                Field field = cls.getDeclaredField("mSearchButton");
                tintImageView(searchView, field, color);
                field = cls.getDeclaredField("mGoButton");
                tintImageView(searchView, field, color);
                field = cls.getDeclaredField("mCloseButton");
                tintImageView(searchView, field, color);
                field = cls.getDeclaredField("mVoiceButton");
                tintImageView(searchView, field, color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private SearchViewUtil() {
        }
    }

    private ToolbarUtil() {
    }
}
