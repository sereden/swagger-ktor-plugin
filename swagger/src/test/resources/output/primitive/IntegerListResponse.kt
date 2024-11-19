package com.test

import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class IntegerListResponse(
    /**
     * Description
     */
    @SerialName("properties")
    public val properties: List<Int>,
)