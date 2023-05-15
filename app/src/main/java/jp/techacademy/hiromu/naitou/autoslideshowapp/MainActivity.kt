package jp.techacademy.hiromu.naitou.autoslideshowapp

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import jp.techacademy.hiromu.naitou.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timer: Timer? = null

    private val PERMISSIONS_REQUEST_CODE = 100

    private var seconds = 0.0
    private var handler = Handler(Looper.getMainLooper())

    private var flag = false

    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }

        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        binding.nextButton.setOnClickListener {
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                cursor!!.moveToNext()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = try {
                    cursor.getLong(fieldIndex)
                }catch(e: Exception){
                    cursor!!.moveToFirst()
                    cursor.getLong(fieldIndex)
                }
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d("Android",imageUri.toString())
                binding.imageView.setImageURI(imageUri)
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }

        }

        binding.prevButton.setOnClickListener {
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                cursor!!.moveToPrevious()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = try {
                    cursor.getLong(fieldIndex)
                }catch(e: Exception){
                    cursor!!.moveToLast()
                    cursor.getLong(fieldIndex)
                }
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d("Android",imageUri.toString())
                binding.imageView.setImageURI(imageUri)
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        binding.ssButton.setOnClickListener {
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                flag = !flag
                if(flag == true) {
                    binding.nextButton.isClickable = false
                    binding.prevButton.isClickable = false
                    binding.ssButton.text = "停止"
                    if (timer == null) {
                        timer = Timer()
                        timer!!.schedule(object : TimerTask() {
                            override fun run() {
                                cursor!!.moveToNext()
                                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                val id = try {
                                    cursor.getLong(fieldIndex)
                                }catch(e: Exception){
                                    cursor!!.moveToFirst()
                                    cursor.getLong(fieldIndex)
                                }
                                val imageUri =
                                    ContentUris.withAppendedId(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        id
                                    )
                                Log.d("Android", imageUri.toString())
                                handler.post {
                                    binding.imageView.setImageURI(imageUri)
                                }
                            }
                        }, 2000, 2000) // 最初に始動させるまで200ミリ秒、ループの間隔を100ミリ秒 に設定
                    }
                }else{
                    binding.nextButton.isClickable = true
                    binding.prevButton.isClickable = true
                    binding.ssButton.text = "再生"
                    timer!!.cancel()
                    timer = null
                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            binding.imageView.setImageURI(imageUri)
        }
        cursor.close()
    }
}