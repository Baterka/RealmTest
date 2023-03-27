package com.example.realmtest.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class User: RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var userName: String? = null
    var password: String? = null
}