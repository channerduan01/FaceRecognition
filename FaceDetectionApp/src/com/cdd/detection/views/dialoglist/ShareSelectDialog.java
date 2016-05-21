package com.cdd.detection.views.dialoglist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class ShareSelectDialog extends Dialog {

    private Builder mBuilder;

    private LinearLayout mRootView;
    private TextView mTitleView;

    private ShareSelectDialog(Context context) {
        super(context);
    }

    public ShareSelectDialog(Builder builder) {
        super(builder.getContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        mBuilder = builder;
        Context context = mBuilder.getContext();
        mRootView = new LinearLayout(context);
        mRootView.setOrientation(LinearLayout.VERTICAL);
        mTitleView = new TextView(context);
        mTitleView.setPadding(ShareUiUtil.dip2px(20, context), ShareUiUtil.dip2px(20, context), ShareUiUtil.dip2px(20, context), ShareUiUtil.dip2px(20, context));
        mTitleView.setBackgroundColor(Color.parseColor("#A2DCF4"));
        mTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        mTitleView.setTextColor(Color.parseColor("#343434"));
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
        mTitleView.setText("Select one to delete");
        mRootView.addView(mTitleView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        View divideView = new View(context);
        divideView.setBackgroundColor(Color.parseColor("#C6C6C6"));
        mRootView.addView(divideView, new LayoutParams(LayoutParams.MATCH_PARENT, ShareUiUtil.dip2px(1, context)));
    }

    public void show() {
        if (isActvityLive(mBuilder.getContext())) {
            setContentView(mRootView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            super.show();
        }
    }

    private boolean isActvityLive(Context context) {
        if (context != null && !((Activity) context).isFinishing()) {
            return true;
        } else {
            return false;
        }
    }

    public static class Builder {
        private boolean mCancelable = true;
        private ListAdapter mAdapter;
        private OnCancelListener mOnCancelListener;
        private OnDismissListener mOnDismissListener;
        private OnClickListener mOnItemClickListener;

        private Context mContext;

        public Builder(Context context) {
            mContext = context;
        }

        public Context getContext() {
            return mContext;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            mOnCancelListener = onCancelListener;
            return this;
        }
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            mOnDismissListener = onDismissListener;
            return this;
        }


        public Builder setAdapter(ListAdapter adapter, final OnClickListener listener) {
            mAdapter = adapter;
            mOnItemClickListener = listener;
            return this;
        }
        public ShareSelectDialog show() {
            ShareSelectDialog dialog = createMyDialog();
            dialog.show();
            return dialog;
        }
        public ShareSelectDialog createMyDialog() {
            final ShareSelectDialog dialog = new ShareSelectDialog(this);

            ListView listView = new ListView(getContext());
            listView.setDivider(new ColorDrawable(Color.parseColor("#C6C6C6")));
            listView.setDividerHeight(ShareUiUtil.dip2px(1,mContext));

            listView.setAdapter(mAdapter);
            if (mOnItemClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnItemClickListener.onClick(dialog, position);
                        dialog.dismiss();
                    }
                });
            }
            dialog.mRootView.addView(listView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            dialog.setOnCancelListener(mOnCancelListener);
            dialog.setOnDismissListener(mOnDismissListener);

            dialog.setCancelable(mCancelable);
            dialog.setCanceledOnTouchOutside(mCancelable);
            if(mCancelable) {
                dialog.mRootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }

            return dialog;
        }
    }
}
