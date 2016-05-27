package com.cdd.soton.views.dialoglist;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cdd.soton.recognize.SubjectBean;

import java.util.List;

public class ShareSelectDialogAdapter extends BaseAdapter {

    private List<SubjectBean> mShareItems = null;
    private Context mContext = null;

    public ShareSelectDialogAdapter(Context mContext, List<SubjectBean> resolveInfos) {
        this.mContext = mContext;
        this.mShareItems = resolveInfos;
    }

    @Override
    public int getCount() {
        return mShareItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mShareItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setBackgroundColor(Color.parseColor("#F2F2F2"));
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setMinimumHeight(ShareUiUtil.dip2px(60, mContext));

            holder = new ViewHolder();

            holder.mNameText = new TextView(mContext);
            holder.mNameText.setPadding(ShareUiUtil.dip2px(12, mContext), 0, 0, 0);
            holder.mNameText.setTextColor(Color.parseColor("#000000"));
            holder.mNameText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
            holder.mNameText.setSingleLine();
            layout.addView(holder.mNameText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            convertView = layout;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mNameText.setText(mShareItems.get(i).name);
        return convertView;
    }

    private static class ViewHolder {
        TextView mNameText;
    }
}
