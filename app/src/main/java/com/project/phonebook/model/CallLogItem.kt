package com.project.phonebook.model

import androidx.annotation.Keep

@Keep
class CallLogItem {
    var number: String? = null
    var name: String? = null
    var type: String? = null
    var duration: Int? = null
    var timing: String? = null
    var date : String? = null
    var timeDay : Long? = null
}