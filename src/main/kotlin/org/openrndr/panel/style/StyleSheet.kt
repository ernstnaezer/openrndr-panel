package org.openrndr.panel.style

import org.openrndr.color.ColorRGBa
import org.openrndr.panel.style.PropertyInheritance.INHERIT
import org.openrndr.panel.style.PropertyInheritance.RESET
import java.util.*
import kotlin.reflect.KProperty

enum class PropertyInheritance {
    INHERIT,
    RESET
}

data class Property(val name: String,
                    val value: Any?)

open class PropertyValue(val inherit: Boolean = false)

sealed class Color(inherit: Boolean = false) : PropertyValue(inherit) {
    class RGBa(val color: ColorRGBa) : Color() {
        override fun toString(): String {
            return "RGBa(color=$color)"
        }
    }

    object Inherit : Color(inherit = true)
}

sealed class LinearDimension(inherit: Boolean = false) : PropertyValue(inherit) {
    class PX(val value: Double) : LinearDimension() {
        override fun toString(): String {
            return "PX(value=$value)"
        }
    }

    class Percent(val value: Double) : LinearDimension()
    object Auto : LinearDimension()
    object Inherit : LinearDimension(inherit = true)
}

data class PropertyBehaviour(val inheritance: PropertyInheritance, val intitial: Any) {


}

object PropertyBehaviours {

    val behaviours = HashMap<String, PropertyBehaviour>()
}

class PropertyHandler<T>(
        val name: String, val inheritance: PropertyInheritance, val initial: T
                        ) {

    init {
        PropertyBehaviours.behaviours.set(name, PropertyBehaviour(inheritance, initial as Any))
    }

    @Suppress("USELESS_CAST", "UNCHECKED_CAST")
    operator fun getValue(stylesheet: StyleSheet, property: KProperty<*>): T {
        val value: T? = stylesheet.getProperty(name)?.value as T?
        if (value != null) {
            return value
        } else {
            return PropertyBehaviours.behaviours[name]!!.intitial as T
        }

    }

    operator fun setValue(stylesheet: StyleSheet, property: KProperty<*>, value: T?) {
        stylesheet.setProperty(name, value)
    }
}

enum class Display {
    INLINE,
    BLOCK,
    FLEX,
    NONE

}

enum class Position {
    STATIC,
    ABSOLUTE,
    RELATIVE,
    FIXED,
    INHERIT
}

sealed class FlexDirection(inherit: Boolean = false) : PropertyValue(inherit) {
    object Row : FlexDirection()
    object Column : FlexDirection()
    object RowReverse : FlexDirection()
    object ColumnReverse : FlexDirection()
    object Inherit : FlexDirection(inherit = true)
}

sealed class Overflow(inherit: Boolean = false) : PropertyValue(inherit) {
    object Visible : Overflow()
    object Hidden : Overflow()
    object Scroll : Overflow()
    object Inherit : Overflow(inherit = true)
}

sealed class ZIndex(inherit: Boolean = false) : PropertyValue(inherit) {
    object Auto : ZIndex()
    class Value(val value: Int) : ZIndex()
    object Inherit : ZIndex(inherit = true)
}

sealed class FlexGrow(inherit: Boolean = false) : PropertyValue(inherit) {
    class Ratio(val value: Double):FlexGrow()
    object Inherit : FlexGrow(inherit = true)
}

class StyleSheet {


    val properties = HashMap<String, Property>()

    var selector: CompoundSelector? = null
        set(value) {
            field = value
            precedence = value?.precedence() ?: SelectorPrecedence()
        }

    var precedence = SelectorPrecedence()
        private set(value) {
            field = value
        }

    fun getProperty(name: String) = properties.get(name)

    fun setProperty(name: String, value: Any?) {
        properties.put(name, Property(name, value))
    }

    fun cascadeOnto(onto: StyleSheet): StyleSheet {
        val cascaded = StyleSheet()
        cascaded.properties.putAll(properties)
        cascaded.properties.putAll(onto.properties)
        return cascaded
    }

    override fun toString(): String {
        return "StyleSheet(properties=$properties)"
    }

//    // computed fields
//    var screenX: Double = 0.0
//    var screenY: Double = 0.0
//    var screenWidth: Double = 0.0
//    var screenHeight: Double = 0.0
}

var StyleSheet.width by PropertyHandler<LinearDimension>("width", RESET, LinearDimension.Auto)
var StyleSheet.height by PropertyHandler<LinearDimension>("height", RESET, LinearDimension.Auto)
var StyleSheet.top by PropertyHandler<LinearDimension>("top", RESET, 0.px) // css default is auto
var StyleSheet.left by PropertyHandler<LinearDimension>("left", RESET, 0.px) // css default is auto

var StyleSheet.marginTop by PropertyHandler<LinearDimension>("margin-top", RESET, 0.px)
var StyleSheet.marginBottom by PropertyHandler<LinearDimension>("margin-bottom", RESET, 0.px)
var StyleSheet.marginLeft by PropertyHandler<LinearDimension>("margin-left", RESET, 0.px)
var StyleSheet.marginRight by PropertyHandler<LinearDimension>("margin-right", RESET, 0.px)

var StyleSheet.position by PropertyHandler<Position>("position", RESET, Position.STATIC)
var StyleSheet.display by PropertyHandler<Display>("display", RESET, Display.BLOCK) // css default is inline

var StyleSheet.flexDirection by PropertyHandler<FlexDirection>("flex-direction", RESET, FlexDirection.Row)
var StyleSheet.flexGrow by PropertyHandler<FlexGrow>("flex-grow", RESET, FlexGrow.Ratio(0.0))


var StyleSheet.background by PropertyHandler<Color>("background-color", RESET, Color.RGBa(ColorRGBa.BLACK.opacify(0.0)))
var StyleSheet.color by PropertyHandler<Color>("color", INHERIT, Color.RGBa(ColorRGBa.WHITE))

var StyleSheet.fontSize by PropertyHandler<LinearDimension>("font-size", INHERIT, 12.px)
var StyleSheet.fontFamily by PropertyHandler<String>("font-family", INHERIT, "default")

var StyleSheet.overflow by PropertyHandler<Overflow>("overflow", RESET, Overflow.Visible)

var StyleSheet.zIndex by PropertyHandler<ZIndex>("z-index", RESET, ZIndex.Auto)

val Number.px: LinearDimension.PX get() = LinearDimension.PX(this.toDouble())
val Number.percent: LinearDimension.Percent get() = LinearDimension.Percent(this.toDouble())

fun styleSheet(init: StyleSheet.() -> Unit): StyleSheet {
    return StyleSheet().apply { init() }
}