package ru.merkulyevsasha.netrepository.network.users

import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.merkulyevsasha.core.models.UserInfo
import ru.merkulyevsasha.core.preferences.KeyValueStorage
import ru.merkulyevsasha.core.repositories.UsersApiRepository
import ru.merkulyevsasha.netrepository.network.base.BaseApiRepository
import ru.merkulyevsasha.netrepository.network.mappers.UserInfoMapper
import ru.merkulyevsasha.network.data.UsersApi
import java.io.File

class UsersApiRepositoryImpl(
    sharedPreferences: KeyValueStorage,
    baseUrl: String,
    debugMode: Boolean
) : BaseApiRepository(sharedPreferences, baseUrl, debugMode), UsersApiRepository {

    private val userInfoMapper: UserInfoMapper by lazy { UserInfoMapper("bearer " + sharedPreferences.getAccessToken(), baseUrl) }

    private val api: UsersApi = retrofit.create(UsersApi::class.java)

    override fun getUserInfo(): Single<UserInfo> {
        return api.getUserInfo()
            .map { userInfoMapper.map(it) }
    }

    override fun updateUser(name: String, phone: String): Single<UserInfo> {
        return api.updateUser(name, phone)
            .map { userInfoMapper.map(it) }
    }

    override fun uploadUserPhoto(profileFileName: String): Single<UserInfo> {
        return Single.fromCallable {
            val fileImage = File(profileFileName)
            val requestBody = fileImage.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("File", fileImage.name, requestBody)
        }
            .flatMap {
                api.uploadUserPhoto(it)
            }
            .map { userInfoMapper.map(it) }
    }
}