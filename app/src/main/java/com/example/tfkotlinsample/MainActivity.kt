package com.example.tfkotlinsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tfkotlinsample.tflite.Classifier
import com.example.tfkotlinsample.tflite.Classifier.create
import java.io.FileDescriptor
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    val RESULT_IMAGEFILE = 1001
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        try {
            classifier = create(this, Classifier.Device.CPU, 2)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun image(V: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_IMAGEFILE)
    }

    fun camera(V: View?) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RESULT_IMAGEFILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == RESULT_IMAGEFILE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                    var pfDescriptor: ParcelFileDescriptor? = null
                    try{
                        var uri: Uri? = resultData.data

                        if (uri != null){
                            pfDescriptor = getContentResolver().openFileDescriptor(uri!!, "r")
                            if (pfDescriptor != null) {
                                val fileDescriptor: FileDescriptor = pfDescriptor.fileDescriptor
                                val bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                                pfDescriptor.close()
                                var height = bmp.height
                                var width = bmp.width

                                while (true) {
                                    var i = 2
                                    if (width < 500 && height < 500) {
                                        break
                                    } else {
                                        if (width > 500 || height > 500) {
                                            width = width / i
                                            height = height / i
                                        } else {
                                            break
                                        }
                                        i++
                                    }
                                }

                                var croppedBitmap =
                                    Bitmap.createScaledBitmap(bmp, width, height, false)

                                imageView.setImageBitmap(croppedBitmap)

                                var results =
                                    classifier.recognizeImage(croppedBitmap, 1)
                                var text: String? = ""

                                for (result in results) {
                                    val title = result.title
                                    text += """
                                    $title
                                    
                                    """.trimIndent()
                                    Log.d("array", title)
                                }
                                textView.text = text
                            }

                        } else {
                            if (requestCode == RESULT_IMAGEFILE) {
                                val bmp: Bitmap?
                                if (resultData.extras == null) {
                                    return
                                } else {
                                    bmp = resultData.extras!!["data"] as Bitmap?
                                    if (bmp != null) {
                                        // 画像サイズを計測
                                        var height = bmp.height
                                        var width = bmp.width

                                        while (true) {
                                            var i = 2
                                            if (width < 500 && height < 500) {
                                                break
                                            } else {
                                                if (width > 500 || height > 500) {
                                                    width = width / i
                                                    height = height / i
                                                } else {
                                                    break
                                                }
                                                i++
                                            }
                                        }

                                        var croppedBitmap =
                                            Bitmap.createScaledBitmap(bmp, width, height, false)

                                        imageView.setImageBitmap(croppedBitmap)

                                        var results =
                                            classifier.recognizeImage(croppedBitmap, 1)
                                        var text: String? = ""

                                        for (result in results) {
                                            val title = result.title
                                            text += """
                                    $title
                                    
                                    """.trimIndent()
                                            Log.d("array", title)
                                        }
                                        textView.text = text
                                    }
                                }
                            }
                        }

                    } catch (e:IOException) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (pfDescriptor != null) {
                                pfDescriptor.close();
                            }
                        } catch (e:Exception) {
                            e.printStackTrace();
                        }
                    }

            }
        }
    }

}