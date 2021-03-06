package com.stratagile.qlink.ui.activity.stake

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.google.gson.Gson
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.qlink.R
import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.base.BaseActivity
import com.stratagile.qlink.constant.ConstantValue
import com.stratagile.qlink.entity.eventbus.CreateMultSignSuccess
import com.stratagile.qlink.entity.eventbus.StakeQlcError
import com.stratagile.qlink.entity.stake.LockResult
import com.stratagile.qlink.entity.stake.MultSign
import com.stratagile.qlink.entity.stake.StakeType
import com.stratagile.qlink.ui.activity.stake.component.DaggerNewStakeComponent
import com.stratagile.qlink.ui.activity.stake.contract.NewStakeContract
import com.stratagile.qlink.ui.activity.stake.module.NewStakeModule
import com.stratagile.qlink.ui.activity.stake.presenter.NewStakePresenter
import com.stratagile.qlink.utils.UIUtils
import com.stratagile.qlink.utils.getLine
import kotlinx.android.synthetic.main.activity_new_stake.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import wendu.dsbridge.DWebView
import wendu.dsbridge.OnReturnValue
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.stake
 * @Description: $description
 * @date 2019/08/08 16:33:44
 */

class NewStakeActivity : BaseActivity(), NewStakeContract.View {

    @Inject
    internal lateinit var mPresenter: NewStakePresenter

    lateinit var stakeViewModel : NewStakeViewModel

    lateinit var webview : DWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        mainColor = R.color.white
        isEditActivity = true
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun initView() {
        setContentView(R.layout.activity_new_stake)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun createMultSign(stakeType: StakeType) {
        var isBack = false
        webview.callHandler("staking.createMultiSig", arrayOf<Any>(stakeType.neoPubKey, ConstantValue.createMultiSigHash), OnReturnValue<JSONObject> { retValue ->
            KLog.i("call succeed,return value is " + retValue!!)
            var multSign = Gson().fromJson(retValue.toString(), MultSign::class.java)
            if (multSign._address == null) {
                AppConfig.instance.saveLog("stake", "createMultSign" + getLine(), Gson().toJson(multSign))
                toast(getString(R.string.create_multsign_error_please_try_later))
                EventBus.getDefault().post(StakeQlcError())
                isBack = true
            } else {
                stakeType.toAddress = multSign._address
                lockQlc(stakeType)
                EventBus.getDefault().post(CreateMultSignSuccess())
                isBack = true
            }
        })
        thread {
            Thread.sleep(3000)
            if (!isBack) {
                runOnUiThread {
                    createMultSign(stakeType)
                }
                KLog.i("重新获取多重签名。。。")
            }
        }
    }

    fun lockQlc(stakeType: StakeType) {
        webview.callHandler("staking.contractLock", arrayOf<Any>(stakeType.neoPriKey, stakeType.fromNeoAddress, stakeType.toAddress, stakeType.qlcchainAddress, stakeType.stakeQLcAmount, stakeType.stakeQlcDays, AppConfig.instance.isMainNet), OnReturnValue<JSONObject> { retValue ->
            KLog.i("lock result= " + retValue!!)
            var lockResult = Gson().fromJson(retValue.toString(), LockResult::class.java)
            if (lockResult.txid == null) {
                AppConfig.instance.saveLog("stake", "voteNode lockQlc" + getLine(), Gson().toJson(lockResult))
                toast(getString(R.string.lock_qlc_error_please_try_later))
                EventBus.getDefault().post(StakeQlcError())
            } else {
                lockResult.stakeType = stakeType
                stakeViewModel.lockResult.postValue(lockResult)
            }
        })
    }
    override fun initData() {
        webview = DWebView(this)
        webview.loadUrl("file:///android_asset/contract.html")

        EventBus.getDefault().register(this)
        stakeViewModel = ViewModelProviders.of(this).get<NewStakeViewModel>(NewStakeViewModel::class.java)
//        DWebView.setWebContentsDebuggingEnabled(true)
//        webview.addJavascriptObject(JsApi(), null)
        title.text = getString(R.string.new_stakeing)
        val titles = ArrayList<String>()
        titles.add(getString(R.string.vote_mining_node))
//        titles.add(getString(R.string.confidant))
        titles.add(getString(R.string.token_mintage))
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                when(position) {
                    0 -> {
                        return VoteNodeFragment()
                    }
//                    1 -> {
//                        return ConfidantFragment()
//                    }
                    1 -> {
                        return TokenMintageFragment()
                    }
                }
                return Fragment()
            }

            override fun getCount(): Int {
                return titles.size
            }
        }
        viewPager.offscreenPageLimit = 3
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return titles.size
            }

            override fun getTitleView(context: Context, i: Int): IPagerTitleView {
                val simplePagerTitleView = SimplePagerTitleView(context)
                simplePagerTitleView.text = titles[i]
                simplePagerTitleView.normalColor = resources.getColor(R.color.color_b3b3b3)
                simplePagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.mainColor)
                simplePagerTitleView.setOnClickListener { viewPager.setCurrentItem(i) }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
                indicator.lineHeight = UIUtils.dip2px(2f, this@NewStakeActivity).toFloat()
                indicator.setColors(resources.getColor(R.color.mainColor))
                return indicator
            }
        }
        indicator.setNavigator(commonNavigator)
        commonNavigator.titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        commonNavigator.titleContainer.dividerDrawable = object : ColorDrawable() {
            override fun getIntrinsicWidth(): Int {
                return UIUtils.dip2px(6f, this@NewStakeActivity)
            }
        }
        ViewPagerHelper.bind(indicator, viewPager)

        if (intent.hasExtra("txid")) {
            stakeViewModel.txidMutableLiveData.postValue("txid")
        }
    }

    override fun setupActivityComponent() {
       DaggerNewStakeComponent
               .builder()
               .appComponent((application as AppConfig).applicationComponent)
               .newStakeModule(NewStakeModule(this))
               .build()
               .inject(this)
    }
    override fun setPresenter(presenter: NewStakeContract.NewStakeContractPresenter) {
            mPresenter = presenter as NewStakePresenter
        }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.stake_explain, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.stakeExplain) {
            startActivity(Intent(this, StakeExplainActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}