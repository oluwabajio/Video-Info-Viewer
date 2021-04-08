package video.information.viewer

import android.app.Activity
import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.UriUtils
import video.information.viewer.databinding.FragmentHomeBinding
import video.information.viewer.utils.PermissionHelper
import video.information.viewer.utils.RealPathUtil
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {



    val PICK_VIDEO_FILE: Int  =123

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view:View = binding.root


        initListeners();
        if (!PermissionHelper.checkPermissions(activity)) {
            PermissionHelper.requestPermissions(activity)
        } else {

        }
        return view
    }

    private fun initListeners() {
      binding.btnSelectFile.setOnClickListener {
          selectFile()
      }
    }


    fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"

        }


        startActivityForResult(intent, PICK_VIDEO_FILE)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === PICK_VIDEO_FILE   && resultCode === Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            var uri: Uri? = null
            if (data != null) {
                uri = data.getData()
                val realPath = RealPathUtil.getRealPath(requireContext(), uri!!)
                // Perform operations on the document using its URI.
                Log.e(TAG, "onActivityResult: FIle path = "+ uri?.path )
                Log.e(TAG, "onActivityResult: FIle path = "+  UriUtils.uri2File(uri).absolutePath )
                Log.e(TAG, "onActivityResult: FIle path = "+  realPath )

                val bundle = Bundle().apply {
                    putString("real_path", realPath)
                }
                findNavController().navigate(R.id.action_HomeFragment_to_DetailsFragment, bundle )

            }
        }
    }



    companion object {
        private const val TAG = "HomeFragment"
    }
}