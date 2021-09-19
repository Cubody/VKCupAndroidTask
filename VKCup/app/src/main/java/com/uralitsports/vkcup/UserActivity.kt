package com.uralitsports.vkcup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.uralitsports.vkcup.models.VKGroup
import com.uralitsports.vkcup.models.VKUser
import com.uralitsports.vkcup.requests.VKUsersCommand
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseUserGroupFields
import com.vk.sdk.api.friends.FriendsService
import com.vk.sdk.api.friends.dto.FriendsGetFieldsResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsFields
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import com.vk.sdk.api.groups.dto.GroupsGetRequestsFieldsResponse
import com.vk.sdk.api.groups.dto.GroupsGetResponse
import com.vk.sdk.api.users.dto.UsersFields

class UserActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            VK.logout()
            LoginActivity.startFrom(this)
            finish()
        }

        requestUsers()

        requestFriends()
        window.decorView.setOnTouchListener(object :SwipeListener(this){
            override fun onSwipeRight() {
                super.onSwipeRight()
                requestFriends()
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                requestGroups()
            }
        })
    }

    private fun requestUsers() {
        VK.execute(VKUsersCommand(), object: VKApiCallback<List<VKUser>> {
            override fun success(result: List<VKUser>) {
                if (!isFinishing && !result.isEmpty()) {
                    val nameTV = findViewById<TextView>(R.id.nameTV)
                    val user = result[0]
                    nameTV.text = "${user.firstName} ${user.lastName}"
                    nameTV.setOnClickListener(createOnClickListener(user.id))

                    val avatarIV = findViewById<ImageView>(R.id.avatarIV)
                    if (!TextUtils.isEmpty(user.photo)) {
                        Picasso.get()
                                .load(user.photo)
                                .error(R.drawable.user_placeholder)
                                .into(avatarIV)
                    } else {
                        avatarIV.setImageResource(R.drawable.user_placeholder)
                    }
                    avatarIV.setOnClickListener(createOnClickListener(user.id))
                }
            }
            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
            }
        })
    }

    private fun requestGroups() {
        val fields = listOf(GroupsFields.CROP_PHOTO)
        VK.execute(
            GroupsService().groupsGetExtended(fields = fields),
            object : VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    val groups = result.items
                    if (!isFinishing && groups.isNotEmpty()) {
                        val vkGroups = groups.map { group ->
                            VKGroup(
                                id = group.id?.value ?: 0,
                                name = group.name ?: "",
                                photo = group.photo200 ?: "",
                                deactivated = group.deactivated != null
                            )
                        }
                        showGroups(vkGroups)
                    }
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, error.toString())
                }
            })
    }

    private fun requestFriends() {
        val fields = listOf(UsersFields.PHOTO_200)
        VK.execute(FriendsService().friendsGet(fields = fields), object: VKApiCallback<FriendsGetFieldsResponse> {
            override fun success(result: FriendsGetFieldsResponse) {
                val friends = result.items
                if (!isFinishing && friends.isNotEmpty()) {
                    val vkUsers = friends.map { friend ->
                        VKUser(
                            id = friend.id?.value ?: 0,
                            firstName = friend.firstName ?: "",
                            lastName = friend.lastName ?: "",
                            photo = friend.photo200 ?: "",
                            deactivated = friend.deactivated != null
                        )
                    }
                    showFriends(vkUsers)
                }
            }
            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
            }
        })
    }

    private fun showFriends(friends: List<VKUser>) {
        val recyclerView = findViewById<RecyclerView>(R.id.friendsRV)
        val friendsText = findViewById<TextView>(R.id.friends_count)
        friendsText.text = "У вас ${friends.count()} друзей:"
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)

        val adapter = FriendsAdapter()
        adapter.setData(friends)

        recyclerView.adapter = adapter
    }

    private fun showGroups(groups: List<VKGroup>) {
        val recyclerView = findViewById<RecyclerView>(R.id.friendsRV)
        val friendsText = findViewById<TextView>(R.id.friends_count)
        friendsText.text = "У вас ${groups.count()} групп:"
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)

        val adapter = GroupsAdapter()
        adapter.setData(groups)

        recyclerView.adapter = adapter
    }

    private fun createOnClickListener(userId: Long) = View.OnClickListener {
        VK.urlResolver.open(it.context, "https://vk.com/id$userId")
    }

    inner class FriendsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val friends: MutableList<VKUser> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                = UserHolder(parent.context)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as UserHolder).bind(friends[position])
        }

        fun setData(friends: List<VKUser>) {
            this.friends.clear()
            this.friends.addAll(friends)
            notifyDataSetChanged()
        }

        override fun getItemCount() = friends.size
    }

    inner class GroupsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val groups: MutableList<VKGroup> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                = GroupHolder(parent.context)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as GroupHolder).bind(groups[position])
        }

        fun setData(groups: List<VKGroup>) {
            this.groups.clear()
            this.groups.addAll(groups)
            notifyDataSetChanged()
        }

        override fun getItemCount() = groups.size
    }

    inner class GroupHolder(context: Context?): RecyclerView.ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_user, null)) {
        private val avatarIV: ImageView = itemView.findViewById(R.id.avatarIV)
        private val nameTV: TextView = itemView.findViewById(R.id.nameTV)

        fun bind(group: VKGroup) {
            nameTV.text = "${group.name}"
            nameTV.setOnClickListener(createOnClickListener(group.id))
            if (!TextUtils.isEmpty(group.photo)) {
                Picasso.get().load(group.photo).error(R.drawable.user_placeholder).into(avatarIV)
            } else {
                avatarIV.setImageResource(R.drawable.user_placeholder)
            }
            avatarIV.setOnClickListener(createOnClickListener(group.id))
        }
    }

    inner class UserHolder(context: Context?): RecyclerView.ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_user, null)) {
        private val avatarIV: ImageView = itemView.findViewById(R.id.avatarIV)
        private val nameTV: TextView = itemView.findViewById(R.id.nameTV)

        fun bind(user: VKUser) {
            nameTV.text = "${user.firstName} ${user.lastName}"
            nameTV.setOnClickListener(createOnClickListener(user.id))
            if (!TextUtils.isEmpty(user.photo)) {
                Picasso.get().load(user.photo).error(R.drawable.user_placeholder).into(avatarIV)
            } else {
                avatarIV.setImageResource(R.drawable.user_placeholder)
            }
            avatarIV.setOnClickListener(createOnClickListener(user.id))
        }
    }

    companion object {
        private const val TAG = "UserActivity"

        fun startFrom(context: Context) {
            val intent = Intent(context, UserActivity::class.java)
            context.startActivity(intent)
        }
    }
}