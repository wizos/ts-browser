/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <contact-sagernet@sekai.icu>             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package com.hinnka.tsbrowser



const val FILE = "file:///"
const val PACKAGE = "package:"

val SupportedSchemaWhenLongPress = setOf(Schema.HTTP, Schema.HTTPS, Schema.VIEW_SOURCE)

object Schema {
    const val HTTP = "http://"
    const val HTTPS = "https://"
    const val VIEW_SOURCE = "view-source:"
    const val DATA = "data:"
}

object PackageName {
    const val PLAY = "com.android.vending"
}

object URL {
    const val GOOGLE = "https://www.google.com"
    const val BAIDU = "https://www.baidu.com"
    const val APP_PLAY = "https://play.google.com/store/apps/details?id=com.hinnka.tsbrowser"
}


object MediaType {
    const val TEXT_PLAIN = "text/plain"
    const val IMAGE_PNG = "image/png"
}

object GroupOrder {
    const val ORIGIN = 0
    const val BY_NAME = 1
    const val BY_DELAY = 2
}
