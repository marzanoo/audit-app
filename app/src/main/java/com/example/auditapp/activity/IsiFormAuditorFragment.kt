package com.example.auditapp.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.ListFormAuditorAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentIsiFormAuditorBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.DetailAuditAnswer
import com.example.auditapp.model.DetailAuditAnswerResponse
import com.example.auditapp.model.DetailFoto
import com.example.auditapp.model.DetailFotoResponseUpdate
import com.example.auditapp.model.StandarFotoResponse
import com.example.auditapp.signature.SignatureDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class IsiFormAuditorFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    ListFormAuditorAdapter.OnItemClickListener {
    private var _binding: FragmentIsiFormAuditorBinding? = null
    private val binding get() = _binding!!
    private var auditAnswerId: Int = 0
    private val listAuditAnswer = mutableListOf<DetailAuditAnswer>()
    private lateinit var auditAnswerAdapter: ListFormAuditorAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private var imageUri: Uri? = null
    private var selectedPosition: Int = -1
    private val localAuditAnswerChanges = mutableMapOf<Int, DetailAuditAnswer>()
    private val localPhotos = mutableMapOf<Int, MutableList<DetailFoto>>()
    private var loadingDialog: AlertDialog? = null

    // Signature Variables
    private var auditorSignatureFile: File? = null
    private var auditeeSignatureFile: File? = null
    private var facilitatorSignatureFile: File? = null

    // Status tracking variables
    private var signatureCompleted = false
    private var auditDataCompleted = false

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        private const val CAMERA_PERMISSION_REQUEST = 101
        private const val MAX_RETRIES = 3

        fun newInstance(auditAnswerId: Int): Fragment {
            val fragment = IsiFormAuditorFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId)
            fragment.arguments = args
            return fragment
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (selectedPosition != -1) {
                openCamera()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Izin kamera diperlukan untuk mengambil foto",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null && isAdded && selectedPosition != -1 && selectedPosition < listAuditAnswer.size) {
                val newPhoto = DetailFoto(
                    id = 0,
                    detail_audit_answer_id = listAuditAnswer[selectedPosition].id ?: 0,
                    image_path = imageUri.toString(),
                    uri = imageUri
                )
                if (listAuditAnswer[selectedPosition].listDetailFoto == null) {
                    listAuditAnswer[selectedPosition].listDetailFoto = mutableListOf()
                }
                listAuditAnswer[selectedPosition].listDetailFoto?.add(newPhoto)
                val detailAuditAnswerId = listAuditAnswer[selectedPosition].id ?: 0
                if (!localPhotos.containsKey(detailAuditAnswerId)) {
                    localPhotos[detailAuditAnswerId] = mutableListOf()
                }
                localPhotos[detailAuditAnswerId]?.add(newPhoto)
                auditAnswerAdapter.notifyItemChanged(selectedPosition)
            } else {
                Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auditAnswerId = it.getInt(ARG_AUDITANSWER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIsiFormAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.GONE

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        binding.swipeRefreshLayout.setOnRefreshListener(this)
        setupRecyclerView()
        setupSignatures()
        getDetailAuditAnswer(auditAnswerId)

        binding.btnSimpan.setOnClickListener {
            signatureCompleted = false
            auditDataCompleted = false
            showLoadingDialog()
            saveAuditData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissLoadingDialog()
        _binding = null
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        auditAnswerAdapter = ListFormAuditorAdapter(listAuditAnswer, this)
        binding.recyclerViewForm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditAnswerAdapter
        }
    }

    private fun setupSignatures() {
        binding.auditorSignatureContainer.setOnClickListener {
            showSignatureDialog("auditor")
        }
        binding.auditorClearSignatureBtn.setOnClickListener {
            clearSignature("auditor")
        }
        binding.auditeeSignatureContainer.setOnClickListener {
            showSignatureDialog("auditee")
        }
        binding.auditeeClearSignatureBtn.setOnClickListener {
            clearSignature("auditee")
        }
        binding.facilitatorSignatureContainer.setOnClickListener {
            showSignatureDialog("facilitator")
        }
        binding.facilitatorClearSignatureBtn.setOnClickListener {
            clearSignature("facilitator")
        }
    }

    private fun showSignatureDialog(signatureType: String) {
        val dialog = SignatureDialog(requireContext())
        dialog.setOnSignatureCompletedListener { bitmap ->
            when (signatureType) {
                "auditor" -> saveSignature(bitmap, "auditor_signature.png", "auditor")
                "auditee" -> saveSignature(bitmap, "auditee_signature.png", "auditee")
                "facilitator" -> saveSignature(bitmap, "facilitator_signature.png", "facilitator")
            }
            updateSaveButtonState()
        }
        dialog.show()
    }

    private fun saveSignature(bitmap: Bitmap, fileName: String, signatureType: String) {
        val file = File(requireContext().cacheDir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
        }
        when (signatureType) {
            "auditor" -> {
                auditorSignatureFile = file
                binding.auditorSignatureImage.setImageBitmap(bitmap)
                binding.auditorSignatureImage.visibility = View.VISIBLE
                binding.auditorSignatureEmptyText.visibility = View.GONE
            }

            "auditee" -> {
                auditeeSignatureFile = file
                binding.auditeeSignatureImage.setImageBitmap(bitmap)
                binding.auditeeSignatureImage.visibility = View.VISIBLE
                binding.auditeeSignatureEmptyText.visibility = View.GONE
            }

            "facilitator" -> {
                facilitatorSignatureFile = file
                binding.facilitatorSignatureImage.setImageBitmap(bitmap)
                binding.facilitatorSignatureImage.visibility = View.VISIBLE
                binding.facilitatorSignatureEmptyText.visibility = View.GONE
            }
        }
    }

    private fun clearSignature(signatureType: String) {
        when (signatureType) {
            "auditor" -> {
                auditorSignatureFile = null
                binding.auditorSignatureImage.setImageBitmap(null)
                binding.auditorSignatureImage.visibility = View.GONE
                binding.auditorSignatureEmptyText.visibility = View.VISIBLE
            }

            "auditee" -> {
                auditeeSignatureFile = null
                binding.auditeeSignatureImage.setImageBitmap(null)
                binding.auditeeSignatureImage.visibility = View.GONE
                binding.auditeeSignatureEmptyText.visibility = View.VISIBLE
            }

            "facilitator" -> {
                facilitatorSignatureFile = null
                binding.facilitatorSignatureImage.setImageBitmap(null)
                binding.facilitatorSignatureImage.visibility = View.GONE
                binding.facilitatorSignatureEmptyText.visibility = View.VISIBLE
            }
        }
        updateSaveButtonState()
    }

    private fun validateSignatures(): Boolean {
        return auditorSignatureFile != null && auditeeSignatureFile != null && facilitatorSignatureFile != null
    }

    private fun updateSaveButtonState() {
        binding.btnSimpan.isEnabled = validateSignatures()
    }

    private fun showLoadingDialog() {
        if (isAdded && _binding != null) {
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
            val textView = dialogView.findViewById<TextView>(R.id.textLoading)
            textView.text = "Menyimpan data..."

            loadingDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            loadingDialog?.show()
            binding.btnSimpan.isEnabled = false // Nonaktifkan tombol Simpan
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
        if (isAdded && _binding != null) {
            binding.btnSimpan.isEnabled = validateSignatures() // Aktifkan kembali tombol Simpan
        }
    }

    private fun getDetailAuditAnswer(auditAnswerId: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            dismissLoadingDialog()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
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
                        detailAuditAnswer.filterNotNull().forEach { serverAnswer ->
                            val localAnswer = localAuditAnswerChanges[serverAnswer.id ?: 0]
                            val mergedAnswer = if (localAnswer != null) {
                                serverAnswer.apply {
                                    score = localAnswer.score ?: score
                                    listTertuduh = localAnswer.listTertuduh ?: listTertuduh
                                    listDetailFoto =
                                        localPhotos[serverAnswer.id ?: 0] ?: listDetailFoto
                                }
                            } else {
                                serverAnswer
                            }
                            listAuditAnswer.add(mergedAnswer)
                            mergedAnswer.variabelFormId?.let { variabelFormId ->
                                fetchStandarFotoVariabel(variabelFormId, mergedAnswer)
                            }
                        }
                        auditAnswerAdapter.notifyDataSetChanged()

                        auditorSignatureFile?.let {
                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            binding.auditorSignatureImage.setImageBitmap(bitmap)
                            binding.auditorSignatureImage.visibility = View.VISIBLE
                            binding.auditorSignatureEmptyText.visibility = View.GONE
                        }
                        auditeeSignatureFile?.let {
                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            binding.auditeeSignatureImage.setImageBitmap(bitmap)
                            binding.auditeeSignatureImage.visibility = View.VISIBLE
                            binding.auditeeSignatureEmptyText.visibility = View.GONE
                        }
                        facilitatorSignatureFile?.let {
                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            binding.facilitatorSignatureImage.setImageBitmap(bitmap)
                            binding.facilitatorSignatureImage.visibility = View.VISIBLE
                            binding.facilitatorSignatureEmptyText.visibility = View.GONE
                        }
                        updateSaveButtonState()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data, mencoba lagi...",
                        Toast.LENGTH_SHORT
                    ).show()
                    retryGetDetailAuditAnswer(auditAnswerId, 0)
                }
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                dismissLoadingDialog()
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Jaringan bermasalah, mencoba lagi...",
                    Toast.LENGTH_SHORT
                ).show()
                retryGetDetailAuditAnswer(auditAnswerId, 0)
            }
        })
    }

    private fun retryGetDetailAuditAnswer(auditAnswerId: Int, retryCount: Int) {
        if (retryCount >= MAX_RETRIES) {
            Toast.makeText(
                requireContext(),
                "Gagal mengambil data setelah beberapa percobaan",
                Toast.LENGTH_LONG
            ).show()
            binding.progressBar.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            dismissLoadingDialog()
            return
        }
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            dismissLoadingDialog()
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
                        detailAuditAnswer.filterNotNull().forEach { serverAnswer ->
                            val localAnswer = localAuditAnswerChanges[serverAnswer.id ?: 0]
                            val mergedAnswer = if (localAnswer != null) {
                                serverAnswer.apply {
                                    score = localAnswer.score ?: score
                                    listTertuduh = localAnswer.listTertuduh ?: listTertuduh
                                    listDetailFoto =
                                        localPhotos[serverAnswer.id ?: 0] ?: listDetailFoto
                                }
                            } else {
                                serverAnswer
                            }
                            listAuditAnswer.add(mergedAnswer)
                            mergedAnswer.variabelFormId?.let { variabelFormId ->
                                fetchStandarFotoVariabel(variabelFormId, mergedAnswer)
                            }
                        }
                        auditAnswerAdapter.notifyDataSetChanged()
                    }
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    dismissLoadingDialog()
                } else {
                    retryGetDetailAuditAnswer(auditAnswerId, retryCount + 1)
                }
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponse>, t: Throwable) {
                retryGetDetailAuditAnswer(auditAnswerId, retryCount + 1)
            }
        })
    }

    private fun fetchStandarFotoVariabel(variableFormId: Int, auditAnswer: DetailAuditAnswer) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) return

        apiServices.getStandarFotoVariabel("Bearer $token", variableFormId).enqueue(object :
            Callback<StandarFotoResponse> {
            override fun onResponse(
                call: Call<StandarFotoResponse>,
                response: Response<StandarFotoResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { standarFotos ->
                        auditAnswer.listStandarFoto = standarFotos.toMutableList()
                        val position = listAuditAnswer.indexOf(auditAnswer)
                        if (position != -1 && isAdded && _binding != null) {
                            auditAnswerAdapter.notifyItemChanged(position)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StandarFotoResponse>, t: Throwable) {
                // Silent retry for non-critical data
            }
        })
    }

    private fun saveAuditData() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            dismissLoadingDialog()
            return
        }

        if (!validateSignatures()) {
            Toast.makeText(
                requireContext(),
                "Harap lengkapi semua tanda tangan",
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBar.visibility = View.GONE
            dismissLoadingDialog()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val totalItems = listAuditAnswer.size
        var completedItems = 0
        var photosUploading = false

        if (totalItems == 0) {
            auditDataCompleted = true
            saveFormWithSignatures()
            return
        }

        listAuditAnswer.forEach { detailAuditAnswer ->
            if (detailAuditAnswer.score == null) {
                Toast.makeText(
                    requireContext(),
                    "Skor tidak boleh kosong untuk item ${detailAuditAnswer.id}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
                dismissLoadingDialog()
                return
            }
            retrySubmitAnswer(detailAuditAnswer, token, 0) { success ->
                if (success) {
                    val hasPhotosToUpload = detailAuditAnswer.listDetailFoto?.any {
                        (it.id ?: 0) <= 0 && it.uri != null
                    } ?: false
                    if (hasPhotosToUpload) {
                        photosUploading = true
                        retryUploadPhotos(detailAuditAnswer, token, 0) { photoSuccess ->
                            completedItems++
                            if (completedItems == totalItems) {
                                auditDataCompleted = true
                                saveFormWithSignatures()
                            }
                        }
                    } else {
                        completedItems++
                        if (completedItems == totalItems) {
                            auditDataCompleted = true
                            saveFormWithSignatures()
                        }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan item ${detailAuditAnswer.id}, silakan coba lagi",
                        Toast.LENGTH_LONG
                    ).show()
                    dismissLoadingDialog()
                }
            }
        }
    }

    private fun retrySubmitAnswer(
        detailAuditAnswer: DetailAuditAnswer,
        token: String,
        retryCount: Int,
        callback: (Boolean) -> Unit
    ) {
        if (retryCount >= MAX_RETRIES) {
            callback(false)
            return
        }
        val score = detailAuditAnswer.score ?: 0
        val tertuduhArray =
            detailAuditAnswer.listTertuduh?.mapNotNull { it.name }?.toTypedArray() ?: emptyArray()
        val temuanArray = detailAuditAnswer.listTertuduh?.mapNotNull { it.temuan }?.toTypedArray()
            ?: emptyArray<Int>()

        apiServices.submitAnswer(
            "Bearer $token",
            auditAnswerId,
            detailAuditAnswer.id ?: 0,
            score,
            tertuduhArray,
            temuanArray
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    localAuditAnswerChanges.remove(detailAuditAnswer.id ?: 0)
                    callback(true)
                } else {
                    retrySubmitAnswer(detailAuditAnswer, token, retryCount + 1, callback)
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                retrySubmitAnswer(detailAuditAnswer, token, retryCount + 1, callback)
            }
        })
    }

    private fun retryUploadPhotos(
        detailAuditAnswer: DetailAuditAnswer,
        token: String,
        retryCount: Int,
        callback: (Boolean) -> Unit
    ) {
        if (retryCount >= MAX_RETRIES) {
            callback(false)
            return
        }
        val photosToUpload = detailAuditAnswer.listDetailFoto?.filter {
            (it.id ?: 0) <= 0 && it.uri != null
        }
        if (photosToUpload.isNullOrEmpty()) {
            callback(true)
            return
        }

        val totalPhotos = photosToUpload.size
        var completedPhotos = 0
        var allSuccess = true

        photosToUpload.forEach { photo ->
            val imageFile = createTempFileFromUri(photo.uri!!)
            if (imageFile != null) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart =
                    MultipartBody.Part.createFormData("image_path", imageFile.name, requestFile)
                val detailAuditAnswerIdBody =
                    detailAuditAnswer.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                apiServices.uploadPhoto(
                    "Bearer $token",
                    detailAuditAnswerIdBody,
                    imagePart
                ).enqueue(object : Callback<DetailFotoResponseUpdate> {
                    override fun onResponse(
                        call: Call<DetailFotoResponseUpdate>,
                        response: Response<DetailFotoResponseUpdate>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.data?.let { uploadedPhoto ->
                                photo.id = uploadedPhoto.id
                                photo.image_path = uploadedPhoto.image_path
                                if (isAdded && _binding != null) {
                                    val position = listAuditAnswer.indexOf(detailAuditAnswer)
                                    if (position != -1) {
                                        auditAnswerAdapter.notifyItemChanged(position)
                                    }
                                }
                            }
                        } else {
                            allSuccess = false
                        }
                        completedPhotos++
                        if (completedPhotos == totalPhotos) {
                            if (allSuccess) {
                                localPhotos.remove(detailAuditAnswer.id ?: 0)
                                callback(true)
                            } else {
                                retryUploadPhotos(
                                    detailAuditAnswer,
                                    token,
                                    retryCount + 1,
                                    callback
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<DetailFotoResponseUpdate>, t: Throwable) {
                        allSuccess = false
                        completedPhotos++
                        if (completedPhotos == totalPhotos) {
                            retryUploadPhotos(detailAuditAnswer, token, retryCount + 1, callback)
                        }
                    }
                })
            } else {
                completedPhotos++
                allSuccess = false
                if (completedPhotos == totalPhotos) {
                    retryUploadPhotos(detailAuditAnswer, token, retryCount + 1, callback)
                }
            }
        }
    }

    private fun saveFormWithSignatures() {
        if (!validateSignatures()) {
            Toast.makeText(
                requireContext(),
                "Harap lengkapi semua tanda tangan",
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBar.visibility = View.GONE
            dismissLoadingDialog()
            return
        }

        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            dismissLoadingDialog()
            return
        }

        retryUploadSignatures(token, 0)
    }

    private fun retryUploadSignatures(token: String, retryCount: Int) {
        if (retryCount >= MAX_RETRIES) {
            Toast.makeText(
                requireContext(),
                "Gagal menyimpan tanda tangan setelah beberapa percobaan",
                Toast.LENGTH_LONG
            ).show()
            binding.progressBar.visibility = View.GONE
            dismissLoadingDialog()
            return
        }

        val auditorSignaturePart =
            createMultipartFromFile(auditorSignatureFile!!, "auditor_signature")
        val auditeeSignaturePart =
            createMultipartFromFile(auditeeSignatureFile!!, "auditee_signature")
        val facilitatorSignaturePart =
            createMultipartFromFile(facilitatorSignatureFile!!, "facilitator_signature")
        val auditAnswerIdBody =
            auditAnswerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        apiServices.uploadSignature(
            "Bearer $token",
            auditAnswerIdBody,
            auditorSignaturePart,
            auditeeSignaturePart,
            facilitatorSignaturePart
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    signatureCompleted = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Semua data berhasil disimpan",
                        Toast.LENGTH_SHORT
                    ).show()
                    localAuditAnswerChanges.clear()
                    localPhotos.clear()
                    auditorSignatureFile = null
                    auditeeSignatureFile = null
                    facilitatorSignatureFile = null
                    navToHomeAuditor()
                    dismissLoadingDialog()
                } else {
                    retryUploadSignatures(token, retryCount + 1)
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                retryUploadSignatures(token, retryCount + 1)
            }
        })
    }

    private fun openCamera() {
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        takePicture.launch(imageUri)
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            val file =
                File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun createMultipartFromFile(file: File, paramName: String): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, file.name, requestFile)
    }

    private fun navToHomeAuditor() {
        if (!isAdded) return
        val fragment = HomeAuditorFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onRefresh() {
        getDetailAuditAnswer(auditAnswerId)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onTakePictureClick(position: Int) {
        selectedPosition = position
        val viewHolder = binding.recyclerViewForm.findViewHolderForAdapterPosition(position)
                as? ListFormAuditorAdapter.ViewHolder
        viewHolder?.let {
            it.binding.etTertuduh.clearFocus()
            it.binding.etTemuan.clearFocus()
            it.binding.etScore.clearFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.binding.root.windowToken, 0)
        }
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
            val auditAnswer = listAuditAnswer[position]
            auditAnswer.score = score
            localAuditAnswerChanges[auditAnswer.id ?: 0] = auditAnswer.copy()
        }
    }
}