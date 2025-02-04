package com.project.phonebook.utils

import android.app.Application
import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.CallLog.Calls.LIMIT_PARAM_KEY
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import androidx.paging.*
import com.project.phonebook.model.CallLogItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogPager(private val application: Application) : PagingSource<Long, CallLogItem>() {

    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, CallLogItem> {
        return try {
            val pageSize = params.loadSize
            val resolver = application.contentResolver
            val twoDaysAgo = Calendar.getInstance()
            twoDaysAgo.set2Day()
            twoDaysAgo.add(Calendar.DATE, -1)
            val dateKey = params.key ?: twoDaysAgo.timeInMillis  // Start from the latest call
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${CallLog.Calls.DATE} < ?")
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(dateKey.toString())
                )
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "${CallLog.Calls.DATE} DESC")
                putInt(ContentResolver.QUERY_ARG_SQL_LIMIT, pageSize)
            }

            val cursor = resolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(LIMIT_PARAM_KEY, pageSize.toString()).build(),
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                queryArgs,
                null
            )
            var nextKey: Long? = null

            val callLogsList = mutableListOf<CallLogItem>()
            var i = 0
            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    i++
                    val number = it.getString(numberIndex) ?: "Unknown"
                    val name = getContactName(application, number)
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
                    nextKey = timeInMillis

                    callLogsList.add(CallLogItem().apply {
                        this.name = name
                        this.timing = timing
                        this.number = number
                        this.type = type
                        this.duration = dur
                        this.timeDay = calendar.timeInMillis
                    })
                }
            }

            LoadResult.Page(
                data = callLogsList,
                prevKey = null,
                nextKey = if (callLogsList.isEmpty()) null else nextKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }

    }

    private fun getContactName(application: Application, phoneNumber: String): String? {
        val resolver = application.contentResolver
        val uri =
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(phoneNumber)
                .build()
        val cursor = resolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null
    }

    override fun getRefreshKey(state: PagingState<Long, CallLogItem>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.timeDay
        }
    }

}