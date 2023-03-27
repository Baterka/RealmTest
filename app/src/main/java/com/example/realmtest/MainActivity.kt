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
import com.example.realmtest.utils.Utils
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.isFrozen
import io.realm.kotlin.ext.isManaged
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Exception

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

    private fun deleteUser(user: User) {
        realm.writeBlocking {
            findLatest(user)?.let { delete(it) }
        }
    }

    private fun editUser(user: User) {
        realm.writeBlocking {
//            query<User>("${User::id.name} == $0", user.id).first().find()?.let {
//                it.userName = Utils.randomString()
//            }
//            try {
//                println("Managed: ${user.isManaged()}")
//            } catch (e: Exception) {
//            }
//            try {
//                println("Valid: ${user.isValid()}")
//            } catch (e: Exception) {
//            }
//            try {
//                println("Frozen: ${user.isFrozen()}")
//            } catch (e: Exception) {
//            }
//            findLatest(user)?.let {
//                println("Managed: ${it.isManaged()}")
//                println("Valid: ${it.isValid()}")
//                println("Frozen: ${it.isFrozen()}")
//            }

//            user.userName = Utils.randomString()
//            findLatest(user)?.let{
//                copyToRealm(user)
//            }

        }
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