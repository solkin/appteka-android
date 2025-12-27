package com.tomclaw.appsend.util.bdui

import com.tomclaw.appsend.util.bdui.model.BduiRef

/**
 * Resolves BduiRef objects to actual values from components.
 * Recursively traverses nested structures (maps and lists) to resolve all refs.
 */
class BduiRefResolver(
    private val valueProvider: BduiValueProvider
) {

    /**
     * Recursively resolves all refs in the given value.
     * - If value is a BduiRef, resolves it to actual value
     * - If value is a Map with type="ref", treats it as a ref and resolves
     * - If value is a Map or List, recursively resolves nested values
     * - Otherwise returns value as-is
     */
    fun resolve(value: Any?): Any? {
        return when (value) {
            null -> null
            is BduiRef -> resolveRef(value)
            is Map<*, *> -> resolveMap(value)
            is List<*> -> resolveList(value)
            else -> value
        }
    }

    private fun resolveRef(ref: BduiRef): Any? {
        return valueProvider.getPropertyValue(ref.id, ref.property)
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveMap(map: Map<*, *>): Any? {
        // Check if this map represents a ref object
        val type = map["type"]
        if (type == "ref") {
            val id = map["id"] as? String
            val property = map["property"] as? String
            if (id != null && property != null) {
                return resolveRef(BduiRef(id = id, property = property))
            }
        }

        // Recursively resolve all values in the map
        return map.mapValues { (_, v) -> resolve(v) }
    }

    private fun resolveList(list: List<*>): List<*> {
        return list.map { resolve(it) }
    }
}

/**
 * Interface for providing property values from BDUI components.
 * Implemented by BduiView to provide access to component states.
 */
interface BduiValueProvider {

    /**
     * Gets the current value of a property from a component.
     *
     * @param id Component ID
     * @param property Property name (e.g., "text", "checked", "value", "visibility")
     * @return Property value or null if not found
     */
    fun getPropertyValue(id: String, property: String): Any?
}

