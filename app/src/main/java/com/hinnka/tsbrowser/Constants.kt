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

const val LoreadBridge = "LoreadBridge"
const val GeoDB = "geodb"
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

object JsFilePath{
    const val loaderWeb = "js/loader.web.js"
    const val loaderNetwork = "js/loader.network.js"

    const val loreadConsole = "js/loread.console.js"
    const val loreadSmartExtractor = "js/loread.smartExtractor.js"
    const val loreadTranslator = "js/loread.translator.js"
    const val loreadReadability = "js/loread.readability.js"


    const val readabilityOld = "js/loread.readability.old.js"

    const val loreadCommon = "js/loread.common.js"
    const val normalize = "js/loread.normalize.js"
    const val videoController = "js/loread.video.controller.js"
    const val videoControllerIconFont = "js/loread.video.controller.iconfont.js"
    const val selector = "js/loread.selector.js"
    const val keepElement = "js/loread.keepElement.js"

    const val zepto = "js/zepto.min.js"
    const val vConsole = "js/vconsole.min.js"

    const val plyr = "js/plyr.js"
    const val highlight = "js/highlight.min.js"
    const val mathjax = "js/tex-mml-chtml.js"
}
object CssFilePath{
    const val articleThemeDay = "css/article_theme_day.css"
    const val articleThemeNight = "css/article_theme_night.css"
    const val videoController = "css/loread.video.controller.css"
    const val selector = "css/loread.selector.css"
    const val translateelement = "css/translateelement.css"
    const val normalize = "css/normalize.css";
}