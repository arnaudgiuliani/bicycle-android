/**
 * Copyright © 2017 Bicycle (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastienbalard.bicycle.misc

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

abstract class SBLog {

    private val logger: Logger

    init {
        val clazz = this::class
        if (!clazz.isCompanion)  {
            throw  UnsupportedOperationException("SBLog object must be a companion object")
        }
        val accompanied = Class.forName(clazz.java.name.substringBeforeLast('$'))
        logger = LoggerFactory.getLogger(accompanied)
    }

    fun v(msg: String) {
        if (logger.isTraceEnabled) logger.trace(msg)
    }

    fun d(msg: String) {
        if (logger.isDebugEnabled) logger.debug(msg)
    }

    fun i(msg: String) {
        if (logger.isInfoEnabled) logger.info(msg)
    }

    fun w(msg: String) {
        if (logger.isWarnEnabled) logger.warn(msg)
    }

    fun w(msg: String, exception: Exception?) {
        if (logger.isWarnEnabled) logger.warn(msg, exception)
    }

    fun e(msg: String) {
        if (logger.isErrorEnabled) logger.error(msg)
    }

    fun e(msg: String, exception: Exception?) {
        if (logger.isErrorEnabled) logger.error(msg, exception)
    }

    /*inline fun e(msg: () -> String) {
        if (logger.isErrorEnabled) logger.error(msg())
    }*/

}