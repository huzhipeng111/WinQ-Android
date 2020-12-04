package com.stratagile.qlink.ui.activity.stake

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.qlink.Account
import com.stratagile.qlink.R

import com.stratagile.qlink.application.AppConfig
import com.stratagile.qlink.base.BaseActivity
import com.stratagile.qlink.constant.ConstantValue
import com.stratagile.qlink.entity.stake.*
import com.stratagile.qlink.hexStringToByteArray
import com.stratagile.qlink.toHex
import com.stratagile.qlink.ui.activity.stake.component.DaggerStakeDetailComponent
import com.stratagile.qlink.ui.activity.stake.contract.StakeDetailContract
import com.stratagile.qlink.ui.activity.stake.module.StakeDetailModule
import com.stratagile.qlink.ui.activity.stake.presenter.StakeDetailPresenter
import com.stratagile.qlink.utils.*
import eu.chainfire.libsuperuser.Application.toast
//import io.neow3j.crypto.ECKeyPair
//import io.neow3j.crypto.Sign
//import io.neow3j.utils.Numeric
import kotlinx.android.synthetic.main.activity_stake_detail.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import neoutils.Neoutils
import qlc.bean.StateBlock
import qlc.mng.BlockMng
import qlc.mng.WalletMng
import qlc.network.QlcClient
import qlc.utils.Helper
import wendu.dsbridge.DWebView
import wendu.dsbridge.OnReturnValue
import java.math.BigDecimal

import javax.inject.Inject;
import kotlin.concurrent.thread

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.stake
 * @Description: $description
 * @date 2019/08/09 15:26:02
 */

class StakeDetailActivity : BaseActivity(), StakeDetailContract.View {

    override fun sign(unLock: UnLock) {
        try {
            val signature = Neoutils.sign(unLock.data.result.unsignedRawTx.hexStringToByteArray(), neoWallet!!.privateKey)
            KLog.i(signature.toHex())
//            var eckey =
//            KLog.i(eckey.address)
//            val signature = Sign.signMessage(unLock.data.result.unsignedRawTx.hexStringToByteArray(), ECKeyPair.create(Numeric.toBigInt(neoWallet!!.privateKey)))
//            KLog.i(signature.concatenated.toHex().toLowerCase())
            unLock.data.result.signature = signature.toHex().toLowerCase()
            unLock.data.result.publicKey = neoWallet!!.publicKey.toLowerCase()
            thread {
                benefitWithdraw(unLock)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { toast(getString(R.string.revoke_failed)) }
            closeProgressDialog()
        }
    }

    @Inject
    internal lateinit var mPresenter: StakeDetailPresenter

    lateinit var myStake: MyStakeList.ResultBean

    lateinit var webview: DWebView

    var isFinish = false
    var checkCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        mainColor = R.color.white
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        isFinish = true
    }

    override fun initView() {
        setContentView(R.layout.activity_stake_detail)
//        getNep5Txid("08b2534acb7697f842769254602b5a1fba97e3c933b4d072dab86e7fba6ca3bc")
    }

    override fun initData() {
        myStake = intent.getParcelableExtra("stake")
        KLog.i(myStake.toString())
        when (myStake.pType) {
            "vote" -> {
                var drawable = getResources().getDrawable(R.mipmap.mining_node_a)
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight())
                title.setCompoundDrawables(drawable, null, null, null)
                title.compoundDrawablePadding = UIUtils.dip2px(6f, this)
                title.text = getString(R.string.vote_mining_node)
                tvStakeType.text = getString(R.string.for_voting_mining_node)
            }
        }
        tvQlcCount1.text = myStake.amount.toBigDecimal().divide(BigDecimal.TEN.pow(8), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString() + " QLC"
        tvQlcCount.text = myStake.amount.toBigDecimal().divide(BigDecimal.TEN.pow(8), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
        tvVoteEarnings.text = myStake.qgas.toBigDecimal().divide(BigDecimal.TEN.pow(8), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
        tvNeoAddress.text = myStake.multiSigAddress
        tvQlcAddress.text = myStake.beneficial
        tvNeoTxid.text = myStake.nep5TxId
        tvNeoAddress.setOnClickListener {
            StakeUtils.copyTxetToClip(this, tvNeoAddress)
        }
        tvQlcAddress.setOnClickListener {
            StakeUtils.copyTxetToClip(this, tvQlcAddress)
        }
        tvNeoTxid.setOnClickListener {
            StakeUtils.copyTxetToClip(this, tvNeoTxid)
        }
        when (myStake.state) {
            "PledgeStart" -> {
                tvStakeStatus.text = getString(R.string.not_succeed_continue_to_invoke)
                tvStakeStatus.setTextColor(resources.getColor(R.color.color_108ee9))
                optionRevokeTime.text = "-/-"
                getLockInfo1()
            }
            "PledgeProcess" -> {
                tvStakeStatus.text = getString(R.string.not_succeed_continue_to_invoke)
                tvStakeStatus.setTextColor(resources.getColor(R.color.color_108ee9))
                optionRevokeTime.text = "-/-"
                getLockInfo1()
            }
            "PledgeDone" -> {
                if (System.currentTimeMillis() > myStake.withdrawTime * 1000) {
                    tvStakeStatus.text = getString(R.string.revoke_stake)
                    tvStakeStatus.setTextColor(resources.getColor(R.color.color_108ee9))
                    optionRevokeTime.text = TimeUtil.getRevokeTime(myStake.withdrawTime)
                } else {
                    tvStakeStatus.text = getString(R.string.staking_in_progress)
                    tvStakeStatus.setTextColor(resources.getColor(R.color.color_29282a))
                    optionRevokeTime.text = TimeUtil.getRevokeTime(myStake.withdrawTime)
                }
                getLockInfo1()
            }
            "WithdrawStart" -> {
                tvStakeStatus.text = getString(R.string.revoke_stake)
                tvStakeStatus.setTextColor(resources.getColor(R.color.color_108ee9))
                optionRevokeTime.text = TimeUtil.getRevokeTime(myStake.withdrawTime)
                getLockInfo1()
            }
            "WithdrawProcess" -> {
                tvStakeStatus.text = getString(R.string.revoke_failed1)
                tvStakeStatus.setTextColor(resources.getColor(R.color.color_29282a))
                optionRevokeTime.text = TimeUtil.getRevokeTime(myStake.withdrawTime)
                getLockInfo1()
            }
            "WithdrawDone" -> {
                tvStakeStatus.text = getString(R.string.withdrawal_successful)
                tvStakeStatus.setTextColor(resources.getColor(R.color.color_0cb8ae))
                optionRevokeTime.text = TimeUtil.getRevokeTime(myStake.withdrawTime)
//                getLockInfo1()
            }
        }
        tvStakeStatus.setOnClickListener {
            when (myStake.state) {
                "PledgeStart" -> {
                    when (myStake.pType) {
                        "vote" -> {
                            showProgressDialog()
                            getNep5Txid()
                        }
                    }
                }
                "PledgeProcess" -> {
                    when (myStake.pType) {
                        "vote" -> {
                            showProgressDialog()
                            getNep5Txid()
                        }
                    }
                }
                "PledgeDone" -> {
                    FireBaseUtils.logEvent(this, FireBaseUtils.Wallet_MyStakings_InvokeNewStakings_Invoke)
                    if (System.currentTimeMillis() > myStake.withdrawTime * 1000) {
                        showProgressDialog()
                        thread {
                            getNep5Txid(myStake.nep5TxId)
//                            getNeoAddressPubKey("AXa39WUxN6rXjRMt36Zs88XZi5hZHcF8GK")
                        }
                    }
                }
                "WithdrawStart" -> {
                    if (System.currentTimeMillis() > myStake.withdrawTime * 1000) {
                        showProgressDialog()
                        thread {
                            getNep5Txid(myStake.nep5TxId)
//                            getNeoAddressPubKey("AXa39WUxN6rXjRMt36Zs88XZi5hZHcF8GK")
                        }
                    }
                }
                "WithdrawProcess" -> {
                    if (System.currentTimeMillis() > myStake.withdrawTime * 1000) {
                        showProgressDialog()
                        thread {
                            getNep5Txid(myStake.nep5TxId)
//                            getNeoAddressPubKey("AXa39WUxN6rXjRMt36Zs88XZi5hZHcF8GK")
                        }
                    }
                }
                "WithdrawDone" -> {
                }
            }
        }
    }

    fun unLockQLc(neoPubKey: String) {
        var map = hashMapOf<String, String>()
        map["multisigAddress"] = myStake.multiSigAddress
        map["txid"] = myStake.nep5TxId
        mPresenter.unLock(map)
    }

    fun getNep5Txid(txid: String) {
        thread {
            val dataJson = jsonObject(
                    "jsonrpc" to "2.0",
                    "method" to "nep5_getLockInfo",
                    "params" to jsonArray(txid),
                    "id" to 3
            )
            var request = ConstantValue.qlcStakeNode.httpPost().body(dataJson.toString())
            request.headers["Content-Type"] = "application/json"
            request.responseString { _, _, result ->
                val (data, error) = result
                if (error == null) {
                    val gson = Gson()
                    KLog.i(data!!)
                    val nodeResponse = gson.fromJson<LockInfo>(data, object : TypeToken<LockInfo>() {}.type)
                    KLog.i(nodeResponse.toString())
                    if (nodeResponse.result != null) {
                        getNeoAddressPubKey(nodeResponse.result.neoAddress)
                    } else {
                        launch {
                            delay(3000)
                            getNep5Txid(txid)
                        }
                    }
                } else {
                    launch {
                        delay(3000)
                        getNep5Txid(txid)
                    }
                    KLog.i(error.localizedMessage)
                }
            }
        }
    }

    var neoWallet: com.stratagile.qlink.db.Wallet? = null
    fun getNeoAddressPubKey(address: String) {
        var walletList = AppConfig.instance.daoSession.walletDao.loadAll()
        neoWallet = walletList.find { it.address.equals(address) }
        KLog.i(neoWallet.toString())
        if (neoWallet != null) {
            unLockQLc(neoWallet!!.publicKey)
        } else {
            runOnUiThread {
                toast(getString(R.string.stake_neo_wallet_not_found, address))
                closeProgressDialog()
            }

        }
    }


    fun getNep5TxidWithDraw(string: String) {
        if (isFinish) {
            return
        }
        thread {
            val dataJson = jsonObject(
                    "jsonrpc" to "2.0",
                    "method" to "getnep5transferbytxid",
                    "params" to jsonArray(string),
                    "id" to 3
            )
//            var request = "https://api.nel.group/api/mainnet".httpPost().body(dataJson.toString())
            var request = "${ConstantValue.neoTranscationNode}${string}".httpGet()
            request.headers["Content-Type"] = "application/json"
            request.responseString { _, _, result ->
                val (data, error) = result
                if (error == null) {
                    val gson = Gson()
                    KLog.i(data!!)
                    val nodeResponse = gson.fromJson<NeoTransactionInfo>(data, object : TypeToken<NeoTransactionInfo>() {}.type)
                    KLog.i(nodeResponse.toString())
                    if (nodeResponse.errors == null && nodeResponse.block_hash != null) {
                        runOnUiThread {
                            closeProgressDialog()
                            toast(getString(R.string.revoke_success))
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    } else {
                        launch {
                            delay(3000)
                            getNep5TxidWithDraw(string)
                        }
                    }
                } else {
                    launch {
                        delay(3000)
                        getNep5TxidWithDraw(string)
                    }
                    KLog.i(error.localizedMessage)
                }
            }
        }
    }


    fun benefitWithdraw(unLock: UnLock) {
        if (isFinish) {
            return
        }
        try {
            val client = QlcClient(ConstantValue.qlcStakeNode)
            val params = JSONArray()
            val paramOne = JSONObject()
            paramOne["beneficial"] = myStake.beneficial
            paramOne["amount"] = myStake.amount.toString()
            paramOne["pType"] = "vote"
            params.add(paramOne)
            params.add(myStake.nep5TxId)
            val result = client.call("nep5_benefitWithdraw", params)
            var stateBlock = Gson().fromJson(result.getJSONObject("result").toString(), StateBlock::class.java)
            KLog.i(result.toJSONString())
            var root = BlockMng.getRoot(stateBlock)
            var params1 = mutableListOf<Pair<String, String>>()
            params1.add(Pair("root", root))
            var request = "${ConstantValue.qlcWorkNode}".httpGet(params1)
            request.timeout(60000)
            request.responseString { _, _, result ->
                KLog.i("远程work返回、、")
                val (data, error) = result
                if (error == null) {
                    KLog.i("work正确、、、---->>> " + data)
                    try {
                        stateBlock.work = data
                        var hash = BlockMng.getHash(stateBlock)
                        KLog.i(QlcReceiveUtils.drivePrivateKey(myStake.beneficial).substring(0, 64))
                        var signature = WalletMng.sign(hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePrivateKey(myStake.beneficial).substring(0, 64)))
                        val signCheck = WalletMng.verify(signature, hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePublicKey(myStake.beneficial)))
                        if (!signCheck) {
                            KLog.i("签名验证失败")
                            return@responseString
                        }
                        stateBlock.setSignature(Helper.byteToHexString(signature))
                        processAsyncWithdraw(stateBlock, unLock)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } else {
                    AppConfig.instance.saveLog("stake", "benefitWithdraw" + getLine(), "work error")
//                    runOnUiThread {
//                        toast(getString(R.string.process_error_please_try_later))
//                    }
                    KLog.i(error.response)
                    KLog.i(error.exception)
                    KLog.i("work出错、、、")

                    benefitWithdraw(unLock)
                }
            }
        } catch (e: Exception) {
            toast(getString(R.string.revoke_failed))
            e.printStackTrace()
            closeProgressDialog()
        }
    }

    fun mintageWithdraw() {

    }

    fun getNep5Txid() {
        thread {
//            getLockInfo()
//            val dataJson = jsonObject(
//                    "jsonrpc" to "2.0",
//                    "method" to "getnep5transferbytxid",
//                    "params" to jsonArray(myStake.nep5TxId),
//                    "id" to 3
//            )
//            var request = "https://api.nel.group/api/mainnet".httpPost().body(dataJson.toString())
            var request = "${ConstantValue.neoTranscationNode}${myStake.nep5TxId}".httpGet()
            request.headers["Content-Type"] = "application/json"
            request.responseString { _, _, result ->
                val (data, error) = result
                if (error == null) {
                    val gson = Gson()
                    KLog.i(data!!)
                    val nodeResponse = gson.fromJson<NeoTransactionInfo>(data, object : TypeToken<NeoTransactionInfo>() {}.type)
                    KLog.i(nodeResponse.toString())
                    if (nodeResponse.errors == null && nodeResponse.block_hash != null) {
                        getLockInfo()
                    } else {
                        launch {
                            delay(3000)
                            getNep5Txid()
                        }
                    }
                }
//                else {
//                    launch {
//                        delay(3000)
//                        getNep5Txid()
//                    }
//                    KLog.i(error.localizedMessage)
//                }
            }
        }
    }

    fun getLockInfo() {
        thread {
            try {
                val client = QlcClient(ConstantValue.qlcStakeNode)
                val params = JSONArray()
                params.add(myStake.nep5TxId)
                var result = client.call("nep5_getLockInfo", params)
                KLog.i(result.toJSONString())
                var lockInfo = Gson().fromJson(result.toJSONString(), LockInfo::class.java)

                if (lockInfo.result.isState) {
                    var lockResult = LockResult()
                    var stakeType = StakeType(0)
                    lockResult.stakeType = stakeType
                    lockResult.stakeType.fromNeoAddress = lockInfo.result.neoAddress
                    lockResult.stakeType.toAddress = lockInfo.result.multiSigAddress
                    lockResult.stakeType.qlcchainAddress = lockInfo.result.qlcAddress
                    lockResult.txid = myStake.nep5TxId
                    var oneDay = 60 * 60 * 24
                    var lockDays = (lockInfo.result.unLockTimestamp - lockInfo.result.lockTimestamp) / oneDay

                    lockResult.stakeType.stakeQLcAmount = lockInfo.result.amount.toBigDecimal().divide(10.toBigDecimal().pow(8), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
                    var neoList = AppConfig.instance.daoSession.walletDao.loadAll()
                    neoList.forEach {
                        if (it.address.equals(lockResult.stakeType.fromNeoAddress)) {
                            neoWallet = it
                            if (neoWallet!!.privateKey.toLowerCase().equals(neoWallet!!.publicKey.toLowerCase())) {

                                Account.fromWIF(neoWallet!!.wif)
//                                neoWallet!!.publicKey = Numeric.toHexStringWithPrefix(Account.getWallet()!!.publicKey)
                                neoWallet!!.publicKey = Account.byteArray2String(Account.getWallet()!!.publicKey).toLowerCase()
                                AppConfig.instance.daoSession.walletDao.update(neoWallet)
                            }
                        }
                    }

                    if (neoWallet == null) {
                        runOnUiThread {
                            closeProgressDialog()
                            toast(getString(R.string.stake_neo_wallet_not_found))
                        }
                    } else {
                        var qlcWallets = AppConfig.instance.daoSession.qlcAccountDao.loadAll()
                        var hasQlcWallet = false
                        qlcWallets.forEach {
                            if (it.address.equals(lockResult.stakeType.qlcchainAddress)) {
                                hasQlcWallet = true
                            }
                        }
                        if (hasQlcWallet) {
                            lockResult.stakeType.neoPriKey = neoWallet!!.privateKey
                            lockResult.stakeType.neoPubKey = neoWallet!!.publicKey
                            prePareBenefitPledge(lockResult = lockResult)
                        } else {
                            runOnUiThread {
                                closeProgressDialog()
                                toast(getString(R.string.qlc_wallet_not_found))
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        closeProgressDialog()
                        toast(getString(R.string.txid_is_unlock))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    closeProgressDialog()
                    toast(getString(R.string.get_lockinfo_error))
                }
            }
        }
    }

    //这个只是用来显示抵押的neo钱包地址
    fun getLockInfo1() {
        thread {
            try {
                val client = QlcClient(ConstantValue.qlcStakeNode)
                val params = JSONArray()
                params.add(myStake.nep5TxId)
                var result = client.call("nep5_getLockInfo", params)
                KLog.i(result.toJSONString())
                var lockInfo = Gson().fromJson(result.toJSONString(), LockInfo::class.java)
                if (lockInfo.result.isState) {
                    runOnUiThread {
                        tvNeoAddress.text = lockInfo.result.neoAddress
//                        var walletList = AppConfig.instance.daoSession.walletDao.loadAll()
//                        neoWallet = walletList.find { it.address.equals(lockInfo.result.neoAddress) }
//                        KLog.i(neoWallet.toString())
//                        KLog.i(Neoutils.neoAddressToScriptHash(neoWallet!!.address))
                    }
                } else {
                    runOnUiThread {
                        toast(getString(R.string.txid_is_unlock))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    closeProgressDialog()
                    toast(getString(R.string.get_lockinfo_error))
                }
            }
        }
    }

    fun prePareBenefitPledge(lockResult: LockResult) {
        val client = QlcClient(ConstantValue.qlcStakeNode)
        val params = JSONArray()
        val paramOne = JSONObject()
        paramOne["beneficial"] = myStake.beneficial
        paramOne["amount"] = myStake.amount
        paramOne["pType"] = "vote"
        params.add(paramOne)
        val paramTwo = JSONObject()
        paramTwo["multiSigAddress"] = lockResult.stakeType.toAddress
        paramTwo["publicKey"] = lockResult.stakeType.neoPubKey
        paramTwo["lockTxId"] = myStake.nep5TxId
        params.add(paramTwo)
        val result = client.call("nep5_prePareBenefitPledge", params)
        KLog.i(result)
        benefitPledgeAsync()
    }

    fun benefitPledgeAsync() {
        val client = QlcClient(ConstantValue.qlcStakeNode)
        val params = JSONArray()
        params.add(myStake.nep5TxId)
        var result = client.call("nep5_benefitPledgeAsync", params)
        KLog.i(result.toString())
        benefitPledgeCheck(result.getString("result"))
    }

    fun benefitPledgeCheck(checkId: String) {
        if (isFinish) {
            return
        }
        if (checkCount >= 10) {
            toast(getString(R.string.stake_failed_please_try_it_later))
            closeProgressDialog()
            return
        }
        checkCount++
        val client = QlcClient(ConstantValue.qlcStakeNode)
        val params = JSONArray()
        params.set(0, checkId)
        KLog.i("checkId= $checkId")
        val result = client.call("nep5_benefitPledgeCheck", params)
        KLog.i(result)
        if (result.getJSONObject("error") != null) {
            if ("NotCompleted".equals(result.getJSONObject("error").getString("message"))) {
                Thread.sleep(5000)
                benefitPledgeCheck(checkId)
            } else {
                runOnUiThread {
                    toast(result.getJSONObject("error").getString("message"))
                }
            }
        } else {
            checkCount = 0
            var stateBlock = Gson().fromJson(result.getJSONObject("result").toString(), StateBlock::class.java)
            var root = BlockMng.getRoot(stateBlock)
            var params1 = mutableListOf<Pair<String, String>>()
            params1.add(Pair("root", root))
            var request = "${ConstantValue.qlcWorkNode}".httpGet(params1)
            request.responseString { _, _, result ->
                KLog.i("远程work返回、、")
                val (data, error) = result
                try {
                    if (error == null) {
                        stateBlock.work = data
                        KLog.i(data)
                        var hash = BlockMng.getHash(stateBlock)
                        var signature = WalletMng.sign(hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePrivateKey(myStake.beneficial).substring(0, 64)))
                        val signCheck = WalletMng.verify(signature, hash, Helper.hexStringToBytes(QlcReceiveUtils.drivePublicKey(myStake.beneficial)))
                        if (!signCheck) {
                            KLog.i("签名验证失败")
                            return@responseString
                        }
                        stateBlock.setSignature(Helper.byteToHexString(signature))
                        processAsync(stateBlock)
                    } else {

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun processAsyncWithdraw(stateBlock: StateBlock, unLock: UnLock) {
        try {
            val client = QlcClient(ConstantValue.qlcStakeNode)
            val params = JSONArray()
            params.add(JSONObject.parse(Gson().toJson(stateBlock)))
            params.add(myStake.nep5TxId)
            params.add(JSONObject.parse(Gson().toJson(unLock.data.result)))
            KLog.i(params)
            KLog.i("开始赎回")
            val result = client.call("ledger_processAsync", params)
            KLog.i(result)
            processCheckWithdraw(unLock, result.getString("result"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun processCheckWithdraw(unLock: UnLock, checkId: String) {
        if (isFinish) {
            return
        }
        if (checkCount >= 10) {
            runOnUiThread {
                toast(getString(R.string.revoke_failed_please_try_it_later))
            }
            closeProgressDialog()
            return
        }
        checkCount++
        try {
            val client = QlcClient(ConstantValue.qlcStakeNode)
            val params = JSONArray()
            params.set(0, checkId)
            KLog.i("checkId= $checkId")
            val result = client.call("ledger_processCheck", params)
            KLog.i(result)
            if (result.getJSONObject("error") != null) {
                if ("NotCompleted".equals(result.getJSONObject("error").getString("message"))) {
                    Thread.sleep(5000)
                    processCheckWithdraw(unLock, checkId)
                } else {
                    runOnUiThread {
                        toast(result.getJSONObject("error").getString("message"))
                    }
                }
            } else {
                checkCount = 0
                getNep5TxidWithDraw(unLock.data.result.unlockTxId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun processAsync(stateBlock: StateBlock) {
        val client = QlcClient(ConstantValue.qlcStakeNode)
        val params = JSONArray()
        params.add(JSONObject.parse(Gson().toJson(stateBlock)))
        params.add(myStake.nep5TxId)

        val result = client.call("ledger_processAsync", params)
        KLog.i(result)
        processCheck(stateBlock, result.getString("result"))
    }

    fun processCheck(stateBlock: StateBlock, checkId: String) {
        if (isFinish) {
            return
        }
        if (checkCount >= 10) {
            toast(getString(R.string.stake_failed_please_try_it_later))
            closeProgressDialog()
            return
        }
        checkCount++
        val client = QlcClient(ConstantValue.qlcStakeNode)
        val params = JSONArray()
        params.set(0, checkId)
        KLog.i("checkId= $checkId")
        val result = client.call("ledger_processCheck", params)
        KLog.i(result)
        if (result.getJSONObject("error") != null) {
            if ("NotCompleted".equals(result.getJSONObject("error").getString("message"))) {
                Thread.sleep(5000)
                processCheck(stateBlock, checkId)
            } else {
                runOnUiThread {
                    toast(result.getJSONObject("error").getString("message"))
                }
            }
        } else {
            checkCount = 0
            runOnUiThread {
                closeProgressDialog()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun setupActivityComponent() {
        DaggerStakeDetailComponent
                .builder()
                .appComponent((application as AppConfig).applicationComponent)
                .stakeDetailModule(StakeDetailModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: StakeDetailContract.StakeDetailContractPresenter) {
        mPresenter = presenter as StakeDetailPresenter
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

}