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

package com.sebastienbalard.bicycle

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class SBLog {

    val LOGGER: Logger

    init {
        val cls = this::class
        if (!cls.isCompanion)  {
            throw  UnsupportedOperationException("SBLog object must be a companion object")
        }
        val accompanied = Class.forName(cls.java.name.substringBeforeLast('$'))
        LOGGER = LoggerFactory.getLogger(accompanied)
    }

    inline fun v(msg: () -> String) {
        if (LOGGER.isTraceEnabled) LOGGER.trace(msg())
    }

    inline fun d(msg: () -> String) {
        if (LOGGER.isDebugEnabled) LOGGER.debug(msg())
    }

    inline fun i(msg: () -> String) {
        if (LOGGER.isInfoEnabled) LOGGER.info(msg())
    }

    inline fun w(msg: () -> String) {
        if (LOGGER.isWarnEnabled) LOGGER.warn(msg())
    }

    inline fun e(msg: () -> String) {
        if (LOGGER.isErrorEnabled) LOGGER.error(msg())
    }

}