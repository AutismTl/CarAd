package com.archeango.net

import com.archeango.model.LoginInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * @author tangliang.autisl@bytedance.com
 * @date 2019/4/5.
 */
interface LoginServer {
    @FormUrlEncoded
    @POST("/user/loginInfo")
    fun postLoginInfo(
        @Field("sign") sign: String, @Field("phoneNumber") phoneNumber: Int,
        @Field("nickName") nickName: String
    ): Observable<HttpResult<LoginInfo>>
}
