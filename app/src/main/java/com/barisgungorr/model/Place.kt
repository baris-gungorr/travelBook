package com.barisgungorr.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Place(
    @ColumnInfo(name = "name")
    var name: String,  // colum isimleri

    @ColumnInfo(name = "latitude")
    var latitude :Double,

    @ColumnInfo(name = "longitude")
    var longitude : Double

            ) {
    @PrimaryKey(autoGenerate = true) // kendi kendine id'leri oluştur
    var id = 0


}