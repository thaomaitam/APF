package com.KTA.APF.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.KTA.APF.R
import com.KTA.APF.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Sử dụng ViewBinding để truy cập các view một cách an toàn
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar làm ActionBar
        setSupportActionBar(binding.toolbar)

        // Tìm NavHostFragment từ layout
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        
        // Lấy NavController từ NavHostFragment
        navController = navHostFragment.navController

        // Cấu hình AppBar để tự động hiển thị tiêu đề và nút Back
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút Back trên ActionBar.
     * NavController sẽ tự động điều hướng trở lại Fragment trước đó.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}