package com.example.dorixona.Type

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.dorixona.R
import com.example.dorixona.databinding.FragmentPersonalBinding
import com.example.dorixona.model.User
import com.example.dorixona.util.ShPHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var img: ImageView
    lateinit var user: User

    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = FragmentPersonalBinding.inflate(inflater, container, false)
        user = arguments?.getSerializable("user") as User
        var userList = ShPHelper.getInstance(requireContext()).getUser()
        img = binding.imageView7
        for (i in userList) {
            if (i == user) {
                if (i.url != null) {
                    img.setImageURI(Uri.parse(i.url))
                }
                else{
                    img.setImageResource(R.drawable.user)
                }
            }
        }
        binding.textView12.text = user.name + " " + user.surname
        binding.textView13.text = user.email
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.imageView7.setOnClickListener {
            binding.camera.visibility = View.VISIBLE
            binding.file.visibility = View.VISIBLE
        }

        binding.camera.setOnClickListener { dispatchTakePictureIntent() }
        binding.file.setOnClickListener { takePhotoResult.launch("image/*") }
        return binding.root
    }

    val takePhotoResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        img.setImageURI(uri)
        val openInputStream = requireActivity().contentResolver?.openInputStream(uri)
        val file = File(requireActivity().filesDir, "${System.currentTimeMillis()}+.jpg")
        val fileOutputStream = FileOutputStream(file)
        ShPHelper.getInstance(requireContext()).setUserImage(user, url = uri.toString())
        openInputStream?.copyTo(fileOutputStream)
        openInputStream?.close()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "uz.itteacher.lessoncameraandgallery",
                it
            )
            takePhotoResultCamera.launch(photoURI)

        }
    }

    val takePhotoResultCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            img.setImageURI(Uri.fromFile(File(currentPhotoPath)))
            ShPHelper.getInstance(requireContext())
                .setUserImage(user, url = Uri.fromFile(File(currentPhotoPath)).toString())
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}