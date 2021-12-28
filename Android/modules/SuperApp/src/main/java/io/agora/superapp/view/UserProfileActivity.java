package io.agora.superapp.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import io.agora.example.base.BaseActivity;
import io.agora.superapp.R;
import io.agora.superapp.databinding.ActivityUserProfileBinding;
import io.agora.superapp.util.UserUtil;

public class UserProfileActivity extends BaseActivity<ActivityUserProfileBinding> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding.editProfileNickname.setText(UserUtil.getLocalUserName(this));
        Glide.with(this)
                .load(UserUtil.getUserProfileIcon(UserUtil.getLocalUserId()))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mBinding.userProfileIcon);
        mBinding.userProfileNicknameSettingLayout.setOnClickListener(v -> {
            FrameLayout dialogView = new FrameLayout(UserProfileActivity.this);
            EditText editText = new EditText(UserProfileActivity.this);
            editText.setText(UserUtil.getLocalUserName(UserProfileActivity.this));
            editText.setHint(R.string.profile_item_title_nickname);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.text_content_padding));
            params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.text_content_padding));
            dialogView.addView(editText, params);
            new AlertDialog.Builder(UserProfileActivity.this)
                    .setTitle(R.string.profile_item_title_nickname)
                    .setView(dialogView)
                    .setNegativeButton(R.string.cmm_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.cmm_ok, (dialog, which) -> {
                        String nUserName = editText.getText().toString();
                        if(TextUtils.isEmpty(nUserName)){
                            Toast.makeText(UserProfileActivity.this, "The user name should not be null.", Toast.LENGTH_SHORT).show();
                        }else{
                            UserUtil.setLocalUserName(nUserName);
                            mBinding.editProfileNickname.setText(nUserName);
                        }
                        dialog.dismiss();
                    })
                    .show();
        });
    }

}
