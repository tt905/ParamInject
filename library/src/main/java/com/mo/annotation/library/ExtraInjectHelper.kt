package com.mo.annotation.library

import android.app.Activity
import java.lang.reflect.Constructor

class ExtraInjectHelper {
    companion object {

        @JvmStatic
        fun inject(activity: Activity) {
            val targetClass = activity::class.java
            val constructor = findBindingConstructorForClass(targetClass)
            constructor?.newInstance(activity)
        }

        private fun findBindingConstructorForClass(cls: Class<*>?): Constructor<*>? {
            if (cls == null) return null
            var bindingConstructor: Constructor<*>? = null
            val clsName = cls.name
            try {
                val bindingClass = cls.classLoader!!.loadClass(clsName + "_Inject")
                bindingConstructor = bindingClass.getConstructor(cls)
            } catch (e: ClassNotFoundException) {
                bindingConstructor = findBindingConstructorForClass(cls.superclass)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Unable to find binding constructor for $clsName", e)
            }
            return bindingConstructor
        }
    }
}