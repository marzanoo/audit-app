    package com.example.auditapp.activity

    import android.app.Activity
    import android.content.Intent
    import android.graphics.Bitmap
    import android.net.Uri
    import android.os.Bundle
    import android.provider.MediaStore
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import com.bumptech.glide.Glide
    import com.example.auditapp.R
    import com.example.auditapp.config.ApiServices
    import com.example.auditapp.config.NetworkConfig
    import com.example.auditapp.databinding.FragmentEditVariabelFormBinding
    import com.example.auditapp.helper.SessionManager
    import com.example.auditapp.model.UpdateVariabelFormResponse
    import com.example.auditapp.model.VariabelForm
    import com.example.auditapp.model.VariabelFormResponse
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.MultipartBody
    import okhttp3.RequestBody
    import okhttp3.RequestBody.Companion.asRequestBody
    import okhttp3.RequestBody.Companion.toRequestBody
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import java.io.File
    import java.io.FileOutputStream

    class EditVariabelFormFragment : Fragment() {
        private var _binding: FragmentEditVariabelFormBinding? = null
        private val binding get() = _binding!!
        private lateinit var sessionManager: SessionManager
        private lateinit var apiServices: ApiServices
        private var variabelFormId: Int = 0
        private var currentImageUrl: String? = null
        private var imageUri: Uri? = null
        private var temaFormId: Int = 0
        private var _method: String? = "PUT"
        companion object {
            private const val ARG_VARIABELFORM_ID = "variabelFormId"
            fun newInstance(variabelFormId: Int?): Fragment {
                val fragment = EditVariabelFormFragment()
                val args = Bundle()
                args.putInt(ARG_VARIABELFORM_ID, variabelFormId?: 0)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                variabelFormId = it.getInt(ARG_VARIABELFORM_ID, 0)
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentEditVariabelFormBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            sessionManager = SessionManager(requireContext())
            apiServices = NetworkConfig().getServices()

            variabelFormId?.let {
                if (it > 0) {
                    getVariabelFormById(it)
                }
            }

            binding.backBtn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            binding.btnBatal.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            binding.btnUbah.setOnClickListener {
                updateVariabelForm()
            }

            binding.btnUpload.setOnClickListener {
                openGallery()
            }

        }

        private fun openGallery() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        private fun getVariabelFormById(variabelFormId: Int) {
            val token = sessionManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
                return
            }
            apiServices.getVariabelFormById("Bearer $token", variabelFormId).enqueue(object :
                Callback<UpdateVariabelFormResponse> {
                override fun onResponse(
                    call: Call<UpdateVariabelFormResponse>,
                    response: Response<UpdateVariabelFormResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        binding.etVariabel.setText(responseData?.data?.variabel ?: "")
                        binding.etStandar.setText(responseData?.data?.standar_variabel ?: "")
                        temaFormId = responseData?.data?.tema_form_id ?: 0
                        val BASE_URL = "http://192.168.19.107:8000/storage/"
                        val imageUrl = BASE_URL + responseData?.data?.standar_foto
                        Glide.with(binding.root.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.logo_wag)
                            .error(R.drawable.logo_wag)
                            .into(binding.ivStandar)
                        currentImageUrl = responseData?.data?.standar_foto
                        Log.d("EditVariabelFormFragment", "Response: ${responseData}")
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengambil data variabel form", Toast.LENGTH_SHORT).show()
                        Log.e("EditVariabelFormFragment", "Gagal mengambil data variabel form: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UpdateVariabelFormResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditVariabelFormFragment", "Network Error: ${t.message}")
                }
            })
        }

        private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                    binding.ivStandar.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updateVariabelForm() {
            val token = sessionManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
                return
            }
            val variabel = binding.etVariabel.text.toString().trim()
            val standar = binding.etStandar.text.toString().trim()

            if (variabel.isEmpty() || standar.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT).show()
                return
            }

            binding.btnUbah.isEnabled = false

            if (imageUri != null) {
                updateWithImage(token, variabel, standar)
            } else {
                updateWithExistingImage(token, variabel, standar)
            }
        }

        private fun updateWithImage(token: String, variabel: String, standar: String) {
            val imageFile = createTempFileFromUri(imageUri!!)
            if (imageFile != null) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("standar_foto", imageFile.name, requestFile)
                val methodPart = _method.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val variabelPart = variabel.toRequestBody("text/plain".toMediaTypeOrNull())
                val standarPart = standar.toRequestBody("text/plain".toMediaTypeOrNull())
                val temaFormIdPart = temaFormId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                apiServices.updateVariabelFormWithImage("Bearer $token", variabelFormId, methodPart, temaFormIdPart, variabelPart, standarPart, imagePart).enqueue(
                    object : Callback<UpdateVariabelFormResponse> {
                        override fun onResponse(
                            call: Call<UpdateVariabelFormResponse>,
                            response: Response<UpdateVariabelFormResponse>
                        ) {
                            val rawResponse = response.errorBody()?.string() ?: response.body().toString()
                            Log.d("RAW RESPONSE", rawResponse)
                            binding.btnUbah.isEnabled = true
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Variabel berhasil diubah", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<UpdateVariabelFormResponse>, t: Throwable) {
                            binding.btnUbah.isEnabled = true
                            Log.e("EditVariabelFormFragment", "Network Error: ${t.localizedMessage}", t)
                            Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        private fun createTempFileFromUri(uri: Uri): File? {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                return file
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        private fun updateWithExistingImage(token: String, variabel: String, standar: String) {
            val variabelForm = VariabelForm(
                tema_form_id = temaFormId,
                variabel = variabel,
                standar_variabel = standar,
                standar_foto = currentImageUrl
            )
            variabelFormId?.let { id ->
                apiServices.updateVariabelForm("Bearer $token", id, variabelForm).enqueue(object :
                    Callback<UpdateVariabelFormResponse> {
                    override fun onResponse(
                        call: Call<UpdateVariabelFormResponse>,
                        response: Response<UpdateVariabelFormResponse>
                    ) {
                        binding.btnUbah.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Variabel berhasil diubah", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Gagal mengubah variabel", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UpdateVariabelFormResponse>, t: Throwable) {
                        binding.btnUbah.isEnabled = true
                        Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("EditVariabelFormFragment", "Network Error: ${t.message}")
                    }
                })
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }