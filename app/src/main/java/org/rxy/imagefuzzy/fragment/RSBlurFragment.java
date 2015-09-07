package org.rxy.imagefuzzy.fragment;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.rxy.imagefuzzy.R;

/**
 * Created by rxy on 15/9/6.
 * E－mail:rxywxsy@163.com
 */
public class RSBlurFragment extends Fragment {

    private ImageView mImageView;
    private TextView mTextView;
    private LinearLayout mLayout;
    private TextView statusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.iv_fragment, container, false);
        mImageView = (ImageView) view.findViewById(R.id.picture);
        mTextView = (TextView) view.findViewById(R.id.text);
        mLayout = (LinearLayout) view.findViewById(R.id.controls);
        statusText = addStatusText(mLayout);
        applyBlur();
        return view;
    }

    /**
     * 允许模糊的方法
     */
    private void applyBlur() {
        mImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                mImageView.buildDrawingCache();

                Bitmap bitmap = mImageView.getDrawingCache();
                blur(bitmap, mTextView);
                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void blur(Bitmap bitmap, View view) {
        long startMs = System.currentTimeMillis();

        float radius = 20;
        Bitmap overlay = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft(), -view.getTop());
        canvas.drawBitmap(bitmap, 0, 0, null);

        RenderScript renderScript = RenderScript.create(getActivity());
        Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, allocation.getElement());

        blur.setInput(allocation);
        blur.setRadius(radius);
        blur.forEach(allocation);
        allocation.copyTo(overlay);

        view.setBackground(new BitmapDrawable(getResources(), overlay));
        renderScript.destroy();
        statusText.setText(System.currentTimeMillis() - startMs + "ms");
    }

    @Override
    public String toString() {
        return "RenderScript";
    }

    private TextView addStatusText(ViewGroup container) {
        TextView tv = new TextView(getActivity());
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setTextColor(0xFFFFFFFF);
        container.addView(tv);
        return tv;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
