package com.hinnka.tsbrowser.ui.composable.download

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.download.DownloadNotificationCreator
import com.hinnka.tsbrowser.download.TSRecorder
import com.hinnka.tsbrowser.ext.longPress
import com.hinnka.tsbrowser.ext.mainScope
import com.hinnka.tsbrowser.ext.observeAsState
import com.hinnka.tsbrowser.persist.AppDatabase
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.PopupMenu
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.recorder.RoomRecorder
import zlc.season.rxdownload4.recorder.RxDownloadRecorder
import zlc.season.rxdownload4.recorder.TaskEntity

@SuppressLint("CheckResult", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DownloadPage() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // var tasks = remember { mutableListOf<TaskEntity>() }
    // val tasksState = AppDatabase.instance.downloadDao().getAllLiveData().observeAsState(listOf())
    val tasks by AppDatabase.instance.downloadDao().getAllFlow().observeAsState(listOf())
    // val tasks by remember { tasksState }

    XLog.d( "进度---222222----：" )
    LaunchedEffect(key1 = tasks) {
        tasks.sortedBy { item ->
            XLog.d( "进度-------------：" )
            when (item.status) {
                is Completed -> {
                    System.currentTimeMillis() - item.task.file().lastModified()
                }
                is Failed -> -1L
                is Paused -> -2L
                is Pending -> -3L
                else -> -4L
            }
        }
    }

    // val tasks = AppDatabase.instance.downloadDao().getAllFlow().observeAsState(arrayListOf()).value.sortedBy { item ->
    //     when (item.status) {
    //         is Completed -> {
    //             System.currentTimeMillis() - item.task.file().lastModified()
    //         }
    //         is Failed -> -1L
    //         is Paused -> -2L
    //         is Pending -> -3L
    //         else -> -4L
    //     }
    // }

    LaunchedEffect(Unit) {
        DownloadHandler.showDownloadingBadge.value = false
    }

    Scaffold(topBar = {
        TSAppBar(title = stringResource(id = R.string.downloads), actions = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .imePadding()
                .navigationBarsPadding()
                .clickable {
                    scope.launch {
                        val it = AppDatabase.instance
                            .downloadDao()
                            .getAllWithStatus(Completed(), Failed())
                        if (it.isNullOrEmpty()) return@launch
                        AlertBottomSheet
                            .Builder(context)
                            .apply {
                                setTitle(R.string.clear)
                                setMessage(
                                    context.getString(
                                        R.string.clear_confirm,
                                        it.size
                                    )
                                )
                                setPositiveButton(R.string.delete) {
                                    it.forEach { entity ->
                                        val manager = entity.task.manager(
                                            recorder = RoomRecorder(),
                                            notificationCreator = DownloadNotificationCreator()
                                        )
                                        manager.delete()
                                        // tasks.removeAll { item -> item.id == entity.id }
                                    }
                                }
                                setNegativeButton(android.R.string.cancel) {
                                }
                            }
                            .show()
                    }
                }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.clear),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                )
            }
        })
    }) {
        // LaunchedEffect(key1 = tasks) {
        //     tasks.sortedBy { item ->
        //         XLog.d( "进度-------------：" )
        //         when (item.status) {
        //             is Completed -> {
        //                 System.currentTimeMillis() - item.task.file().lastModified()
        //             }
        //             is Failed -> -1L
        //             is Paused -> -2L
        //             is Pending -> -3L
        //             else -> -4L
        //         }
        //     }
        // }

        // LaunchedEffect(key1 = tasks) {
        //     tasks = AppDatabase.instance.downloadDao().getAll()
        //     scope.launch {
        //         val list = AppDatabase.instance.downloadDao().getAll()
        //         val iterator = list.iterator()
        //         while (iterator.hasNext()){
        //             val li = iterator.next()
        //             if (li.status is Completed && !li.task.file().exists()){
        //                 li.task.manager(recorder = TSRecorder()).delete()
        //                 iterator.remove()
        //             }
        //         }
        //
        //         tasks.clear()
        //         XLog.d("添加任务")
        //         tasks.addAll(list.sortedBy { item ->
        //             when (item.status) {
        //                 is Completed -> { System.currentTimeMillis() - item.task.file().lastModified() }
        //                 is Failed -> -1L
        //                 is Paused -> -2L
        //                 is Pending -> -3L
        //                 else -> -4L
        //             }
        //         })
        //     }
        // }

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(id = R.string.downloads_empty),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
        val state = rememberLazyListState()
        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            itemsIndexed(tasks) { index, entity ->
                val manager = entity.task.manager(
                    recorder = TSRecorder(),
                    notificationCreator = DownloadNotificationCreator()
                )

                XLog.d( "进度44444：" + entity.progress.percentStr())

                // XLog.d( "进度00000：" + item.progress.percentStr())
                val showPopup = remember { mutableStateOf(false) }
                val popupOffset = remember { mutableStateOf(IntOffset.Zero) }

                Box(modifier = Modifier
                    .longPress {
                        showPopup.value = true
                        popupOffset.value = IntOffset(it.x.toInt(), it.y.toInt())
                    }
                    .height(100.dp)) {
                    when (entity.status) {
                        is Completed -> if(entity.task.file().exists()) CompletedItem(entity = entity) else manager.delete()
                        is Failed -> ErrorItem(entity = entity)
                        else -> {
                            LaunchedEffect(key1 = entity) {
                                manager.subscribe {
                                    entity.status = it
                                    XLog.d("进度：" + it.progress.percentStr())
                                    // tasks[index] = entity
                                }
                            }
                            DownloadingItem(entity)
                        }
                    }

                    PopupMenu(alignment = Alignment.TopStart,
                        expanded = showPopup.value,
                        offset = popupOffset.value,
                        onDismissRequest = { showPopup.value = false },){
                        DropdownMenuItem(onClick = {
                            clipboardManager.setText(AnnotatedString(entity.task.url))
                            showPopup.value = false
                        }) {
                            Text(
                                text = stringResource(id = R.string.copy_link),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        DropdownMenuItem(onClick = {
                            manager.delete()
                            // tasks.removeAll { item -> item.id == entity.id }
                            showPopup.value = false
                        }) {
                            Text(
                                text = stringResource(id = R.string.delete),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
