package video.information.viewer


import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ToastUtils
import video.information.viewer.databinding.FragmentDetailsBinding
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DetailsFragment : Fragment() {

    private val videoExtractor = MediaExtractor()
    private val audioExtractor = MediaExtractor()
    var videoTrackIdx: Int = -1;
    var audioTrackIdx: Int = -1;
    lateinit var binding: FragmentDetailsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        val realPath = arguments?.getString("real_path").toString()
        initProcess(realPath)
        initMediaMetaRetrieval(realPath)

        return view
    }

    private fun initMediaMetaRetrieval(realPath: String) {
        val file = File(realPath)

        if (!file.exists()) {
            return
        }
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(realPath)


        val FileName = getName(realPath)
        //val Format = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

        val FileSize = java.lang.Long.toString(file.length())
        val MimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        val Format = getFormat(MimeType)
        val Duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val BitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        val Date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
        val Width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val Height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        try {
            val VideoWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
        } catch (e: NumberFormatException) {
        }
        try {
            val VideoHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                    .toInt()
        } catch (e: NumberFormatException) {
        }

        val bitmap = retriever.getFrameAtTime(Duration!!.toLong() / 2)

        val currentWidth = bitmap!!.width.toDouble()
        val currentHeight = bitmap!!.height.toDouble()

        var thumbWidth = currentWidth
        var thumbHeight = currentHeight

        val maxDimension = 640.0
        val widthGreater = bitmap!!.width > bitmap!!.height
        if (widthGreater && currentWidth > maxDimension) {
            val ratio = maxDimension / currentWidth
            thumbWidth = currentWidth * ratio
            thumbHeight = currentHeight * ratio
        } else if (!widthGreater && currentHeight > maxDimension) {
            val ratio = maxDimension / currentHeight
            thumbWidth = currentWidth * ratio
            thumbHeight = currentHeight * ratio
        }

//        try {
//            val out = FileOutputStream(ThumbnailFilePath())
//            val smaller = Bitmap.createScaledBitmap(
//                bitmap!!,
//                thumbWidth.toInt(), thumbHeight.toInt(), true
//            )
//            bitmap!!.recycle()
//            smaller.compress(Bitmap.CompressFormat.PNG, 90, out)
//            out.close()
//            smaller.recycle()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }


        binding.tvName.text = FileName
        binding.tvBitRate.text = BitRate+"bits/sec"
        binding.tvFileFormat.text = Format
        binding.tvDuration.text = getFormattedTime(Duration)
        binding.tvFileSize.text = getSizeInMb(FileSize)
        binding.tvTaggedDate.text = Date
        binding.tvMimeType.text = MimeType
        val gcd = gcd(Width!!.toInt(), Height!!.toInt())
        binding.tvAspectRatio.text =   getAspectRatio(Width!!.toFloat(), Height!!.toFloat())
        Log.e(TAG, "initMediaMetaRetrieval: Mime type = " + MimeType)
        Log.e(TAG, "initMediaMetaRetrieval: " + retriever)
        Log.e(TAG, "initMediaMetaRetrieval: gcd = " + gcd(Width?.toInt()!!, Height?.toInt()!!))

        for (i in 0..35) {
            Log.e(TAG, "initMediaMetaRetrieval: " + retriever.extractMetadata(i))
        }


    }

    private fun getSizeInMb(fileSize: String): String? {
        return (fileSize.toLong() / (1024 * 1024)).toString()

    }
    fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }

    private fun getFormattedTime(duration: String): String? {
        return String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration.toLong()),
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    duration.toLong()
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    duration.toLong()
                )
            )
        );


    }

    private fun getFormat(mimeType: String?): String? {
        if (mimeType?.contains("/")!!) {
            val strs = mimeType.split("/").toTypedArray()
            return strs[0]
        } else {
            return mimeType
        }

    }


    private fun getName(realPath: String): String {
        val directory = realPath.substringBeforeLast("/")
        val fullName = realPath.substringAfterLast("/")
        val fileName = fullName.substringBeforeLast(".")
        val extension = fullName.substringAfterLast(".")
        return fileName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    fun initProcess(inputFilePath: String) {
        videoExtractor.setDataSource(inputFilePath)
        videoTrackIdx = getVideoTrackIdx(videoExtractor)
        if (videoTrackIdx == -1) {
            Log.e(TAG, "initProcess: video not found")
            ToastUtils.showLong("No Video file found")
        }
        videoExtractor.selectTrack(videoTrackIdx)



        audioExtractor.setDataSource(inputFilePath)
        audioTrackIdx = getAudioTrackIdx(audioExtractor)
        if (audioTrackIdx == -1) {
            Log.e(TAG, "initProcess: " + "audio not found")
            throw RuntimeException("audio not found")
        }
        audioExtractor.selectTrack(audioTrackIdx)


    }

    private fun getAspectRatio(width: Float, height: Float) : String {
        val numbers = FloatArray(9)
        numbers[0] = 9.toFloat()/16.toFloat()
        numbers[1] = 1.toFloat()
        numbers[2] = 6.toFloat()/5.toFloat()
        numbers[3] = 5.toFloat()/4.toFloat()
        numbers[4] = 4.toFloat()/3.toFloat()
        numbers[5] = 11.toFloat()/8.toFloat()
        numbers[6] = 1.43.toFloat()
        numbers[7] = 3.toFloat()/2.toFloat()
        numbers[8] = 14.toFloat()/9.toFloat()
      //  numbers[9] = 16.toFloat()/10.toFloat()



        val myNumber= width/height

        Log.e(TAG, "getAspectRatio: width = "+width )
        Log.e(TAG, "getAspectRatio: height = "+height )
        Log.e(TAG, "getAspectRatio: myNumber = "+myNumber )
        var distance: Float = Math.abs(numbers.get(0) - myNumber)
        var idx = 0
        for (c in 1 until numbers.size) {
            val cdistance: Float = Math.abs(numbers.get(c) - myNumber)
            if (cdistance < distance) {
                idx = c
                distance = cdistance
            }
        }
        val theNumber: Float = numbers.get(idx)
        Log.e(TAG, "getAspectRatio: position = "+ idx )

        return formatAspectRatio(idx)
    }

    private fun formatAspectRatio(idx: Int): String {

        when (idx) {
            0 -> return "16 : 9"
            1 -> return "1 : 1"
            2 -> return "6 : 5"
            3 -> return "5 : 4"
            4 -> return "4 : 3"
            5 -> return "11 : 8"
            6 -> return "1.43 : 1"
            7 -> return "3 : 2"
            8 ->return "14 : 9"
            else ->{ return ""}
        }

    }

    private fun getAudioTrackIdx(extractor: MediaExtractor): Int {
        for (idx in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(idx)
            val mime = format.getString(MediaFormat.KEY_MIME)
            Log.e(TAG, "getAudioTrackIdx: " + "inputAudioFormat:" + format)
            Log.e(TAG, "getAudioTrackIdx: " + format.getLong(MediaFormat.KEY_DURATION))
            Log.e(
                TAG, "getAudioTrackIdx: ${
                    TimeUnit.MILLISECONDS.toMinutes(
                        format.getLong(MediaFormat.KEY_DURATION)!!.toLong()
                    ).toString()
                }"
            )
            if (mime?.startsWith("audio") == true) {
                return idx
            }
        }
        return -1
    }

    private fun getVideoTrackIdx(extractor: MediaExtractor): Int {
        for (idx in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(idx)
            val mime = format.getString(MediaFormat.KEY_MIME)
            Log.e(TAG, "initProcess: " + "inputVideoFormat: $format")
            populateViews(format)
            if (mime?.startsWith("video") == true) {
                return idx
            }
        }
        return -1
    }

    private fun populateViews(format: MediaFormat) {


    }

    companion object {
        private const val TAG = "DetailsFragment"
    }
}