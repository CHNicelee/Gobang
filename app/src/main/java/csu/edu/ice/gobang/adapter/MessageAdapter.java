package csu.edu.ice.gobang.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import csu.edu.ice.gobang.R;

/**
 * Created by ice on 2018/4/10.
 */

public class MessageAdapter extends BaseQuickAdapter<String,BaseViewHolder> {
    public MessageAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvMessage,item);
    }
}
