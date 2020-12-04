package com.stratagile.qlink.ui.activity.defi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.ButterKnife
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.qlink.Account
import com.stratagile.qlink.R
import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.base.BaseFragment
import com.stratagile.qlink.blockchain.cypto.digest.Sha256
import com.stratagile.qlink.constant.ConstantValue
import com.stratagile.qlink.db.EthWallet
import com.stratagile.qlink.db.SwapRecord
import com.stratagile.qlink.db.Wallet
import com.stratagile.qlink.db.WalletDao
import com.stratagile.qlink.entity.AllWallet
import com.stratagile.qlink.entity.BaseHub
import com.stratagile.qlink.entity.NeoWalletInfo
import com.stratagile.qlink.entity.TokenPrice
import com.stratagile.qlink.entity.defi.CheckHubState
import com.stratagile.qlink.entity.defi.EthGasPrice
import com.stratagile.qlink.entity.swap.WrapperOnline
import com.stratagile.qlink.ui.activity.defi.component.DaggerSwapComponent
import com.stratagile.qlink.ui.activity.defi.contract.SwapContract
import com.stratagile.qlink.ui.activity.defi.module.SwapModule
import com.stratagile.qlink.ui.activity.defi.presenter.SwapPresenter
import com.stratagile.qlink.ui.activity.otc.OtcChooseWalletActivity
import com.stratagile.qlink.utils.DefiUtil
import com.stratagile.qlink.utils.SpUtil
import com.stratagile.qlink.utils.eth.ETHWalletUtils
import com.stratagile.qlink.view.SweetAlertDialog
import io.neow3j.contract.ContractInvocation
import io.neow3j.contract.ContractParameter
import io.neow3j.contract.ScriptHash
import io.neow3j.contract.Test1Contract
import io.neow3j.protocol.Neow3j
import io.neow3j.protocol.http.HttpService
import kotlinx.android.synthetic.main.fragment_swap.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import wendu.dsbridge.OnReturnValue
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.defi
 * @Description: $description
 * @date 2020/08/12 15:49:07
 */

class SwapFragment : BaseFragment(), SwapContract.View {

    @Inject
    lateinit internal var mPresenter: SwapPresenter
    lateinit var ethWallet: EthWallet
    lateinit var neow3j: Neow3j
    lateinit var swapRecord: SwapRecord
    var currentAddress = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swap, null)
        ButterKnife.bind(this, view)
        val mBundle = arguments
        currentAddress = mBundle!!.getString("address")!!
        return view
    }

    object SwapFragmentInstance {
        @JvmStatic
        fun getInstance(address: String): SwapFragment {
            var bundle = Bundle()
            bundle.putString("address", address)
            var swapRecordFragment = SwapFragment()
            swapRecordFragment.arguments = bundle
            return swapRecordFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swapRecord = SwapRecord()
        swapRecord.type = SwapRecord.SwapType.typeNep5ToErc20.ordinal
        swapRecord.isMainNet = SpUtil.getBoolean(activity, ConstantValue.isMainNet, true)
        webview.loadUrl("file:///android_asset/eth.html")
        KLog.i("加载网页")
//        KLog.i(Numeric.toHexStringNoPrefix("942795a2a82e4228af75fc566a4cb909".toByteArray()))
//        AppConfig.instance.daoSession.swapRecordDao.deleteAll()
        miniSwapQlc.text = getString(R.string.the_minimum_stake_amount_is_1_s_qlc, "1")
        etStakeQlcAmount.hint = ""
        // http://seed1.o3node.org:10332
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        builder.callTimeout(600, TimeUnit.SECONDS)
        builder.readTimeout(600, TimeUnit.SECONDS)
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.addInterceptor(logging)
        //        neow = Neow3j.build(new HttpService("http://seed7.ngd.network:10332", builder.build()));
        neow3j = Neow3j.build(HttpService(ConstantValue.neoNode, builder.build()))
        invoke.setOnClickListener {
            if (!this::wrapperOnline.isInitialized) {
                checkWrapperOnline()
                toast(getString(R.string.pleasewait))
                return@setOnClickListener
            }
//            if (wrapperOnline.ethBalance < 0.01) {
//                toast(getString(R.string.is_withdraw_limit))
//                return@setOnClickListener
//            }
            if (neoBalance == -1.toDouble()) {
                toast(getString(R.string.pleasewait))
                getTestNetQlcBalance(wallet.address)
                return@setOnClickListener
            }
            if (ethPrice == 0.toDouble()) {
                mPresenter.getEthPrice()
                toast(getString(R.string.pleasewait))
                return@setOnClickListener
            }
            if (!this::ethGasPrice.isInitialized) {
                mPresenter.getEthGasPrice(hashMapOf())
                toast(getString(R.string.pleasewait))
                return@setOnClickListener
            }
            if (ethBalance == -1.toBigInteger()) {
                getEthbalance()
                toast(getString(R.string.pleasewait))
                return@setOnClickListener
            }
            if (neoBalance < etStakeQlcAmount.text.toString().toInt()) {
                toast(getString(R.string.insufficient_balance))
                return@setOnClickListener
            }
            if (etStakeQlcAmount.text.toString().toFloat() < 1) {
                toast(getString(R.string.the_minimum_stake_amount_is_1_s_qlc, (1.toString())))
                return@setOnClickListener
            }
            if (!"".equals(etStakeQlcAmount.text.toString()) && this::ethWallet.isInitialized) {
                showEthFee()
            }
        }
        getNeoWallet()
        llSelectETHWallet.setOnClickListener {
            var intent1 = Intent(activity, OtcChooseWalletActivity::class.java)
            intent1.putExtra("walletType", AllWallet.WalletType.EthWallet.ordinal)
            intent1.putExtra("select", true)
            startActivityForResult(intent1, AllWallet.WalletType.EthWallet.ordinal)
            activity!!.overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out)
        }

        etStakeQlcAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                if (p0.toString().length == 0) {
                    invoke.setBackgroundResource(R.drawable.unable_bt_bg)
                } else {
                    if (this@SwapFragment::ethWallet.isInitialized) {
                        invoke.setBackgroundResource(R.drawable.main_color_bt_bg)
                    } else {
                        invoke.setBackgroundResource(R.drawable.unable_bt_bg)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        thread {
            checkWrapperOnline()
        }

        saHalf = ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        saHalf.setDuration(400)

        sa1 = ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa1.setDuration(1000)

    }

    fun userLock() {
        showSwapPup()
        thread {
            val orginHash = UUID.randomUUID().toString().replace("-", "")
            KLog.i(orginHash)
            val hashSha256 = Sha256.from(orginHash.toByteArray()).toString()
            KLog.i(hashSha256)
            swapRecord.rOrign = orginHash
            swapRecord.rHash = hashSha256
            var txid = Test1Contract.userLock(neow3j, wallet.address, wallet.wif, etStakeQlcAmount.text.toString().toBigInteger(), ethWallet.address, wrapperOnline.neoContract)
            KLog.i(txid)
            if ("".equals(txid)) {
                runOnUiThread {
                    toast(getString(R.string.please_retry))
                }
                sweetAlertDialogSwap!!.dismissWithAnimation()
                return@thread
            }
            swapRecord.lockTime = System.currentTimeMillis()
            swapRecord.txHash = txid
            swapRecord.toAddress = ethWallet.address
            swapRecord.amount = etStakeQlcAmount.text.toString().toInt()
            swapRecord.state = 0
            AppConfig.instance.daoSession.swapRecordDao.insert(swapRecord)
            checkNep5Transaction()
        }
    }

    var ethPrice = 0.toDouble()
    override fun setEthPrice(tokenPrice: TokenPrice) {
        ethPrice = tokenPrice.data[0].price
        getEthbalance()
    }

    lateinit var ethGasPrice: EthGasPrice
    var gasLimit = 200000

    override fun setEthGasPrice(string: String) {
        var ethGasPrice1 = Gson().fromJson<EthGasPrice>(string, EthGasPrice::class.java)
        ethGasPrice = ethGasPrice1
        KLog.i(ethGasPrice)
        mPresenter.getEthPrice()
    }

    var sweetAlertDialog: SweetAlertDialog? = null
    var gasPrice = ""
    fun showEthFee() {
        val view = layoutInflater.inflate(R.layout.show_eth_fee, null, false)
        var tvOk = view.findViewById<View>(R.id.tvOk)
        var ivClose = view.findViewById<View>(R.id.ivClose)
        var ivSendChain = view.findViewById<ImageView>(R.id.ivSendChain)

        var tvEthWalletName1 = view.findViewById<TextView>(R.id.tvEthWalletName1)
        var tvEthWalletAddess1 = view.findViewById<TextView>(R.id.tvEthWalletAddess1)
        var tvToAddress = view.findViewById<TextView>(R.id.tvToAddress)
        var tvEthAmount1 = view.findViewById<TextView>(R.id.tvEthAmount1)
        var llSlow = view.findViewById<LinearLayout>(R.id.llSlow)
        var llAverage = view.findViewById<LinearLayout>(R.id.llAverage)
        var llFast = view.findViewById<LinearLayout>(R.id.llFast)

        var slowCostEth = view.findViewById<TextView>(R.id.slowCostEth)
        var slowCostUsd = view.findViewById<TextView>(R.id.slowCostUsd)
        val slowGas = Convert.toWei(ethGasPrice.result.safeGasPrice, Convert.Unit.GWEI).divide(Convert.toWei(1.toString() + "", Convert.Unit.ETHER))
        var slowEth = slowGas * gasLimit.toBigDecimal()
        var slowUsd = (slowGas * gasLimit.toBigDecimal()).multiply(ethPrice.toBigDecimal())
        slowCostEth.text = slowEth.stripTrailingZeros().toPlainString() + " ETH"
        slowCostUsd.text = "$ " + slowUsd.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()

        var averageCostEth = view.findViewById<TextView>(R.id.averageCostEth)
        var averageCostUsd = view.findViewById<TextView>(R.id.averageCostUsd)
        val averageGas = Convert.toWei(ethGasPrice.result.proposeGasPrice, Convert.Unit.GWEI).divide(Convert.toWei(1.toString() + "", Convert.Unit.ETHER))
        var averageEth = averageGas * gasLimit.toBigDecimal()
        var averageUsd = (averageGas * gasLimit.toBigDecimal()).multiply(ethPrice.toBigDecimal())
        averageCostEth.text = averageEth.stripTrailingZeros().toPlainString() + " ETH"
        averageCostUsd.text = "$ " + averageUsd.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()

        var fastCostEth = view.findViewById<TextView>(R.id.fastCostEth)
        var fastCostUsd = view.findViewById<TextView>(R.id.fastCostUsd)
        val fastGas = Convert.toWei(ethGasPrice.result.fastGasPrice, Convert.Unit.GWEI).divide(Convert.toWei(1.toString() + "", Convert.Unit.ETHER))
        var fastEth = fastGas * gasLimit.toBigDecimal()
        var fastUsd = (fastGas * gasLimit.toBigDecimal()).multiply(ethPrice.toBigDecimal())
        fastCostEth.text = fastEth.stripTrailingZeros().toPlainString() + " ETH"
        fastCostUsd.text = "$ " + fastUsd.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()



        llSlow.isSelected = false
        llAverage.isSelected = true
        llFast.isSelected = false
        // https://api.etherscan.io/api?module=gastracker&action=gasoracle&apikey=YourApiKeyToken
        gasPrice = ethGasPrice.result.safeGasPrice

        llSlow.setOnClickListener {
            gasPrice = ethGasPrice.result.safeGasPrice
            llSlow.isSelected = true
            llAverage.isSelected = false
            llFast.isSelected = false
        }
        llAverage.setOnClickListener {
            gasPrice = ethGasPrice.result.proposeGasPrice
            llSlow.isSelected = false
            llAverage.isSelected = true
            llFast.isSelected = false
        }
        llFast.setOnClickListener {
            gasPrice = ethGasPrice.result.fastGasPrice
            llSlow.isSelected = false
            llAverage.isSelected = false
            llFast.isSelected = true
        }

        tvEthWalletAddess1.text = wallet.address
        tvEthWalletName1.text = wallet.name
        ivSendChain.setImageResource(R.mipmap.icons_neo_wallet)
        tvToAddress.text = ethWallet.address
        tvEthAmount1.text = etStakeQlcAmount.text.toString() + " QLC"
        sweetAlertDialog = SweetAlertDialog(activity)
        val window = sweetAlertDialog!!.window
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        sweetAlertDialog!!.setView(view)
        sweetAlertDialog!!.show()
        ivClose.setOnClickListener {
            sweetAlertDialog!!.cancel()
        }
        tvOk.setOnClickListener {
            val needGas = Convert.toWei(gasPrice, Convert.Unit.GWEI).divide(Convert.toWei(1.toString() + "", Convert.Unit.ETHER))
            var needEth = needGas * gasLimit.toBigDecimal()
            if (Convert.toWei(needEth, Convert.Unit.ETHER) > ethBalance.toBigDecimal()) {
                toast(getString(R.string.no_enough_eth))
                return@setOnClickListener
            }
            sweetAlertDialog!!.cancel()
            userLock()
        }
    }

    var ethBalance = -1.toBigInteger()
    fun getEthbalance() {
        thread {
            try {
                val web3j = Web3j.build(org.web3j.protocol.http.HttpService(ConstantValue.ethNodeUrl))
                var balance = web3j.ethGetBalance(ethWallet.address, DefaultBlockParameter.valueOf("latest")).send()
                ethBalance = balance.balance
                KLog.i(balance.balance)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun derivePrivateKey(address: String): String? {
        val ethWallets = AppConfig.getInstance().daoSession.ethWalletDao.loadAll()
        var ethWallet = EthWallet()
        for (i in ethWallets.indices) {
            if (ethWallets[i].address.equals(address, true)) {
                ethWallet = ethWallets[i]
                break
            }
        }
        return ETHWalletUtils.derivePrivateKey(ethWallet.id!!)
    }

    /**
     * 检查发出的交易是否已经上链
     */
    fun checkNep5Transaction() {
        runOnUiThread {
            tvDot!!.text = "2"
        }
        var log = neow3j.getApplicationLog(swapRecord.txHash).send()
        if (log.hasError()) {
            KLog.i(log.error.message)
            if ("Unknown transaction".equals(log.error.message)) {
                Thread.sleep(5000)
                checkNep5Transaction()
            }
        } else {
            KLog.i(log.applicationLog.transactionId)
            log.applicationLog.executions.forEach {
                KLog.i(it.state)
            }
            neoTransactionConfirmed()
        }
    }

    fun neoTransactionConfirmed() {
        if (isClose) {
            return
        }
        runOnUiThread {
            tvDot!!.text = "3"
        }
        val dataJson = jsonObject(
                "hash" to swapRecord.txHash
        )
        KLog.i(dataJson.toString())
        var request = (ConstantValue.qlcHubEndPoint + "/deposit/neoTransactionConfirmed").httpPost().body(dataJson.toString())
        DefiUtil.addRequestHeader(request)
        request.responseString { _, _, result ->
            val (data, error) = result
            KLog.i(data)
            if (error == null) {
                Thread.sleep(checkStatusTime)
                getEthOwnerSign()
            } else {
                error.exception.printStackTrace()
                Thread.sleep(checkStatusTime)
                neoTransactionConfirmed()
            }
        }
    }

    fun getEthOwnerSign() {
        if (isClose) {
            return
        }
        runOnUiThread {
            tvDot!!.text = "4"
        }
        val dataJson = jsonObject(
                "hash" to swapRecord.txHash
        )
        KLog.i(dataJson.toString())
        var request = (ConstantValue.qlcHubEndPoint + "/deposit/getEthOwnerSign").httpPost().body(dataJson.toString())
        DefiUtil.addRequestHeader(request)
        request.responseString { _, _, result ->
            val (data, error) = result
            KLog.i(data)
            var baseHub = Gson().fromJson<BaseHub>(data, BaseHub::class.java)
            if (error == null) {
                Thread.sleep(checkStatusTime)
                mintErc20Qlc(baseHub.value)
            } else {
                error.exception.printStackTrace()
                Thread.sleep(checkStatusTime)
                getEthOwnerSign()
            }
        }
    }

    fun mintErc20Qlc(signedData: String) {
        runOnUiThread {
            tvDot!!.text = "5"
        }
        val arrays = arrayOfNulls<Any>(9)
        arrays[0] = ConstantValue.ethNodeUrl
        arrays[1] = derivePrivateKey(swapRecord.toAddress)
//        arrays[2] = Numeric.hexStringToByteArray("0x" + signedData)
        arrays[2] = "0x" + signedData
        arrays[3] = swapRecord.toAddress
        arrays[4] = swapRecord.ethContractAddress
        arrays[5] = gasPrice
        arrays[6] = gasLimit
        arrays[7] = swapRecord.amount
//        arrays[8] = stringToBytes32(Numeric.toHexString(swapRecord.txHash.toByteArray()))
        arrays[8] = "0x" + swapRecord.txHash

        arrays.forEach {
            KLog.i(it)
        }
        KLog.i("开始调用html。。。")
//        erc20Transaction()
        Thread {
            webview.callHandler<Any>("ethSmartContract.mint", arrays, object : OnReturnValue<Any> {
                override fun onValue(var1: Any) {
                    this.onValue(var1 as JSONArray)

                }

                fun onValue(retValue: JSONArray) {
                    val result = java.lang.StringBuilder().append("call succeed,return value is ")
                    KLog.i(result.append(retValue).toString())
                    if (retValue[0] == 0) {
                        swapRecord.swaptxHash = retValue[1].toString()
                        checkErc20Transaction()
                    } else {

                    }
                }
            })
        }.start()
    }

    var checkStatusTime = 5000.toLong()
    fun checkLcokState() {
        if (isClose) {
            return
        }
        val dataJson = jsonObject(
                "hash" to swapRecord.txHash
        )
        var list = arrayListOf<Pair<String, String>>(Pair("hash", swapRecord.txHash))
        KLog.i(dataJson.toString())
        var request = (ConstantValue.qlcHubEndPoint + "/info/swapInfoByTxHash").httpGet(list)
        DefiUtil.addRequestHeader(request)
        request.responseString { _, _, result ->
            val (data, error) = result
            KLog.i(data)
            if (error == null) {
                var checkHubState = Gson().fromJson<CheckHubState>(data, CheckHubState::class.java)
                if (checkHubState.state.toInt() == 0) {
                    //DepositInit == 0
                    swapRecord.state = checkHubState.state.toInt()
                    AppConfig.instance.daoSession.swapRecordDao.update(swapRecord)
//                    runOnUiThread {
//                        tvDot!!.text = "1"
//                    }
                    Thread.sleep(checkStatusTime)
                    checkLcokState()
                } else if (checkHubState.state.toInt() == 1) {
                    //DepositNeoLockedDone == 1
                    swapRecord.state = checkHubState.state.toInt()
                    AppConfig.instance.daoSession.swapRecordDao.update(swapRecord)
                    runOnUiThread {
                        tvDot!!.text = "7"
                        swapRecord.state = 1
                        AppConfig.instance.daoSession.swapRecordDao.update(swapRecord)
                        startActivity(Intent(activity, SwapDetailActivity::class.java).putExtra("swapRecord", swapRecord))
                        var newSwapRecord = SwapRecord()
                        newSwapRecord.isMainNet = SpUtil.getBoolean(activity, ConstantValue.isMainNet, true)
                        newSwapRecord.wrpperEthAddress = wrapperOnline.ethAddress
                        newSwapRecord.ethContractAddress = wrapperOnline.ethContract
                        newSwapRecord.wrapperNeoAddress = wrapperOnline.neoAddress
                        newSwapRecord.neoContractAddress = wrapperOnline.neoContract
                        newSwapRecord.type = SwapRecord.SwapType.typeNep5ToErc20.ordinal
                        newSwapRecord.fromAddress = swapRecord.fromAddress
                        swapRecord = newSwapRecord
                        sweetAlertDialogSwap!!.dismissWithAnimation()
                        toast(getString(R.string.success))
                        activity!!.finish()
                    }
                }
            } else {
                error.exception.printStackTrace()
                Thread.sleep(checkStatusTime)
                checkLcokState()
            }
        }
    }

    /**
     * 检查erc20 claim的状态
     */
    fun checkErc20Transaction() {
        runOnUiThread {
            tvDot!!.text = "6"
        }
        thread {
            try {
                val web3j = Web3j.build(org.web3j.protocol.http.HttpService(ConstantValue.ethNodeUrl))
                var transaction = web3j.ethGetTransactionByHash(swapRecord.swaptxHash).send()
                if (transaction.hasError()) {
                    KLog.i(transaction.error.message)
                    if ("Unknown transaction".equals(transaction.error.message)) {
                        Thread.sleep(checkStatusTime)
                        checkErc20Transaction()
                    }
                } else {
                    Thread.sleep(checkStatusTime)
                    checkLcokState()
                }
            } catch (e: Exception) {
                Thread.sleep(checkStatusTime)
                checkLcokState()
                e.printStackTrace()
            }
        }
    }

    var sweetAlertDialogSwap: SweetAlertDialog? = null
    var llDot: View? = null
    var tvDot: TextView? = null
    var isClose = false

    fun showSwapPup() {
        val view = layoutInflater.inflate(R.layout.alert_swap, null, false)
        llDot = view.findViewById(R.id.llDot)
        tvDot = view.findViewById(R.id.tvDot)
        tvDot!!.text = "1"
        sweetAlertDialogSwap = SweetAlertDialog(activity)
        var ivClose = view.findViewById<ImageView>(R.id.ivClose)
        ivClose.setOnClickListener {
            isClose = true
            getTestNetQlcBalance(wallet!!.address)
            etStakeQlcAmount.setText("")
            sweetAlertDialogSwap!!.dismissWithAnimation()
            var newSwapRecord = SwapRecord()
            newSwapRecord.isMainNet = SpUtil.getBoolean(activity, ConstantValue.isMainNet, true)
            newSwapRecord.wrpperEthAddress = wrapperOnline.ethAddress
            newSwapRecord.ethContractAddress = wrapperOnline.ethContract
            newSwapRecord.wrapperNeoAddress = wrapperOnline.neoAddress
            newSwapRecord.neoContractAddress = wrapperOnline.neoContract
            newSwapRecord.type = SwapRecord.SwapType.typeNep5ToErc20.ordinal
            newSwapRecord.fromAddress = swapRecord.fromAddress
            swapRecord = newSwapRecord
        }

        sweetAlertDialogSwap!!.setView(view)
        sweetAlertDialogSwap!!.show()
        scaleAnimationTo1(llDot!!)
    }

    fun scaleAnimationTo1(imageView: View) {
        sa1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                scaleAnimationToHalf(imageView)
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
        imageView.startAnimation(sa1)
    }

    fun scaleAnimationToHalf(imageView: View) {
        saHalf.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                scaleAnimationTo1(imageView)
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
        imageView.startAnimation(saHalf)
    }

    lateinit var saHalf: ScaleAnimation
    lateinit var sa1: ScaleAnimation

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AllWallet.WalletType.EthWallet.ordinal && resultCode == Activity.RESULT_OK) {
            ethWallet = data!!.getParcelableExtra("wallet")
            tvETHWalletName.text = ethWallet!!.name
            tvETHWalletAddess.text = ethWallet!!.address
            swapRecord.toAddress = ethWallet.address

            mPresenter.getEthGasPrice(hashMapOf())
            if (!"".equals(etStakeQlcAmount.text.toString())) {
                invoke.setBackgroundResource(R.drawable.main_color_bt_bg)
            } else {
                invoke.setBackgroundResource(R.drawable.unable_bt_bg)
            }
        }
    }

    lateinit var wallet: Wallet
    fun getNeoWallet() {
        var list = AppConfig.instance.daoSession.walletDao.queryBuilder().where(WalletDao.Properties.Address.eq(currentAddress)).list()
        wallet = list[0]
        tvNeoWalletAddess.text = wallet.address
        tvNeoWalletName.text = wallet.name
        swapRecord.fromAddress = wallet.address
        getTestNetQlcBalance(wallet.address)
    }

    override fun initDataFromNet() {
//        checkWrapperOnline()
    }

    var neoBalance = -1.toDouble()

    //获取测试网的qlc的余额
    fun getTestNetQlcBalance(address: String) {
        KLog.i("开始查询qlc余额。。。。")
        thread {
            try {
                var scriptHash = ScriptHash(ConstantValue.qlchash)

                val builder: ContractInvocation.Builder = ContractInvocation.Builder(neow3j)
                        .contractScriptHash(scriptHash)
                        .function("balanceOf")
                var parameter = ContractParameter.byteArrayFromAddress(Account.getWallet()!!.address)
                var result = builder.parameter(parameter).build().testInvoke()
                var balance = Test1Contract.streamResponse(result)!!.asByteArray().asNumber.toDouble() / 100000000.toDouble()
                KLog.i(balance)
                var tokenbalance = NeoWalletInfo.DataBean.BalanceBean()
                tokenbalance.asset_hash = ConstantValue.qlchash
                tokenbalance.asset_symbol = "QLC"
                tokenbalance.amount = balance.toDouble()
                neoBalance = tokenbalance.amount
                runOnUiThread {
                    tvQlcBalance?.text = getString(R.string.balance) + ": ${tokenbalance.amount} QLC"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    lateinit var wrapperOnline: WrapperOnline
    fun checkWrapperOnline() {
        var list = arrayListOf<Pair<String, String>>(Pair("value", ""))
        var request = (ConstantValue.qlcHubEndPoint + "/info/ping").httpGet(list)
        DefiUtil.addRequestHeader(request)
        request.responseString { _, _, result ->
            val (data, error) = result
            KLog.i(data)
            if (error == null) {
                if (isFinish) {
                    return@responseString
                }
                try {
                    wrapperOnline = Gson().fromJson<WrapperOnline>(data, WrapperOnline::class.java)
                    KLog.i(wrapperOnline.toString())
                    swapRecord.wrpperEthAddress = wrapperOnline.ethAddress
                    swapRecord.ethContractAddress = wrapperOnline.ethContract
                    swapRecord.wrapperNeoAddress = wrapperOnline.neoAddress
                    swapRecord.neoContractAddress = wrapperOnline.neoContract
                    runOnUiThread {
                        miniSwapQlc.text = getString(R.string.the_minimum_stake_amount_is_1_s_qlc, "1")
                        miniSwapQlc.text = getString(R.string.the_minimum_stake_amount_is_1_s_qlc, (1).toString())
                        etStakeQlcAmount.hint = getString(R.string.from_1_s_qlc, (1).toString())
                        etStakeQlcAmount.hint = getString(R.string.from_1_s_qlc, "1")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {

            }
        }
    }

    var isFinish = false
    override fun onDestroy() {
        isFinish = true
        super.onDestroy()
    }


    override fun setupFragmentComponent() {
        DaggerSwapComponent
                .builder()
                .appComponent((activity!!.application as AppConfig).applicationComponent)
                .swapModule(SwapModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: SwapContract.SwapContractPresenter) {
        mPresenter = presenter as SwapPresenter
    }

    override fun initDataFromLocal() {

    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }
}