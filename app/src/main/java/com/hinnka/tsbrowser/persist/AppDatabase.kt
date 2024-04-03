package com.hinnka.tsbrowser.persist

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import zlc.season.rxdownload4.recorder.StatusConverter
import zlc.season.rxdownload4.recorder.TaskEntity
import java.io.File

@Database(entities = [TabInfo::class, SearchHistory::class, History::class, Favorite::class, TaskEntity::class], version = 1, exportSchema = false)
@TypeConverters(StatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        val databaseName = "app-db-${App.processName}"
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(App.instance, AppDatabase::class.java, AppDatabase.databaseName)
                .allowMainThreadQueries()
                .build()
        }
    }

    fun export(){
        val dir: File = File(App.instance.getExternalFilesDir(null), "database")
        if (!dir.exists()){ dir.mkdirs()}
        App.instance.getDatabasePath(AppDatabase.databaseName).let { file ->
            if (file.exists()) {
                XLog.d("文件存在")
                file.copyTo(File(dir, file.name))
            }else{
                XLog.d("文件不存在")
            }
        }
        App.instance.getDatabasePath(AppDatabase.databaseName + "-shm").let { file ->
            if (file.exists()) file.copyTo(File(dir, file.name), true)
        }
        App.instance.getDatabasePath(AppDatabase.databaseName + "-wal").let { file ->
            if (file.exists()) file.copyTo(File(dir, file.name), true)
        }
    }

    fun import(){
        val dir: File = File(App.instance.getExternalFilesDir(null), "database")
        if (!dir.exists()){ return }
        App.instance.getDatabasePath(AppDatabase.databaseName).let { file ->
            File(dir, file.name).let { backupFile ->
                if (backupFile.exists()) backupFile.copyTo(file, true)
            }
        }
        App.instance.getDatabasePath(AppDatabase.databaseName + "-shm").let { file ->
            File(dir, file.name).let { backupFile ->
                if (backupFile.exists()) backupFile.copyTo(file, true)
            }
        }
        App.instance.getDatabasePath(AppDatabase.databaseName + "-wal").let { file ->
            File(dir, file.name).let { backupFile ->
                if (backupFile.exists()) backupFile.copyTo(file, true)
            }
        }
    }
}