package com.stratagile.qlink.ui.activity.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.stratagile.qlink.R;
import com.stratagile.qlink.application.AppConfig;
import com.stratagile.qlink.base.BaseActivity;
import com.stratagile.qlink.entity.QrEntity;
import com.stratagile.qlink.entity.TokenInfo;
import com.stratagile.qlink.ui.activity.wallet.component.DaggerWalletQRCodeComponent;
import com.stratagile.qlink.ui.activity.wallet.contract.WalletQRCodeContract;
import com.stratagile.qlink.ui.activity.wallet.module.WalletQRCodeModule;
import com.stratagile.qlink.ui.activity.wallet.presenter.WalletQRCodePresenter;
import com.stratagile.qlink.utils.ThreadUtil;
import com.stratagile.qlink.utils.ToastUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.wallet
 * @Description: $description
 * @date 2018/10/30 15:54:27
 */

public class WalletQRCodeActivity extends BaseActivity implements WalletQRCodeContract.View {

    @Inject
    WalletQRCodePresenter mPresenter;
    @BindView(R.id.ivQRCode)
    ImageView ivQRCode;
    @BindView(R.id.tvWalletAddess)
    TextView tvWalletAddess;
    private QrEntity qrEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainColor = R.color.white;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_wallet_qr_code);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initData() {
        qrEntity = getIntent().getParcelableExtra("qrentity");
        if (qrEntity.getIcon() != null && !"".equals(qrEntity.getIcon())) {
            Bitmap logo = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(qrEntity.getIcon(), "mipmap", getPackageName()));
                ThreadUtil.Companion.CreateEnglishQRCode createEnglishQRCode = new ThreadUtil.Companion.CreateEnglishQRCode(qrEntity.getContent(), ivQRCode, logo);
                createEnglishQRCode.execute();
        } else {
            ThreadUtil.Companion.CreateEnglishQRCode createEnglishQRCode = new ThreadUtil.Companion.CreateEnglishQRCode(qrEntity.getContent(), ivQRCode, null);
            createEnglishQRCode.execute();
        }
        setTitle(qrEntity.getTitle());
        tvWalletAddess.setText(qrEntity.getContent());
    }

    @Override
    protected void setupActivityComponent() {
        DaggerWalletQRCodeComponent
                .builder()
                .appComponent(((AppConfig) getApplication()).getApplicationComponent())
                .walletQRCodeModule(new WalletQRCodeModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setPresenter(WalletQRCodeContract.WalletQRCodeContractPresenter presenter) {
        mPresenter = (WalletQRCodePresenter) presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void closeProgressDialog() {
        progressDialog.hide();
    }

    @OnClick(R.id.tvWalletAddess)
    public void onViewClicked() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setPrimaryClip(ClipData.newPlainText("", tvWalletAddess.getText().toString()));
        ToastUtil.displayShortToast("copy success");
    }
}