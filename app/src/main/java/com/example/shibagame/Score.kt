package com.example.shibagame

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Score(
    @PrimaryKey open var id : String = UUID.randomUUID().toString(),
    open var num : Int = 0,
        open var score : Int = 0

): RealmObject()