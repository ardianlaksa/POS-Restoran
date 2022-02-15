/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dnhsolution.restokabmalang.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dnhsolution.restokabmalang.utilities.DataTypeConverterRoom
import com.dnhsolution.restokabmalang.utilities.TblProdukKategoriDao

@Database(entities = [TblProdukKategori::class], version = 1)
@TypeConverters(DataTypeConverterRoom::class)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun tblProdukKategoriDao(): TblProdukKategoriDao

    companion object {
        var INSTANCE: AppRoomDatabase? = null

        fun getAppDataBase(context: Context): AppRoomDatabase? {
            if (INSTANCE == null){
                synchronized(AppRoomDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppRoomDatabase::class.java, "pos_android").build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}
