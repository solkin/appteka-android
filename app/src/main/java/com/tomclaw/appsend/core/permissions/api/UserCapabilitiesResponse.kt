package com.tomclaw.appsend.core.permissions.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.util.GsonModel

/**
 * Response of `/api/1/user/capabilities` — a snapshot of every global,
 * non-resource-scoped action the viewer can perform. Fetched once at
 * session start and refreshed when the viewer logs in or when the
 * server signals an ACL change.
 *
 * `role` and `accessList` are echoed for convenience — clients should
 * still drive UI off [capabilities] rather than re-deriving decisions
 * from raw rules.
 */
@GsonModel
data class UserCapabilitiesResponse(
    @SerializedName("role")
    val role: Int,
    @SerializedName("access_list")
    val accessList: List<Int>?,
    @SerializedName("capabilities")
    val capabilities: Map<String, Capability>?,
)
