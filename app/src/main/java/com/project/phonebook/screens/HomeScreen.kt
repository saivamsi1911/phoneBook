package com.project.phonebook.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.phonebook.R
import com.project.phonebook.model.CallLogItem
import com.project.phonebook.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(viewModel: MainViewModel, callNumber : (String) -> Unit) {
    val scrollState = rememberScrollState()

    val list = viewModel.callListLiveData.observeAsState()
    val pairedList = viewModel.callListPairedData.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getCallLogs()
    }

    @Composable
    fun getDay(time : Long?) : String {
        val smp1 = SimpleDateFormat(
            "dd MMM yyyy", Locale.getDefault()
        )
        return smp1.format(time).orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {

        Text(
            "Home",
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFffe6e6))
                .padding(top = 20.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            color = Color(0xFF000000)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
//            list.value?.forEach { item ->
//                HomeItem(item)
//            }

            pairedList.value?.forEach { pair ->
                Text(getDay(pair.first) ?: "Unknown", fontSize = 18.sp, modifier = Modifier.padding(start = 10.dp, top = 14.dp))

                pair.second.forEach {  item ->
                    HomeItem(item, callNumber)

                }

            }
        }
    }
}

@Composable
fun HomeItem(model: CallLogItem,callNumber: (String) -> Unit) {

    @Composable
    fun getTimeText(dur : Int) : String {
        val min = dur/60
        val sec= dur %60
        return "$min min $sec sec"
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (model.type) {
                "Incoming" -> Image(
                    painter = painterResource(R.drawable.in_call),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color(0xFF006A4E))
                )

                "Outgoing" -> Image(
                    painter = painterResource(R.drawable.out_call),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color(0xFF006A4E))
                )

                "Missed" -> Image(
                    painter = painterResource(R.drawable.miss_call),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color(0xFFE8A317))
                )

                "Rejected" -> Image(
                    painter = painterResource(R.drawable.reject_call),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color(0xFF990000))
                )

            }

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Text(model.name ?: model.number ?: "")
                    if((model.duration ?: 0) > 0)Text(getTimeText(model.duration ?: 0), modifier = Modifier.clip(
                        RoundedCornerShape(20.dp)
                    ).background(Color(0xFF64E986)).padding(horizontal = 5.dp))

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(model.timing ?: "")
                }

            }
        }

        Image(
            painter = painterResource(R.drawable.call),
            modifier = Modifier.size(24.dp).clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = Color(0xffACACAC)
                ),
                onClick = {
                    callNumber.invoke(model.number ?: "")
                }
            ),
            contentDescription = ""
        )

    }
}

@Preview
@Composable
fun HomeItemPreview() {
    val model = CallLogItem().apply {
        name = "name"
        number = "number"
        timing = "timing"
        duration = 5
        type = "Incoming"
    }
    HomeItem(model){}
}




