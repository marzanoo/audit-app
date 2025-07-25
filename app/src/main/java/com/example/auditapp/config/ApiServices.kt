package com.example.auditapp.config


import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.AuditAnswerItem
import com.example.auditapp.model.AuditAnswerResponse
import com.example.auditapp.model.AuditAnswerResponseUpdate
import com.example.auditapp.model.AuditExcelResponse
import com.example.auditapp.model.AuditOfficeResponse
import com.example.auditapp.model.DetailAuditAnswerResponse
import com.example.auditapp.model.DetailAuditAnswerResponseUpdate
import com.example.auditapp.model.DetailFotoResponse
import com.example.auditapp.model.DetailFotoResponseUpdate
import com.example.auditapp.model.ExcelDownloadResponse
import com.example.auditapp.model.Form
import com.example.auditapp.model.FormResponse
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.Lantai
import com.example.auditapp.model.LantaiResponse
import com.example.auditapp.model.LoginRequest
import com.example.auditapp.model.LoginResponse
import com.example.auditapp.model.LogoutResponse
import com.example.auditapp.model.OtpRequest
import com.example.auditapp.model.OtpResponse
import com.example.auditapp.model.PicAreaResponse
import com.example.auditapp.model.RegisterRequest
import com.example.auditapp.model.RegisterResponse
import com.example.auditapp.model.ResetPasswordRequest
import com.example.auditapp.model.ResetPasswordResponse
import com.example.auditapp.model.SingleAreaResponse
import com.example.auditapp.model.StandarFotoResponse
import com.example.auditapp.model.TemaForm
import com.example.auditapp.model.TemaFormResponse
import com.example.auditapp.model.UpdateAreaResponse
import com.example.auditapp.model.UpdateFormResponse
import com.example.auditapp.model.UpdateTemaFormResponse
import com.example.auditapp.model.UpdateVariabelFormResponse
import com.example.auditapp.model.UserResponseGetById
import com.example.auditapp.model.VariabelForm
import com.example.auditapp.model.VariabelFormResponse
import com.example.auditapp.model.VerifikasiOtpRequest
import com.example.auditapp.model.VerifikasiOtpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiServices {
    //Auth
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): retrofit2.Call<LoginResponse>

    @POST("password/forgot")
    fun sendResetLink(@Body request: OtpRequest): Call<OtpResponse>

    @POST("verify-reset-otp")
    fun verifyResetOtp(@Body request: VerifikasiOtpRequest): Call<VerifikasiOtpResponse>

    @POST("password/reset")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): retrofit2.Call<LogoutResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): retrofit2.Call<RegisterResponse>

    @POST("resend-otp")
    fun resendOtp(@Body request: OtpRequest): retrofit2.Call<OtpResponse>

    @POST("resend-otp-reset")
    fun resendOtpReset(@Body request: OtpRequest): retrofit2.Call<OtpResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: VerifikasiOtpRequest): retrofit2.Call<VerifikasiOtpResponse>

    //Lantai
    @GET("lantai")
    fun getLantai(@Header("Authorization") token: String): Call<LantaiResponse>

    @DELETE("lantai/{id}")
    fun deleteLantai(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    @GET("total-lantai")
    fun getTotalLantai(@Header("Authorization") token: String): Call<LantaiResponse>

    @POST("lantai")
    fun createLantai(
        @Header("Authorization") token: String,
        @Body lantai: Lantai
    ): Call<LantaiResponse>

    //Area
    @GET("area")
    fun getArea(@Header("Authorization") token: String): Call<AreaResponse>

    @POST("area")
    fun createArea(
        @Header("Authorization") token: String,
        @Body area: Area
    ): Call<UpdateAreaResponse>

    @PUT("area/{id}")
    fun updateArea(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body area: Area
    ): Call<UpdateAreaResponse>

    @DELETE("area/{id}")
    fun deleteArea(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    @GET("area/{id}")
    fun getAreaById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<SingleAreaResponse>

    @GET("total-area")
    fun getTotalArea(@Header("Authorization") token: String): Call<AreaResponse>

    //Pic Area
    @GET("pic-area")
    fun getPicArea(@Header("Authorization") token: String): Call<PicAreaResponse>

    //Karyawan
    @GET("karyawan-pic")
    fun getKaryawanPic(@Header("Authorization") token: String): Call<KaryawanResponse>

    //Form
    @GET("form")
    fun getForm(@Header("Authorization") token: String): Call<FormResponse>

    @POST("form")
    fun createForm(
        @Header("Authorization") token: String,
        @Body form: Form
    ): Call<UpdateFormResponse>

    @PUT("form/{id}")
    fun updateForm(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<UpdateFormResponse>

    @DELETE("form/{id}")
    fun deleteForm(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    //Tema
    @GET("tema-form/{id}")
    fun getTemaForm(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<TemaFormResponse>

    @DELETE("tema-form/{id}")
    fun deleteTemaForm(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    @POST("tema-form")
    fun createTemaForm(
        @Header("Authorization") token: String,
        @Body temaForm: TemaForm
    ): Call<UpdateTemaFormResponse>

    @GET("tema-form-single/{id}")
    fun getTemaFormById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<UpdateTemaFormResponse>

    @PUT("tema-form/{id}")
    fun updateTemaForm(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body tema: TemaForm
    ): Call<UpdateTemaFormResponse>

    //Variabel
    @GET("variabel-form/{id}")
    fun getVariabelForm(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<VariabelFormResponse>

    @DELETE("variabel-form/{id}")
    fun deleteVariabelForm(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    //    @POST("variabel-form")
//    fun createVariabelForm(@Header("Authorization") token: String, @Body variabelForm: VariabelForm): Call<UpdateVariabelFormResponse>
    @Multipart
    @POST("variabel-form")
    fun createVariabelForm(
        @Header("Authorization") token: String,
        @Part("tema_form_id") temaFormId: RequestBody,
        @Part("variabel") variabel: RequestBody,
        @Part("standar_variabel") standarVariabel: RequestBody,
        @Part standarFoto: MultipartBody.Part?
    ): Call<UpdateVariabelFormResponse>

    @GET("standar-variabel-foto/{variable_form_id}")
    fun getStandarFotoVariabel(
        @Header("Authorization") token: String,
        @Path("variable_form_id") variableFormId: Int
    ): Call<StandarFotoResponse>

    @PUT("variabel-form/{id}")
    fun updateVariabelForm(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body variabelForm: VariabelForm
    ): Call<UpdateVariabelFormResponse>

//    @PUT("variabel-form/{id}")
//    fun updateVariabelFormWithImage(@Header("Authorization") token: String, @Path("id") id: Int, @Body variabelForm: VariabelForm, @Part standarFoto: MultipartBody.Part?): Call<UpdateVariabelFormResponse>

    @Multipart
    @POST("variabel-form/{id}")
    fun updateVariabelFormWithImage(
        @Header("Authorization") token: String,
        @Path("id") variabelFormId: Int,
        @Part("_method") method: RequestBody,
        @Part("tema_form_id") temaFormId: RequestBody,
        @Part("variabel") variabel: RequestBody,
        @Part("standar_variabel") standarVariabel: RequestBody,
        @Part standarFoto: MultipartBody.Part?
    ): Call<UpdateVariabelFormResponse>

    @GET("variabel-form-single/{id}")
    fun getVariabelFormById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<UpdateVariabelFormResponse>

    @GET("total-variabel")
    fun getTotalVariabel(@Header("Authorization") token: String): Call<VariabelFormResponse>

    //User
    @GET("user/{id}")
    fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<UserResponseGetById>

    //Audit Answer
    @POST("audit-answer-insert")
    fun createAuditAnswer(
        @Header("Authorization") token: String,
        @Body auditAnswer: AuditAnswerItem
    ): Call<AuditAnswerResponseUpdate>

    @GET("audit-answer-total")
    fun getAuditAnswerTotal(@Header("Authorization") token: String): Call<AuditAnswerResponse>

    @GET("audit-answer-area/{areaId}")
    fun getAuditAnswerByArea(
        @Header("Authorization") token: String,
        @Path("areaId") areaId: Int
    ): Call<AuditAnswerResponse>

    @GET("audit-answer-auditor/{id}")
    fun getAuditAnswerAuditor(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<AuditAnswerResponse>

    @GET("audit-answer/{id}")
    fun getAuditAnswerById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<AuditAnswerResponseUpdate>

    //Detail Audit Answer
    @GET("detail-audit-answer/{id}")
    fun getDetailAuditAnswer(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<DetailAuditAnswerResponse>

    @FormUrlEncoded
    @POST("detail-audit-answer/{auditAnswerId}/detail/{detailAuditAnswerId}")
    fun submitAnswer(
        @Header("Authorization") token: String,
        @Path("auditAnswerId") auditAnswerId: Int,
        @Path("detailAuditAnswerId") detailAuditAnswerId: Int,
        @Field("score") score: Int,
        @Field("tertuduh[]") tertuduh: Array<String>,
        @Field("temuan[]") temuan: Array<Int>
    ): Call<Any>

    @Multipart
    @POST("detail-audit-answer/upload-photo")
    fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part("detail_audit_answer_id") detailAuditAnswerId: RequestBody,
        @Part image_path: MultipartBody.Part
    ): Call<DetailFotoResponseUpdate>

    @Multipart
    @POST("detail-audit-answer/upload-signature")
    fun uploadSignature(
        @Header("Authorization") token: String,
        @Part("audit_answer_id") auditAnswerId: RequestBody,
        @Part auditor_signature: MultipartBody.Part,
        @Part auditee_signature: MultipartBody.Part,
        @Part facilitator_signature: MultipartBody.Part
    ): Call<Any>

    @GET("detail-audit-answer-show/{auditAnswerId}")
    fun getDetailAuditAnswerShow(
        @Header("Authorization") token: String,
        @Path("auditAnswerId") auditAnswerId: Int
    ): Call<DetailAuditAnswerResponseUpdate>

    //Audit Office
    @GET("audit-office/detail/{id}")
    fun getDetailAuditOffice(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<DetailAuditAnswerResponseUpdate>

    @GET("audit-office/download/{id}")
    @Streaming
    fun downloadAuditOfficeExcel(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<AuditExcelResponse>

}