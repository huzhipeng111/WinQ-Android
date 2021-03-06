package com.stratagile.qlink.ui.activity.eth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socks.library.KLog;
import com.stratagile.qlink.R;
import com.stratagile.qlink.application.AppConfig;
import com.stratagile.qlink.base.BaseActivity;
import com.stratagile.qlink.db.EosAccount;
import com.stratagile.qlink.db.EthWallet;
import com.stratagile.qlink.db.QLCAccount;
import com.stratagile.qlink.db.Wallet;
import com.stratagile.qlink.entity.AllWallet;
import com.stratagile.qlink.ui.activity.eos.EosResourceManagementActivity;
import com.stratagile.qlink.ui.activity.eth.component.DaggerWalletDetailComponent;
import com.stratagile.qlink.ui.activity.eth.contract.WalletDetailContract;
import com.stratagile.qlink.ui.activity.eth.module.WalletDetailModule;
import com.stratagile.qlink.ui.activity.eth.presenter.WalletDetailPresenter;
import com.stratagile.qlink.ui.activity.qlc.QlcMnemonicShowActivity;
import com.stratagile.qlink.ui.activity.wallet.ExportEthKeyStoreActivity;
import com.stratagile.qlink.ui.activity.wallet.VerifyWalletPasswordActivity;
import com.stratagile.qlink.utils.LocalWalletUtil;
import com.stratagile.qlink.utils.ToastUtil;
import com.stratagile.qlink.utils.eth.ETHWalletUtils;
import com.stratagile.qlink.view.SweetAlertDialog;
import com.vondear.rxtools.view.RxQRCode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qlc.utils.Helper;

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.eth
 * @Description: $description
 * @date 2018/10/25 15:02:11
 */

public class WalletDetailActivity extends BaseActivity implements WalletDetailContract.View {

    @Inject
    WalletDetailPresenter mPresenter;
    @BindView(R.id.ivWalletAvatar)
    ImageView ivWalletAvatar;
    @BindView(R.id.tvWalletName)
    TextView tvWalletName;
    @BindView(R.id.tvWalletAddress)
    TextView tvWalletAddress;
    @BindView(R.id.tvWalletAsset)
    TextView tvWalletAsset;
    @BindView(R.id.llAbucoins)
    LinearLayout llAbucoins;
    @BindView(R.id.llExportKeystore)
    LinearLayout llExportKeystore;
    @BindView(R.id.llExportPrivateKey)
    LinearLayout llExportPrivateKey;
    @BindView(R.id.llExportNeoEncryptedKey)
    LinearLayout llExportNeoEncryptedKey;
    @BindView(R.id.llExportNeoPrivateKey)
    LinearLayout llExportNeoPrivateKey;
    @BindView(R.id.tvDeleteWallet)
    TextView tvDeleteWallet;
    @BindView(R.id.ResourceManagement)
    LinearLayout ResourceManagement;
    @BindView(R.id.ownerPrivateKey)
    LinearLayout ownerPrivateKey;
    @BindView(R.id.activePrivateKey)
    LinearLayout activePrivateKey;
    @BindView(R.id.ExportQlcPrivateKey)
    LinearLayout ExportQlcPrivateKey;
    @BindView(R.id.ExportQlcSeed)
    LinearLayout ExportQlcSeed;

    private EthWallet ethWallet;

    private Wallet wallet;

    private EosAccount eosAccount;
    private QLCAccount qlcAccount;

    private AllWallet.WalletType walletType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainColor = R.color.white;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_eth_wallet_detail);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getIntExtra("walletType", 0) == AllWallet.WalletType.EthWallet.ordinal()) {
            ethWallet = getIntent().getParcelableExtra("ethwallet");
            walletType = AllWallet.WalletType.EthWallet;

            ivWalletAvatar.setImageDrawable(getResources().getDrawable(R.mipmap.icons_eth_wallet));
            tvWalletName.setText(ethWallet.getName());
            tvWalletAddress.setText(ethWallet.getAddress());

            llExportNeoEncryptedKey.setVisibility(View.GONE);
            llExportNeoPrivateKey.setVisibility(View.GONE);

            ResourceManagement.setVisibility(View.GONE);

            ownerPrivateKey.setVisibility(View.GONE);
            activePrivateKey.setVisibility(View.GONE);

            ExportQlcPrivateKey.setVisibility(View.GONE);
            ExportQlcSeed.setVisibility(View.GONE);
        } else if (getIntent().getIntExtra("walletType", 0) == AllWallet.WalletType.NeoWallet.ordinal()) {
            walletType = AllWallet.WalletType.NeoWallet;
            wallet = getIntent().getParcelableExtra("neowallet");
            ivWalletAvatar.setImageDrawable(getResources().getDrawable(R.mipmap.icons_neo_wallet));
            tvWalletName.setText(wallet.getAddress());
            tvWalletAddress.setText(wallet.getAddress());
            llAbucoins.setVisibility(View.GONE);
            llExportKeystore.setVisibility(View.GONE);
            llExportPrivateKey.setVisibility(View.GONE);
            ResourceManagement.setVisibility(View.GONE);

            ownerPrivateKey.setVisibility(View.GONE);
            activePrivateKey.setVisibility(View.GONE);

            ExportQlcPrivateKey.setVisibility(View.GONE);
            ExportQlcSeed.setVisibility(View.GONE);

        } else if (getIntent().getIntExtra("walletType", 0) == AllWallet.WalletType.EosWallet.ordinal()) {
            walletType = AllWallet.WalletType.EosWallet;
            eosAccount = getIntent().getParcelableExtra("eoswallet");
            ivWalletAvatar.setImageDrawable(getResources().getDrawable(R.mipmap.icons_eos_wallet));
            tvWalletName.setText(eosAccount.getWalletName());
            tvWalletAddress.setText(eosAccount.getAccountName());
            llAbucoins.setVisibility(View.GONE);
            llExportKeystore.setVisibility(View.GONE);
            llExportPrivateKey.setVisibility(View.GONE);
            llExportNeoEncryptedKey.setVisibility(View.GONE);
            llExportNeoPrivateKey.setVisibility(View.GONE);

            ExportQlcPrivateKey.setVisibility(View.GONE);
            ExportQlcSeed.setVisibility(View.GONE);

            if (eosAccount.getOwnerPrivateKey() == null || "".equals(eosAccount.getOwnerPrivateKey())) {
                ownerPrivateKey.setVisibility(View.GONE);
            }
            if (eosAccount.getActivePrivateKey() == null || "".equals(eosAccount.getActivePrivateKey())) {
                activePrivateKey.setVisibility(View.GONE);
            }
        } else if (getIntent().getIntExtra("walletType", 0) == AllWallet.WalletType.QlcWallet.ordinal()) {
            walletType = AllWallet.WalletType.QlcWallet;
            qlcAccount = getIntent().getParcelableExtra("qlcwallet");
            ivWalletAvatar.setImageDrawable(getResources().getDrawable(R.mipmap.icons_qlc_wallet));
            tvWalletName.setText(qlcAccount.getAccountName());
            tvWalletAddress.setText(qlcAccount.getAddress());

            llAbucoins.setVisibility(View.GONE);
            llExportKeystore.setVisibility(View.GONE);
            llExportPrivateKey.setVisibility(View.GONE);
            llExportNeoEncryptedKey.setVisibility(View.GONE);
            llExportNeoPrivateKey.setVisibility(View.GONE);
            ResourceManagement.setVisibility(View.GONE);

            ownerPrivateKey.setVisibility(View.GONE);
            activePrivateKey.setVisibility(View.GONE);

            ExportQlcPrivateKey.setVisibility(View.VISIBLE);
            if (qlcAccount.getSeed() == null || qlcAccount.getSeed().equals("")) {
                ExportQlcSeed.setVisibility(View.GONE);
            } else {
                ExportQlcSeed.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_manage));
    }

    @Override
    protected void setupActivityComponent() {
        DaggerWalletDetailComponent
                .builder()
                .appComponent(((AppConfig) getApplication()).getApplicationComponent())
                .walletDetailModule(new WalletDetailModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setPresenter(WalletDetailContract.EthWalletDetailContractPresenter presenter) {
        mPresenter = (WalletDetailPresenter) presenter;
    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void closeProgressDialog() {
        progressDialog.hide();
    }

    @OnClick({R.id.llAbucoins, R.id.llExportKeystore, R.id.llExportPrivateKey, R.id.llExportNeoEncryptedKey, R.id.llExportNeoPrivateKey, R.id.tvDeleteWallet, R.id.ResourceManagement, R.id.ownerPrivateKey, R.id.activePrivateKey, R.id.ExportQlcPrivateKey, R.id.ExportQlcSeed})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.llAbucoins:
                if (ethWallet.getIsLook()) {
                    ToastUtil.displayShortToast(getString(R.string.only_watch_eth_wallet_cannot_look_mnemonic));
                    return;
                }
                if (ethWallet.getMnemonic() == null) {
                    cannotShowMnemonic();
                } else {
                    exportMnemonic();
                }
                break;
            case R.id.llExportKeystore:
                if (ethWallet.getIsLook()) {
                    ToastUtil.displayShortToast(getString(R.string.only_watch_eth_wallet_cannot_exportkeystore));
                    return;
                }
                startActivity(new Intent(this, ExportEthKeyStoreActivity.class).putExtra("wallet", ethWallet));
                break;
            case R.id.llExportPrivateKey:
                if (ethWallet.getIsLook()) {
                    ToastUtil.displayShortToast(getString(R.string.only_watch_wallet_cannot_export_keystore));
                    return;
                }
                ExportPrivateKey();
                break;
            case R.id.llExportNeoEncryptedKey:
                ExportNeoEncryptedKey();
                break;
            case R.id.llExportNeoPrivateKey:
                ExportNeoPrivateKey();
                break;
            case R.id.tvDeleteWallet:
                showDeleteWalletDialog();
                break;
            case R.id.ResourceManagement:
                startActivity(new Intent(this, EosResourceManagementActivity.class).putExtra("eosAccount", eosAccount));
                break;
            case R.id.ownerPrivateKey:
                exportOwnerPrivateKey();
                break;
            case R.id.activePrivateKey:
                exporActivePrivateKey();
                break;
            case R.id.ExportQlcPrivateKey:
                exportQlcMnemonicStr();
                break;
            case R.id.ExportQlcSeed:
                exportQlcSeed();
                break;
            default:
                break;
        }
    }

    private void exportQlcMnemonicStr() {
        String privateKey = qlcAccount.getMnemonic();
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.export_mnemonic_phrase));
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        TextView tvWarn = view.findViewById(R.id.tv_warn);
        tvWarn.setVisibility(View.GONE);
        tvWarn.setText(getString(R.string.warning_export_mnemonic_is_very_dangerous_we_recommend_you_backup_with_keystore));
        Bitmap bitmap = RxQRCode.builder(privateKey).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvContent.setText(privateKey);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
        lp.setMargins((int) getResources().getDimension(R.dimen.x30), (int) getResources().getDimension(R.dimen.x70), (int) getResources().getDimension(R.dimen.x30), (int) getResources().getDimension(R.dimen.x70));
        tvContent.setLayoutParams(lp);
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                }
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void exportQlcSeed() {
        String privateKey = qlcAccount.getSeed();
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.export_wallet_seed));
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        TextView tvWarn = view.findViewById(R.id.tv_warn);
        tvWarn.setText(getString(R.string.warning_export_plain_mnemonic_is_very_dangerous_we_recommend_you_backup_with_mnemonic_or_keystore));
//        view.findViewById(R.id.tv_warn).setVisibility(View.GONE);
        Bitmap bitmap = RxQRCode.builder(privateKey).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvContent.setText(privateKey);
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                }
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void exportOwnerPrivateKey() {
        String privateKey = eosAccount.getOwnerPrivateKey();
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        Bitmap bitmap = RxQRCode.builder(privateKey).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvContent.setText(privateKey);
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                }
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void exporActivePrivateKey() {
        String privateKey = eosAccount.getActivePrivateKey();
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        Bitmap bitmap = RxQRCode.builder(privateKey).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvContent.setText(privateKey);
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                }
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void cannotShowMnemonic() {
        View view = getLayoutInflater().inflate(R.layout.alert_dialog, null, false);
        TextView tvContent = view.findViewById(R.id.tvContent);
        tvContent.setText(getString(R.string.wallet_can_not_be_seen_through_the_private_key_and_keystore_import_wallet));
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        ImageView ivTitle = view.findViewById(R.id.ivTitle);
        ivTitle.setImageDrawable(getResources().getDrawable(R.mipmap.careful_1));
        TextView tvOk = view.findViewById(R.id.tvOpreate);
        ivClose.setVisibility(View.INVISIBLE);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvOk.setText(getResources().getString(R.string.ok));
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
    }

    private void exportMnemonic() {
        startActivity(new Intent(this, EthMnemonicShowActivity.class).putExtra("wallet", ethWallet).putExtra("isEth", true));
    }

    private void exportQlcMnemonic() {
        startActivity(new Intent(this, QlcMnemonicShowActivity.class).putExtra("wallet", qlcAccount).putExtra("isEth", false));
    }

    private void showDeleteWalletDialog() {
        View view = getLayoutInflater().inflate(R.layout.alert_dialog_choose, null, false);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvConform = view.findViewById(R.id.tvConform);
        TextView tvCancel = view.findViewById(R.id.tvCancel);
        ImageView imageView = view.findViewById(R.id.ivTitle);
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        imageView.setImageDrawable(getResources().getDrawable(R.mipmap.careful));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvConform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
                deleteWallet();
            }
        });

        tvContent.setText(getString(R.string.do_you_confirm_to_delete_the_wallet));
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();

    }

    private void deleteWallet() {
        startActivityForResult(new Intent(this, VerifyWalletPasswordActivity.class).putExtra("flag", ""), 0);
        overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (walletType == AllWallet.WalletType.NeoWallet) {
                AppConfig.getInstance().getDaoSession().getWalletDao().delete(wallet);
            } else if (walletType == AllWallet.WalletType.EthWallet) {
                AppConfig.getInstance().getDaoSession().getEthWalletDao().delete(ethWallet);
            } else if (walletType == AllWallet.WalletType.EosWallet) {
                AppConfig.getInstance().getDaoSession().getEosAccountDao().delete(eosAccount);
            } else if (walletType == AllWallet.WalletType.QlcWallet) {
                AppConfig.getInstance().getDaoSession().getQLCAccountDao().delete(qlcAccount);
            }
            LocalWalletUtil.updateLocalNeoWallet();
            LocalWalletUtil.updateLocalEthWallet();
            LocalWalletUtil.updateLocalEosWallet();
            LocalWalletUtil.updateLocalQlcWallet();
            showTestDialog();
        }
    }

    private void showTestDialog() {
        View view = getLayoutInflater().inflate(R.layout.alert_dialog_tip, null, false);
        TextView tvContent = view.findViewById(R.id.tvContent);
        ImageView imageView = view.findViewById(R.id.ivTitle);
        imageView.setImageDrawable(getResources().getDrawable(R.mipmap.op_success));
        tvContent.setText(getString(R.string.delete_wallet_success));
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        tvDeleteWallet.postDelayed(new Runnable() {
            @Override
            public void run() {
                sweetAlertDialog.cancel();
                tvDeleteWallet.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        onBackPressed();
                    }
                }, 200);
            }
        }, 2000);
    }

    private void ExportPrivateKey() {
        String privateKey = ETHWalletUtils.derivePrivateKey(ethWallet.getId());
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        Bitmap bitmap = RxQRCode.builder(privateKey).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvContent.setText(privateKey);
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                }
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void ExportNeoPrivateKey() {
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        TextView tv_warn = (TextView) view.findViewById(R.id.tv_warn);//输入内容
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);//输入内容
        tv_warn.setVisibility(View.GONE);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        tvContent.setText(wallet.getPrivateKey());
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        Bitmap bitmap = RxQRCode.builder(wallet.getPrivateKey()).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                    tv_warn.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                    tv_warn.setVisibility(View.INVISIBLE);
                }
            }
        });
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

    private void ExportNeoEncryptedKey() {
        View view = View.inflate(this, R.layout.dialog_export_privatekey_layout, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);//输入内容
        TextView tv_warn = (TextView) view.findViewById(R.id.tv_warn);//输入内容
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);//输入内容
        tvTitle.setText(getString(R.string.export_encrypted_key));
        tv_warn.setVisibility(View.GONE);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        TextView tvCopy = view.findViewById(R.id.tvCopy);//取消按钮
        tvContent.setText(wallet.getWif());
        TextView tvQrCode = view.findViewById(R.id.tvQrCode);
        ImageView ivQRCode = view.findViewById(R.id.ivQRCode);
        Bitmap bitmap = RxQRCode.builder(wallet.getWif()).
                backColor(getResources().getColor(com.vondear.rxtools.R.color.white)).
                codeColor(getResources().getColor(com.vondear.rxtools.R.color.black)).
                codeSide(800).
                into(ivQRCode);
        tvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivQRCode.getVisibility() == View.VISIBLE) {
                    ivQRCode.setVisibility(View.GONE);
                    tv_warn.setVisibility(View.GONE);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                    tv_warn.setVisibility(View.INVISIBLE);
                }
            }
        });
        //取消或确定按钮监听事件处l
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        Window window = sweetAlertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        sweetAlertDialog.setView(view);
        sweetAlertDialog.show();
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog.cancel();
            }
        });
        tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setPrimaryClip(ClipData.newPlainText("", tvContent.getText().toString()));
                ToastUtil.displayShortToast(getResources().getString(R.string.copy_success));
                sweetAlertDialog.cancel();
            }
        });
    }

}