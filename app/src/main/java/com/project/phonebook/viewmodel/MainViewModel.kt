package com.project.phonebook.viewmodel

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.project.phonebook.model.CallLogItem
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val callListLiveData = MutableLiveData<MutableList<CallLogItem>>().apply { postValue(null) }

    val callListPairedData = MutableLiveData<List<Pair<Long?,List<CallLogItem>>>>().apply { postValue(null) }
    fun getCallLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            val callLogsList = mutableListOf<CallLogItem>()
            val resolver = getApplication(context).contentResolver
            val cursor = resolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            )
            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val number = it.getString(numberIndex) ?: "Unknown"
                    val name = getContactName(number)
                    val type = when (it.getInt(typeIndex)) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        CallLog.Calls.REJECTED_TYPE -> "Rejected"
                        else -> "Unknown"
                    }
                    val timeInMillis = it.getLong(dateIndex)
                    val smp = SimpleDateFormat(
                        "dd MMM hh:mm:ss a", Locale.getDefault()
                    )
                    val timing = smp.format(timeInMillis).orEmpty()
                    val smp1 = SimpleDateFormat(
                        "dd MMM", Locale.getDefault()
                    )
                    val date = smp1.format(timeInMillis).orEmpty()

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = timeInMillis
                    calendar.setDayStart()

                    val dur = it.getInt(durationIndex)

                    callLogsList.add(CallLogItem().apply {
                        this.name = name
                        this.timing = timing
                        this.number = number
                        this.type = type
                        this.duration = dur
                        this.date = date
                        this.timeDay = calendar.timeInMillis
                    })
                }
            }
            callListLiveData.postValue(callLogsList)
            toGroupByDate(callLogsList)
        }
    }

    private fun toGroupByDate(list: MutableList<CallLogItem>) {
        val data = list.groupBy {
            it.timeDay
        }.toList().sortedBy { it.first }.reversed()
        println("@@@ data -> ${Gson().toJson(data)}")
        callListPairedData.postValue(data)
    }

    private fun Calendar.setDayStart() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun getContactName(phoneNumber: String): String? {
        val resolver = getApplication(context).contentResolver
        val uri =
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(phoneNumber)
                .build()
        val cursor = resolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null
    }

}