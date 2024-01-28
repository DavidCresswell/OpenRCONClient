// Copyright (C) 2024 David Cresswell
// Licensed under GPLv3

package uk.cresswell.rcon

import java.io.Serializable

data class Server(val name: String, val address: String, val port: Int, val password: String) : Serializable {
    override fun toString(): String {
        return name
    }
}