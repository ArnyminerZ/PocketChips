package com.arnyminerz.pocketchips.game.requests

import com.arnyminerz.pocketchips.game.response.GameStatus

const val REQUEST_STATUS = "status"

object GameStatusRequest: Request<GameStatus, GameStatus.Companion>(REQUEST_STATUS)
