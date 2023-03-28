package com.example.realmtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.realmtest.databinding.ActivityBinding
import com.example.realmtest.databinding.ItemUserBinding
import com.example.realmtest.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityBinding

    private val config = RealmConfiguration.Builder(setOf(User::class))
        .deleteRealmIfMigrationNeeded()
        .build()
    private val realm: Realm = Realm.open(config)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val usersAdapter = UsersAdapter(::editUser, ::deleteUser)
        binding.rvUsers.adapter = usersAdapter

        lifecycleScope.launch {
            realm.query<User>().asFlow().collectLatest {
                usersAdapter.users = realm.copyFromRealm(it.list.toList())
            }
        }

        binding.btnAdd.setOnClickListener {
            realm.writeBlocking {
                val user = User().apply {
                    userName = "David"
                    password = "1234"
                }
                copyToRealm(user)
            }
        }
    }

    private fun logDiagnostics(user: User) {
        println("Method name: ${Thread.currentThread().stackTrace.getOrNull(3)?.methodName}")
        println("User ID: ${user.id.toHexString()}")
        println("Username: ${user.userName}")
        println("Password: ${user.password}")
        try {
            println("Managed: ${user.isManaged()}")
        } catch (e: Exception) {
        }
        try {
            println("Valid: ${user.isValid()}")
        } catch (e: Exception) {
        }
        try {
            println("Frozen: ${user.isFrozen()}")
        } catch (e: Exception) {
        }
    }

    private fun deleteUser(user: User) {
        logDiagnostics(user)
        realm.writeBlocking {
            findLatestManaged(user, User::id)?.let {
                logDiagnostics(it)
                delete(it)
            }
        }
    }

    private fun editUser(user: User) {
//        realm.query<User>("${User::id.name} == $0", user.id).first().find()?.let {
//            println("Before update")
//            logDiagnostics(it)
//            println("========")
//        }
//
        val person = realm.query<User>("${User::id.name} == $0", user.id).first().find()
        val personCopy = person?.copyFromRealm()
        personCopy?.let {
            logDiagnostics(personCopy)
            it.userName = "New User"
            realm.writeBlocking {
                val newPerson = upsert(it)
                logDiagnostics(newPerson)
            }
        }

//        realm.writeBlocking {
//            val newPerson = upsert(User().apply {
//                userName = "Daniel"
//                password = "ADs"
//            })
//            logDiagnostics(newPerson)
//        }

//        realm.writeBlocking {
//            val new = upsert(User().apply {
//                id = user.id
//                userName = "Daniel"
//                password = "heslo123"
//            })
//            println("New value")
//            logDiagnostics(new)
//            println("====")
//
//            val new2 = upsert(User().apply {
//                userName = "Daniel"
//                password = "heslo123"
//            })
//            println("New value2")
//            logDiagnostics(new2)
//            println("====2")
//        }
//
//        realm.query<User>("${User::id.name} == $0", user.id).first().find()?.let {
//            println("After update")
//            logDiagnostics(it)
//        }
    }

    inner class UsersAdapter(val onEditClick: (user: User) -> Unit, val onDeleteClick: (user: User) -> Unit) :
        RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {
        var users: List<User> = emptyList()
            set(value) {
                notifyDataSetChanged()
                field = value
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserViewHolder(binding)
        }

        override fun getItemCount() = users.size

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(users[position])
        }

        inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(user: User) {
                binding.id.text = user.id.timestamp.toString().takeLast(4)
                binding.username.text = user.userName
                binding.password.text = user.password

                binding.btnEdit.setOnClickListener { onEditClick(user) }
                binding.btnDel.setOnClickListener { onDeleteClick(user) }
            }
        }
    }
}