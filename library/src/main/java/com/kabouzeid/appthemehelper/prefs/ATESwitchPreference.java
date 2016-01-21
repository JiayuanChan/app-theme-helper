package com.kabouzeid.appthemehelper.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.views.ATESwitch;

import java.lang.reflect.Field;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATESwitchPreference extends SwitchPreference {

    public ATESwitchPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ATESwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATESwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ATESwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private String mKey;
    private ATESwitch mSwitch;

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(com.kabouzeid.appthemehelper.R.layout.ate_preference_custom);
        setWidgetLayoutResource(com.kabouzeid.appthemehelper.R.layout.ate_preference_switch);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, com.kabouzeid.appthemehelper.R.styleable.ATESwitchPreference, 0, 0);
            try {
                mKey = a.getString(com.kabouzeid.appthemehelper.R.styleable.ATESwitchPreference_ateKey_pref_switch);
            } finally {
                a.recycle();
            }
        }

        try {
            Field canRecycleLayoutField = Preference.class.getDeclaredField("mCanRecycleLayout");
            canRecycleLayoutField.setAccessible(true);
            canRecycleLayoutField.setBoolean(this, true);
        } catch (Exception ignored) {
        }

        try {
            Field hasSpecifiedLayout = Preference.class.getDeclaredField("mHasSpecifiedLayout");
            hasSpecifiedLayout.setAccessible(true);
            hasSpecifiedLayout.setBoolean(this, true);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSwitch = (ATESwitch) view.findViewById(com.kabouzeid.appthemehelper.R.id.switchWidget);
        mSwitch.setChecked(isChecked());
        mSwitch.setKey(mKey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mSwitch.setBackground(null);

        ATH.apply(view, mKey);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
    }
}