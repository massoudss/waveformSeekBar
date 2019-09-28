package com.masoudss.lib.soundParser

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.masoudss.lib.exception.InvalidInputException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class SoundFile private constructor() {

    private var mProgressListener: ProgressListener? = null
    private var mInputFile: File? = null
    private var fileSizeBytes = 0
    private var avgBitrateKbps = 0
    private var sampleRate = 0
    private var channels = 0
    private var numSamples = 0
    private var mDecodedBytes: ByteBuffer? = null
    private var mDecodedSamples: ShortBuffer? = null
    private var numFrames = 0
    var frameGains: IntArray? = null
    private var mFrameLens: IntArray? = null
    private var mFrameOffsets: IntArray? = null
    private val samplesPerFrame = 1024

    private interface ProgressListener {
        fun reportProgress(fractionComplete: Double): Boolean
    }

    companion object {

        private val supportedExtensions = arrayOf("mp3", "wav", "3gpp", "3gp", "amr", "aac", "m4a", "ogg")
        private val additionalExtensions = ArrayList<String>()

        fun addCustomExtension(extension: String) = additionalExtensions.add(extension)

        fun removeCustomExtension(extension: String) = additionalExtensions.remove(extension)

        fun addCustomExtensions(extensions: List<String>) = additionalExtensions.addAll(extensions)

        fun removeCustomExtensions(extensions: List<String>) = additionalExtensions.removeAll(extensions)

        private fun isFilenameSupported(filename: String): Boolean {

            for (i in supportedExtensions.indices) {
                if (filename.endsWith("." + supportedExtensions[i])) {
                    return true
                }
            }
            for (i in additionalExtensions.indices) {
                if (filename.endsWith("." + additionalExtensions[i])) {
                    return true
                }
            }
            return false
        }

        @Throws(FileNotFoundException::class, IOException::class, InvalidInputException::class)
        fun create(fileName: String, ignoreExtension: Boolean = false): SoundFile? {
            if (!ignoreExtension && !isFilenameSupported(fileName))
                throw InvalidInputException("Not supported file extension.")

            val f = File(fileName)
            if (!f.exists()) {
                throw FileNotFoundException(fileName)
            }
            val soundFile = SoundFile()
            soundFile.readFile(f)
            return soundFile
        }
    }

    private fun readFile(inputFile: File) {
        val extractor = MediaExtractor()
        var format: MediaFormat? = null

        mInputFile = inputFile
        fileSizeBytes = mInputFile!!.length().toInt()
        extractor.setDataSource(mInputFile!!.path)
        val numTracks = extractor.trackCount

        var i = 0
        while (i < numTracks) {
            format = extractor.getTrackFormat(i)
            if (format!!.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                extractor.selectTrack(i)
                break
            }
            i++
        }
        if (i == numTracks) {
            throw InvalidInputException("No audio track found in " + mInputFile!!)
        }
        channels = format!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val expectedNumSamples = (format.getLong(MediaFormat.KEY_DURATION) / 1000000f * sampleRate + 0.5f).toInt()

        val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME))
        codec.configure(format, null, null, 0)
        codec.start()

        var decodedSamplesSize = 0
        var decodedSamples: ByteArray? = null
        @Suppress("DEPRECATION")
        val inputBuffers = codec.inputBuffers
        @Suppress("DEPRECATION")
        var outputBuffers = codec.outputBuffers
        var sampleSize: Int
        val info = MediaCodec.BufferInfo()
        var presentationTime: Long
        var totSizeRead = 0
        var doneReading = false
        mDecodedBytes = ByteBuffer.allocate(1 shl 20)
        var firstSampleData = true
        while (true) {

            val inputBufferIndex = codec.dequeueInputBuffer(100)
            if (!doneReading && inputBufferIndex >= 0) {

                sampleSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    extractor.readSampleData(codec.getInputBuffer(inputBufferIndex)!!, 0)
                else
                    extractor.readSampleData(inputBuffers[inputBufferIndex], 0)

                if (firstSampleData
                    && format.getString(MediaFormat.KEY_MIME) == "audio/mp4a-latm"
                    && sampleSize == 2
                ) {
                    extractor.advance()
                    totSizeRead += sampleSize
                } else if (sampleSize < 0) {
                    codec.queueInputBuffer(inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    doneReading = true
                } else {
                    presentationTime = extractor.sampleTime
                    codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTime, 0)
                    extractor.advance()
                    totSizeRead += sampleSize
                    if (mProgressListener != null) {
                        if (!mProgressListener!!.reportProgress((totSizeRead.toFloat() / fileSizeBytes).toDouble())) {
                            // We are asked to stop reading the file. Returning immediately. The
                            // SoundFile object is invalid and should NOT be used afterward!
                            extractor.release()
                            codec.stop()
                            codec.release()
                            return
                        }
                    }
                }
                firstSampleData = false
            }

            // Get decoded stream from the decoder output buffers.
            val outputBufferIndex = codec.dequeueOutputBuffer(info, 100)
            if (outputBufferIndex >= 0 && info.size > 0) {
                if (decodedSamplesSize < info.size) {
                    decodedSamplesSize = info.size
                    decodedSamples = ByteArray(decodedSamplesSize)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    codec.getOutputBuffer(outputBufferIndex)!!.get(decodedSamples, 0, info.size)
                    codec.getOutputBuffer(outputBufferIndex)!!.clear()
                } else {
                    outputBuffers[outputBufferIndex].get(decodedSamples, 0, info.size)
                    outputBuffers[outputBufferIndex].clear()
                }
                // Check if buffer is big enough. Resize it if it's too small.
                if (mDecodedBytes!!.remaining() < info.size) {
                    // Getting a rough estimate of the total size, allocate 20% more, and
                    // make sure to allocate at least 5MB more than the initial size.
                    val position = mDecodedBytes!!.position()
                    var newSize = (position * (1.0 * fileSizeBytes / totSizeRead) * 1.2).toInt()
                    if (newSize - position < info.size + 5 * (1 shl 20)) {
                        newSize = position + info.size + 5 * (1 shl 20)
                    }
                    var newDecodedBytes: ByteBuffer? = null
                    // Try to allocate memory. If we are OOM, try to run the garbage collector.
                    var retry = 10
                    while (retry > 0) {
                        try {
                            newDecodedBytes = ByteBuffer.allocate(newSize)
                            break
                        } catch (e: OutOfMemoryError) {
                            retry--
                        }
                    }
                    if (retry == 0) {
                        break
                    }
                    mDecodedBytes!!.rewind()
                    newDecodedBytes!!.put(mDecodedBytes)
                    mDecodedBytes = newDecodedBytes
                    mDecodedBytes!!.position(position)
                }

                mDecodedBytes!!.put(decodedSamples, 0, info.size)
                codec.releaseOutputBuffer(outputBufferIndex, false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                    @Suppress("DEPRECATION")
                    outputBuffers = codec.outputBuffers
            }

            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 || mDecodedBytes!!.position() / (2 * channels) >= expectedNumSamples) {
                break
            }
        }
        numSamples = mDecodedBytes!!.position() / (channels * 2)  // One sample = 2 bytes.
        mDecodedBytes!!.rewind()
        mDecodedBytes!!.order(ByteOrder.LITTLE_ENDIAN)
        mDecodedSamples = mDecodedBytes!!.asShortBuffer()
        avgBitrateKbps = (fileSizeBytes * 8 * (sampleRate.toFloat() / numSamples) / 1000).toInt()

        extractor.release()
        codec.stop()
        codec.release()

        numFrames = numSamples / samplesPerFrame
        if (numSamples % samplesPerFrame != 0)
            numFrames++
        frameGains = IntArray(numFrames)
        mFrameLens = IntArray(numFrames)
        mFrameOffsets = IntArray(numFrames)
        var j: Int
        var gain: Int
        var value: Int
        val frameLens = (1000 * avgBitrateKbps / 8 * (samplesPerFrame.toFloat() / sampleRate)).toInt()
        i = 0

        while (i < numFrames) {
            gain = -1
            j = 0
            while (j < samplesPerFrame) {
                value = 0
                for (k in 0 until channels)
                    if (mDecodedSamples!!.remaining() > 0)
                        value += Math.abs(mDecodedSamples!!.get().toInt())
                value /= channels
                if (gain < value)
                    gain = value
                j++
            }
            frameGains!![i] = Math.sqrt(gain.toDouble()).toInt()
            mFrameLens!![i] = frameLens
            mFrameOffsets!![i] = (i.toFloat() * (1000 * avgBitrateKbps / 8).toFloat() * (samplesPerFrame.toFloat() / sampleRate)).toInt()
            i++
        }

        mDecodedSamples!!.rewind()

    }
}
