package com.stratagile.qlink.ui.adapter.defi

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stratagile.qlink.R
import com.stratagile.qlink.db.SwapRecord
import com.stratagile.qlink.utils.TimeUtil
import kotlinx.android.synthetic.main.activity_swap_detail.*

class SwapListAdapter(array: ArrayList<SwapRecord>) : BaseQuickAdapter<SwapRecord, BaseViewHolder>(R.layout.item_swap_list, array) {
    override fun convert(helper: BaseViewHolder, item: SwapRecord) {
        helper.setText(R.id.tvLockTime, TimeUtil.getOrderTime(item.lockTime))
        helper.setText(R.id.qlcAmount, item.amount.toString())
        helper.setVisible(R.id.tvOperator, false)
        // 1为nep5 --> erc20, 2为erc20 --> nep5
        if (item.type == 1) {
            //nep5 -> erc20
            /**
             * // deposit
            DepositInit LockerState = iota
            DepositNeoLockedDone
            DepositEthLockedPending
            //3 可以开始领取
            DepositEthLockedDone
            //
            DepositEthUnLockedDone
            // 5
            DepositNeoUnLockedPending
            //6
            DepositNeoUnLockedDone
            //7抵押超时，wrapper正在销毁eth
            DepositEthFetchPending
            //8抵押超时，wrapper销毁eth完成
            DepositEthFetchDone
            //9抵押超时，nep5退回完成
            DepositNeoFetchDone

            Failed
            Invalid
             */
            helper.setText(R.id.tvSwapType, mContext.resources.getString(R.string.nep5_token_erc20_token))
            when(item.state) {
                -1 -> {
                    helper.setText(R.id.tvSwapState, mContext.getString(R.string.waiting_for_confirm))
                    helper.setVisible(R.id.tvOperator, true)
                    helper.setText(R.id.tvOperator, mContext.getString(R.string.confirm))
                }
                0 -> {
                    if (item.swaptxHash == null || item.swaptxHash.equals("")) {
                        helper.setVisible(R.id.tvOperator, true)
                        helper.setText(R.id.tvSwapState, mContext.getString(R.string.waiting_for_confirm))
                    } else {
                        helper.setText(R.id.tvSwapState, mContext.getString(R.string.pending))
                    }
                }

                1 -> {
                    helper.setText(R.id.tvSwapState, mContext.getString(R.string.completed))
                }

            }
        } else {
            /**
             *  // withdraw
            //10 eth交易确认
            WithDrawEthLockedDone
            11 locknep5中
            WithDrawNeoLockedPending
            12 nep5lock成功，可以开始赎回
            WithDrawNeoLockedDone
            13 nep5赎回成功，app端作为成功状态
            WithDrawNeoUnLockedDone
            14 wrapper 开始拿走erc20
            WithDrawEthUnlockPending
            15 wrapper 成功拿走erc20
            WithDrawEthUnlockDone
            16 赎回超时，wrapper开始取回nep5
            WithDrawNeoFetchPending
            17 赎回超时，wrapper成功取回nep5
            WithDrawNeoFetchDone
            18 赎回超时，用户取回erc20成功
            WithDrawEthFetchDone

            Failed
            Invalid
             */
            helper.setText(R.id.tvSwapType, mContext.resources.getString(R.string.qlc_erc20_token_nep5_token))
            when(item.state) {
                -2 -> {
                    helper.setText(R.id.tvSwapState, mContext.getString(R.string.failed))
                }

                2 -> {
                    helper.setText(R.id.tvSwapState, mContext.getString(R.string.pending))
                }

                3 -> {
                    helper.setText(R.id.tvSwapState, mContext.getString(R.string.completed))
                }

            }
        }
        helper.addOnClickListener(R.id.tvOperator)
    }
}