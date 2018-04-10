package csu.edu.ice.gobang;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by ice on 2018/4/6.
 */

public class ResultDialog extends DialogFragment {

    private boolean isWin;
    public ResultDialog(){}

    String mUrl;
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        isWin = args.getBoolean("isWin");
        mUrl = args.getString("url");
    }

    public void setArguments(String url,boolean isWin){
        Bundle bundle = new Bundle();
        bundle.putString("url",url);
        bundle.putBoolean("isWin",isWin);
        setArguments(bundle);
    }
    long enterTime;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_result, container);
        Picasso.with(getActivity()).load(mUrl).into((ImageView) view.findViewById(R.id.ivResult));
        TextView tvResult = view.findViewById(R.id.tvResult);
        Button btnClose = view.findViewById(R.id.btnReturn);
        enterTime = System.currentTimeMillis();
        if(isWin == true){
            tvResult.setText(getResources().getString(R.string.success));
        }else{
            tvResult.setText(getResources().getString(R.string.failed));
        }
        btnClose.setOnClickListener(v -> {
            if(isWin ==false &&System.currentTimeMillis() - enterTime < 3*1000){
                //时间不到3秒
                Toast.makeText(getActivity(), "还是看够3秒吧，长长记性", Toast.LENGTH_SHORT).show();
                return;
            }
            dismiss();
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable( new ColorDrawable(Color.WHITE));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        win.setAttributes(params);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getActivity().finish();
    }
}
