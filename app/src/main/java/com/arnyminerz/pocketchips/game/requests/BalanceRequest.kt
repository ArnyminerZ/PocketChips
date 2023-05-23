package com.arnyminerz.pocketchips.game.requests

import com.arnyminerz.pocketchips.game.response.Balance

const val REQUEST_BALANCE = "balance"

object BalanceRequest: Request<Balance, Balance.Companion>(REQUEST_BALANCE)
