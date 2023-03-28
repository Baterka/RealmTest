package com.example.realmtest

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

fun <T : RealmObject> MutableRealm.upsert(obj: T): T = copyToRealm(obj, UpdatePolicy.ALL)

inline fun <reified T : RealmObject> MutableRealm.findManaged(unmanaged: T, prop: KProperty<Any>): T? {
    return query<T>("${prop.name} == $0", unmanaged.readInstanceProperty(prop)).first().find()
}

inline fun <reified T : RealmObject> MutableRealm.findLatestManaged(unmanaged: T, prop: KProperty<Any>): T? {
    return findManaged(unmanaged, prop)?.let {
        findLatest(it)
    }
}

@Suppress("UNCHECKED_CAST")
fun <C : Any, R : Any> C.readInstanceProperty(prop: KProperty<Any>): R {
    val property = this::class.members.first { it == prop } as KProperty1<C, R>
    return property.get(this)
}

fun <C : Any> C.writeInstanceProperty(prop: KProperty<Any>, value: Any) {
    val property = this::class.members.first { it.name == prop.name } as KMutableProperty<*>
    property.setter.call(this, value)
}