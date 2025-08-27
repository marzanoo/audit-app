package com.example.auditapp.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

        fun newInstance(auditAnswerId: Int): Fragment {
            val fragment = IsiFormAuditorFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId)
            fragment.arguments = args
            return fragment
        }
    }

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
            Toast.makeText(
                requireContext(),
                "Izin kamera diperlukan untuk mengambil foto",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // In IsiFormAuditorFragment.kt, update the takePicture result handler
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null && isAdded && selectedPosition != -1 && selectedPosition < listAuditAnswer.size) {
                // Create a new DetailFoto object with the URI
                val newPhoto = DetailFoto(
                    id = 0,
                    detail_audit_answer_id = listAuditAnswer[selectedPosition].id ?: 0,
                    image_path = imageUri.toString(),
                    uri = imageUri
                )

                // Add the photo to the list if it doesn't exist yet
                if (listAuditAnswer[selectedPosition].listDetailFoto == null) {
                    listAuditAnswer[selectedPosition].listDetailFoto = mutableListOf()
                }

                listAuditAnswer[selectedPosition].listDetailFoto?.add(newPhoto)

                // Store in local photos
                val detailAuditAnswerId = listAuditAnswer[selectedPosition].id ?: 0
                if (!localPhotos.containsKey(detailAuditAnswerId)) {
                    localPhotos[detailAuditAnswerId] = mutableListOf()
                }
                localPhotos[detailAuditAnswerId]?.add(newPhoto)
                // Update the adapter
                auditAnswerAdapter.notifyItemChanged(selectedPosition)
            } else if (success && imageUri != null) {
                Toast.makeText(
                    requireContext(),
                    "Gagal menambahkan foto: Posisi tidak valid",
                    Toast.LENGTH_SHORT
                ).show()
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

        // Hide bottom navigation
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.GONE

        // Initialize components
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        // Setup UI elements
        setupSignatures()
        setupRecyclerView()

        // Fetch data
        getDetailAuditAnswer(auditAnswerId)

        // Setup save button
        binding.btnSimpan.setOnClickListener {
            // Reset status tracking variables
            signatureCompleted = false
            auditDataCompleted = false

            // Start save processes
            saveAuditData()
            saveFormWithSignatures()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Restore bottom navigation visibility
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.VISIBLE
    }

    // UI Setup Methods
    private fun setupRecyclerView() {
        auditAnswerAdapter = ListFormAuditorAdapter(listAuditAnswer, this)
        binding.recyclerViewForm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditAnswerAdapter
        }
    }

    private fun setupSignatures() {
        // Auditor signature
        binding.auditorSignatureContainer.setOnClickListener {
            showSignatureDialog("auditor")
        }

        binding.auditorClearSignatureBtn.setOnClickListener {
            clearSignature("auditor")
        }

        // Auditee signature
        binding.auditeeSignatureContainer.setOnClickListener {
            showSignatureDialog("auditee")
        }

        binding.auditeeClearSignatureBtn.setOnClickListener {
            clearSignature("auditee")
        }

        // Facilitator signature
        binding.facilitatorSignatureContainer.setOnClickListener {
            showSignatureDialog("facilitator")
        }

        binding.facilitatorClearSignatureBtn.setOnClickListener {
            clearSignature("facilitator")
        }
    }

    // Signature Methods
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

    // Network Methods
    private fun getDetailAuditAnswer(auditAnswerId: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
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
                        // Merge server data with local changes
                        detailAuditAnswer.filterNotNull().forEach { serverAnswer ->
                            val localAnswer = localAuditAnswerChanges[serverAnswer.id ?: 0]
                            val mergedAnswer = if (localAnswer != null) {
                                // Merge fields (e.g., score, tertuduh, etc.)
                                serverAnswer.apply {
                                    score = localAnswer.score ?: score
                                    listTertuduh = localAnswer.listTertuduh ?: listTertuduh
                                    // Merge photos
                                    listDetailFoto =
                                        localPhotos[serverAnswer.id ?: 0]
                                            ?: serverAnswer.listDetailFoto
                                }
                            } else {
                                serverAnswer
                            }
                            listAuditAnswer.add(mergedAnswer)
                            // Fetch standar foto for each audit answer
                            mergedAnswer.variabelFormId?.let { variabelFormId ->
                                fetchStandarFotoVariabel(variabelFormId, mergedAnswer)
                            }
                        }
//                        listAuditAnswer.addAll(detailAuditAnswer.filterNotNull())
//                        // Fetch standar foto for each audit answer
//                        listAuditAnswer.forEach { auditAnswer ->
//                            auditAnswer.variabelFormId?.let { variabelFormId ->
//                                fetchStandarFotoVariabel(variabelFormId, auditAnswer)
//                            }
//                        }
                        auditAnswerAdapter.notifyDataSetChanged()

                        // Reapply signatures to UI
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
                        "Gagal mengambil data audit answer",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("IsiFormAuditorFragment", "Network Error: ${t.message}")
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
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
                        Log.d(
                            "IsiFormAuditorFragment",
                            "Fetched ${standarFotos.size} standar fotos for variableFormId: $variableFormId"
                        )
                        val position = listAuditAnswer.indexOf(auditAnswer)
                        if (position != -1 && isAdded && _binding != null) {
                            auditAnswerAdapter.notifyItemChanged(position)
                        }
                    } ?: run {
                        Log.d(
                            "IsiFormAuditorFragment",
                            "No standar fotos found for variableFormId: $variableFormId"
                        )
                    }
                } else {
                    Log.e(
                        "IsiFormAuditorFragment",
                        "Gagal mengambil foto standar: ${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<StandarFotoResponse>, t: Throwable) {
                Log.e("IsiFormAuditorFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun saveAuditData() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        if (!validateSignatures()) {
            Toast.makeText(
                requireContext(),
                "Harap lengkapi semua tanda tangan",
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        val totalItems = listAuditAnswer.size
        var completedItems = 0
        var failedItems = 0
        var photosUploading = false

        // If there are no audit items, mark as completed right away
        if (totalItems == 0) {
            auditDataCompleted = true
            checkAllOperationsComplete()
            return
        }

        listAuditAnswer.forEach { detailAuditAnswer ->
            val score = detailAuditAnswer.score ?: 0
            val tertuduhArray =
                detailAuditAnswer.listTertuduh?.map { it.name }?.toTypedArray() ?: emptyArray()
            val temuanArray =
                detailAuditAnswer.listTertuduh?.map { it.temuan }?.toTypedArray() ?: emptyArray()

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
                        // Check if this item has photos to upload
                        val hasPhotosToUpload = detailAuditAnswer.listDetailFoto?.any {
                            (it.id ?: 0) <= 0 && it.uri != null
                        } ?: false

                        if (hasPhotosToUpload) {
                            photosUploading = true
                            uploadPhotos(detailAuditAnswer, token) { success ->
                                if (!success) {
                                    Log.e("IsiFormAuditorFragment", "Failed to upload some photos")
                                }

                                completedItems++
                                checkProgress(completedItems, failedItems, totalItems)
                            }
                        } else {
                            completedItems++
                            checkProgress(completedItems, failedItems, totalItems)
                        }
                    } else {
                        failedItems++
                        completedItems++
                        checkProgress(completedItems, failedItems, totalItems)
                        Log.e(
                            "IsiFormAuditorFragment",
                            "API Error: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    failedItems++
                    completedItems++
                    checkProgress(completedItems, failedItems, totalItems)
                    Log.e("IsiFormAuditorFragment", "Submit Error: ${t.message}")
                }
            })
        }

        // If no photos need to be uploaded and all submissions failed immediately
        if (!photosUploading && completedItems == totalItems) {
            checkProgress(completedItems, failedItems, totalItems)
        }
    }

    private fun uploadPhotos(
        detailAuditAnswer: DetailAuditAnswer,
        token: String,
        callback: (Boolean) -> Unit
    ) {
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
                            val responseData = response.body()
                            responseData?.data?.let { uploadedPhoto ->
                                photo.id = uploadedPhoto.id
                                photo.image_path = uploadedPhoto.image_path

                                // Only try to update the UI if the fragment is still attached
                                if (isAdded && _binding != null) {
                                    val position = listAuditAnswer.indexOf(detailAuditAnswer)
                                    if (position != -1) {
                                        auditAnswerAdapter.notifyItemChanged(position)
                                    }
                                }
                            }
                        } else {
                            allSuccess = false
                            Log.e(
                                "IsiFormAuditorFragment",
                                "Error body: ${response.errorBody()?.string()}"
                            )
                        }

                        completedPhotos++
                        if (completedPhotos == totalPhotos) {
                            callback(allSuccess)
                        }
                    }

                    override fun onFailure(call: Call<DetailFotoResponseUpdate>, t: Throwable) {
                        allSuccess = false
                        Log.e("IsiFormAuditorFragment", "Network Error: ${t.message}")

                        completedPhotos++
                        if (completedPhotos == totalPhotos) {
                            callback(allSuccess)
                        }
                    }
                })
            } else {
                allSuccess = false
                completedPhotos++
                if (completedPhotos == totalPhotos) {
                    callback(allSuccess)
                }
            }
        }
    }

    private fun saveFormWithSignatures() {
        // Ensure we have all signatures
        if (!validateSignatures()) {
            Toast.makeText(
                requireContext(),
                "Harap lengkapi semua tanda tangan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Create multipart files for signatures
        val auditorSignaturePart =
            createMultipartFromFile(auditorSignatureFile!!, "auditor_signature")
        val auditeeSignaturePart =
            createMultipartFromFile(auditeeSignatureFile!!, "auditee_signature")
        val facilitatorSignaturePart =
            createMultipartFromFile(facilitatorSignatureFile!!, "facilitator_signature")

        // Create request body for audit answer ID
        val auditAnswerIdBody =
            auditAnswerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        // Call API to save form with signatures
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            return
        }


        apiServices.uploadSignature(
            "Bearer $token",
            auditAnswerIdBody,
            auditorSignaturePart,
            auditeeSignaturePart,
            facilitatorSignaturePart
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // Only update UI if the fragment is still attached
                if (isAdded && _binding != null) {
                    if (!response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Gagal menyimpan tanda tangan: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }

                // Mark signature upload as completed
                signatureCompleted = true

                // Check if we can proceed to home screen
                checkAllOperationsComplete()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Only update UI if the fragment is still attached
                if (isAdded && _binding != null) {
                    Toast.makeText(
                        requireContext(),
                        "Network Error saat menyimpan tanda tangan: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Log.e("IsiFormAuditorFragment", "Signature upload failed: ${t.message}")
            }
        })
    }

    // Helper Methods
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
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            val file =
                File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
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

    private fun createMultipartFromFile(file: File, paramName: String): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, file.name, requestFile)
    }

    private fun checkProgress(completed: Int, failed: Int, total: Int) {
        if (completed == total) {
            // All items processed
            auditDataCompleted = true

            // Only update UI if the fragment is still attached
            if (isAdded && _binding != null) {
                if (failed > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Selesai dengan $failed item gagal disimpan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Check if we can proceed to home screen
            checkAllOperationsComplete()
        }
    }


    private fun checkAllOperationsComplete() {
        if (signatureCompleted && auditDataCompleted) {
            // Both operations completed, we can now navigate to home
            if (isAdded && _binding != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Semua data berhasil disimpan", Toast.LENGTH_SHORT)
                    .show()
            }

            // Clear local changes
            localAuditAnswerChanges.clear()
            localPhotos.clear()
            auditorSignatureFile = null
            auditeeSignatureFile = null
            facilitatorSignatureFile = null

            // Navigate to the home fragment
            navToHomeAuditor()
        }
    }

    private fun navToHomeAuditor() {
        // Make sure we're still attached to the activity
        if (!isAdded) return

        val fragment = HomeAuditorFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Interface Implementations
    override fun onRefresh() {
        getDetailAuditAnswer(auditAnswerId)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onTakePictureClick(position: Int) {
        selectedPosition = position
        val viewHolder = binding.recyclerViewForm.findViewHolderForAdapterPosition(position)
                as? ListFormAuditorAdapter.ViewHolder
        // Check for camera permission

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