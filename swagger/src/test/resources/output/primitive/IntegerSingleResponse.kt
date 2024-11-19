package com.test

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class IntegerSingleResponse(
    @SerialName("property")
    public val `property`: Int,
)