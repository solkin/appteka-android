package com.tomclaw.appsend.util.bdui.parser

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiAction
import com.tomclaw.appsend.util.bdui.model.action.BduiCallbackAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcResponse
import com.tomclaw.appsend.util.bdui.model.action.BduiSequenceAction
import com.tomclaw.appsend.util.bdui.model.action.BduiTransformAction
import com.tomclaw.appsend.util.bdui.model.component.BduiButtonComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiCardComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiCheckboxComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiChipComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiChipGroupComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiDividerComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiFabComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiHiddenComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiIconButtonComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiIconComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiImageComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiInputComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiProgressComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiRadioComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiRadioGroupComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiRatingComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiSliderComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiSpaceComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiSwitchComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiTextComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiToolbarComponent
import com.tomclaw.appsend.util.bdui.model.container.BduiContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiFlipperContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiFrameContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiLinearContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiRecyclerContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiScrollContainer
import com.tomclaw.appsend.util.bdui.model.transform.BduiBatchTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiPropertyTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiTransform
import java.lang.reflect.Type

/**
 * JSON parser for BDUI schemas.
 * Provides a pre-configured Gson instance with type adapters for polymorphic types.
 */
object BduiJsonParser {

    /**
     * Pre-configured Gson instance for parsing BDUI schemas.
     */
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BduiNode::class.java, BduiNodeDeserializer())
        .registerTypeAdapter(BduiAction::class.java, BduiActionDeserializer())
        .registerTypeAdapter(BduiTransform::class.java, BduiTransformDeserializer())
        .create()

    /**
     * Parses a JSON string into a BduiNode.
     *
     * @param json JSON string representing a BDUI schema
     * @return Parsed BduiNode
     * @throws JsonParseException if parsing fails
     */
    fun parseNode(json: String): BduiNode {
        return gson.fromJson(json, BduiNode::class.java)
    }

    /**
     * Parses a JSON string into a BduiRpcResponse.
     *
     * @param json JSON string representing an RPC response
     * @return Parsed BduiRpcResponse
     * @throws JsonParseException if parsing fails
     */
    fun parseRpcResponse(json: String): BduiRpcResponse {
        return gson.fromJson(json, BduiRpcResponse::class.java)
    }

    /**
     * Parses a JSON string into a BduiAction.
     *
     * @param json JSON string representing an action
     * @return Parsed BduiAction
     * @throws JsonParseException if parsing fails
     */
    fun parseAction(json: String): BduiAction {
        return gson.fromJson(json, BduiAction::class.java)
    }
}

/**
 * Deserializer for BduiNode polymorphic type.
 * Routes to appropriate container or component based on "type" field.
 */
class BduiNodeDeserializer : JsonDeserializer<BduiNode> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BduiNode {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString
            ?: throw JsonParseException("BduiNode requires 'type' field")

        val targetClass: Class<out BduiNode> = when (type) {
            // Containers
            BduiFrameContainer.TYPE -> BduiFrameContainer::class.java
            BduiLinearContainer.TYPE -> BduiLinearContainer::class.java
            BduiRecyclerContainer.TYPE -> BduiRecyclerContainer::class.java
            BduiScrollContainer.TYPE -> BduiScrollContainer::class.java
            BduiFlipperContainer.TYPE -> BduiFlipperContainer::class.java

            // Components
            BduiHiddenComponent.TYPE -> BduiHiddenComponent::class.java
            BduiTextComponent.TYPE -> BduiTextComponent::class.java
            BduiButtonComponent.TYPE -> BduiButtonComponent::class.java
            BduiIconButtonComponent.TYPE -> BduiIconButtonComponent::class.java
            BduiFabComponent.TYPE -> BduiFabComponent::class.java
            BduiImageComponent.TYPE -> BduiImageComponent::class.java
            BduiIconComponent.TYPE -> BduiIconComponent::class.java
            BduiInputComponent.TYPE -> BduiInputComponent::class.java
            BduiSwitchComponent.TYPE -> BduiSwitchComponent::class.java
            BduiCheckboxComponent.TYPE -> BduiCheckboxComponent::class.java
            BduiRadioComponent.TYPE -> BduiRadioComponent::class.java
            BduiRadioGroupComponent.TYPE -> BduiRadioGroupComponent::class.java
            BduiChipComponent.TYPE -> BduiChipComponent::class.java
            BduiChipGroupComponent.TYPE -> BduiChipGroupComponent::class.java
            BduiProgressComponent.TYPE -> BduiProgressComponent::class.java
            BduiSliderComponent.TYPE -> BduiSliderComponent::class.java
            BduiRatingComponent.TYPE -> BduiRatingComponent::class.java
            BduiCardComponent.TYPE -> BduiCardComponent::class.java
            BduiDividerComponent.TYPE -> BduiDividerComponent::class.java
            BduiSpaceComponent.TYPE -> BduiSpaceComponent::class.java
            BduiToolbarComponent.TYPE -> BduiToolbarComponent::class.java

            else -> throw JsonParseException("Unknown BduiNode type: $type")
        }

        return context.deserialize(json, targetClass)
    }
}

/**
 * Deserializer for BduiAction polymorphic type.
 */
class BduiActionDeserializer : JsonDeserializer<BduiAction> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BduiAction {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString
            ?: throw JsonParseException("BduiAction requires 'type' field")

        val targetClass: Class<out BduiAction> = when (type) {
            BduiRpcAction.TYPE -> BduiRpcAction::class.java
            BduiCallbackAction.TYPE -> BduiCallbackAction::class.java
            BduiTransformAction.TYPE -> BduiTransformAction::class.java
            BduiSequenceAction.TYPE -> BduiSequenceAction::class.java
            else -> throw JsonParseException("Unknown BduiAction type: $type")
        }

        return context.deserialize(json, targetClass)
    }
}

/**
 * Deserializer for BduiTransform polymorphic type.
 */
class BduiTransformDeserializer : JsonDeserializer<BduiTransform> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BduiTransform {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString
            ?: throw JsonParseException("BduiTransform requires 'type' field")

        val targetClass: Class<out BduiTransform> = when (type) {
            BduiPropertyTransform.TYPE -> BduiPropertyTransform::class.java
            BduiBatchTransform.TYPE -> BduiBatchTransform::class.java
            else -> throw JsonParseException("Unknown BduiTransform type: $type")
        }

        return context.deserialize(json, targetClass)
    }
}

