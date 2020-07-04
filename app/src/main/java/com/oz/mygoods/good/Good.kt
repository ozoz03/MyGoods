package com.oz.mygoods.good

import com.google.gson.annotations.SerializedName

data class Good(
    @SerializedName("id")
    var id: Long = 0,

    @SerializedName("name")
    var name: String? = null,


    @SerializedName("needed")
    var needed: Boolean? = null
){
    override fun toString(): String {
        return name ?: "Unknown #$id"
    }
}