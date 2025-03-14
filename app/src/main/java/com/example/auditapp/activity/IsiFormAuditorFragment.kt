package com.example.auditapp.activity

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.FormAnswerAdapter
import com.example.auditapp.adapter.ListFormAuditorAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentIsiFormAuditorBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.DetailAuditAnswer
import com.example.auditapp.model.DetailAuditAnswerResponse
import com.example.auditapp.model.DetailFoto
import com.example.auditapp.model.DetailFotoResponseUpdate
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class IsiFormAuditorFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, ListFormAuditorAdapter.OnItemClickListener {
    private var _binding: FragmentIsiFormAuditorBinding? = null
    private val binding get() = _binding!!
    private var auditAnswerId: Int = 0
    private val listAuditAnswer = mutableListOf<DetailAuditAnswer>()
    private lateinit var auditAnswerAdapter: ListFormAuditorAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private var imageUri: Uri? = null
    private var selectedPosition: Int = -1

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, take picture
            if (selectedPosition != -1) {
                openCamera()
            }
        } else {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            if (selectedPosition != -1) {
                // Create a new DetailFoto object with the URI
                val newPhoto = DetailFoto(
                    id = 0, // You might need to generate a unique ID
                    detail_audit_answer_id = listAuditAnswer[selectedPosition].id ?: 0,
                    image_path = imageUri.toString(),
                    uri = imageUri
                )

                // Add the photo to the list if it doesn't exist yet
                if (listAuditAnswer[selectedPosition].listDetailFoto == null) {
                    listAuditAnswer[selectedPosition].listDetailFoto = mutableListOf()
                }

                listAuditAnswer[selectedPosition].listDetailFoto?.add(newPhoto)

                // Update the adapter
                auditAnswerAdapter.notifyItemChanged(selectedPosition)
            }
        } else {
            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        private const val CAMERA_PERMISSION_REQUEST = 101
        fun newInstance(auditAnswerId: Int): Fragment {
            val fragment = IsiFormAuditorFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auditAnswerId = it.getInt(ARG_AUDITANSWER_ID)
        }
    }

    private fun getDetailAuditAnswer(auditAnswerId: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getDetailAuditAnswer("Bearer $token", auditAnswerId).enqueue(object :
            Callback<DetailAuditAnswerResponse> {
            override fun onResponse(
                call: Call<DetailAuditAnswerResponse>,
                response: Response<DetailAuditAnswerResponse>
            ) {
                if (response.isSuccessful) {
                    val detailAuditAnswerResponse = response.body()
                    detailAuditAnswerResponse?.data?.let { detailAuditAnswer ->
                        listAuditAnswer.clear()
                        listAuditAnswer.addAll(detailAuditAnswer.filterNotNull())
                        auditAnswerAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data audit answer", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("IsiFormAuditorFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun openCamera() {
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        takePicture.launch(imageUri)
    }


    override fun onTakePictureClick(position: Int) {
        selectedPosition = position

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onScoreChanged(position: Int, score: Int) {
        if (position < listAuditAnswer.size) {
            listAuditAnswer[position].score = score
            // You may want to update the backend with the new score
        }
    }

    private fun setupRecylerView() {
        auditAnswerAdapter = ListFormAuditorAdapter(listAuditAnswer, this)
        binding.recyclerViewForm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditAnswerAdapter
        }

    }

    override fun onRefresh() {
        getDetailAuditAnswer(auditAnswerId)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentIsiFormAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.GONE
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        setupRecylerView()
        getDetailAuditAnswer(auditAnswerId)

        binding.btnSimpan.setOnClickListener {
            saveAuditData()
        }
    }

    private fun navToHomeAuditor() {
        val fragment = HomeAuditorFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun saveAuditData() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val totalItems = listAuditAnswer.size
        var completedItems = 0
        var failedItems = 0

        listAuditAnswer.forEach { detailAuditAnswer ->
            val score = detailAuditAnswer.score ?: 0

            val tertuduh = detailAuditAnswer.tertuduh

            apiServices.submitAnswer("Bearer $token", auditAnswerId, detailAuditAnswer.id?: 0, score, tertuduh).enqueue(
                object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            uploadPhotos(detailAuditAnswer, token)
                        } else {
                            Toast.makeText(requireContext(), "Gagal menyimpan data: ${response.message()}", Toast.LENGTH_SHORT).show()
                            Log.e("IsiFormAuditorFragment", "API Error: ${response.errorBody()?.string()}")
                        }

                        completedItems++
                        checkProgress(completedItems, failedItems, totalItems)
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        failedItems++
                        completedItems++
                        checkProgress(completedItems, failedItems, totalItems)
                        Log.e("IsiFormAuditorFragment", "Submit Error: ${t.message}")
                    }
                })
        }
    }

    private fun uploadPhotos(detailAuditAnswer: DetailAuditAnswer, token: String) {
        val photos = detailAuditAnswer.listDetailFoto ?: return

        photos.forEach { photo ->
            if ((photo.id ?: 0) > 0 || photo.uri == null ) return@forEach

            val imageFile = createTempFileFromUri(photo.uri!!)
            if (imageFile != null) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image_path", imageFile.name, requestFile)
                val detailAuditAnswerIdBody = detailAuditAnswer.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                apiServices.uploadPhoto("Bearer $token", detailAuditAnswerIdBody, imagePart).enqueue(
                    object : Callback<DetailFotoResponseUpdate> {
                        override fun onResponse(
                            call: Call<DetailFotoResponseUpdate>,
                            response: Response<DetailFotoResponseUpdate>
                        ) {
                            Log.d("IsiFormAuditorFragment", "Response code: ${response.code()}")
                            Log.d("IsiFormAuditorFragment", "Response body: ${response.body()}")
                            if (response.isSuccessful) {
                                val responseData = response.body()
                                responseData?.data?.let { uploadedPhoto ->
                                    photo.id = uploadedPhoto.id
                                    photo.image_path = uploadedPhoto.image_path
                                    val position = listAuditAnswer.indexOf(detailAuditAnswer)
                                    auditAnswerAdapter.notifyItemChanged(position)
                                }
                                Toast.makeText(requireContext(), "Foto berhasil diunggah", Toast.LENGTH_SHORT).show()
                                navToHomeAuditor()
                            } else {
                                Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                                Log.e("IsiFormAuditorFragment", "Error body: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<DetailFotoResponseUpdate>, t: Throwable) {
                            Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            Log.e("IsiFormAuditorFragment", "Network Error: ${t.message}")
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
            }
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
            Log.e("IsiFormAuditorFragment", "Error: ${e.message}")
            return null
        }
    }

//    private fun getRealPathFromURI(uri: Uri): String? {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
//        return cursor?.use {
//            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            it.moveToFirst()
//            it.getString(columnIndex)
//        } ?: run {
//            // If cursor is null, try to get path directly from URI (works for file:// URIs)
//            uri.path
//        }
//    }

    private fun checkProgress(completed: Int, failed: Int, total: Int) {
        if (completed == total) {
            // All items processed, hide loading
            binding.progressBar.visibility = View.GONE

            if (failed > 0) {
                Toast.makeText(
                    requireContext(),
                    "Selesai dengan $failed item gagal",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menyimpan semua data",
                    Toast.LENGTH_SHORT
                ).show()

                // Refresh the data to show the updated values
                getDetailAuditAnswer(auditAnswerId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.VISIBLE
    }
}
