package com.capstone.edstroke.view.advice

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edstroke.R
import com.capstone.edstroke.databinding.ActivityAdviceBinding
import com.capstone.edstroke.view.ViewModelFactory

class AdviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdviceBinding

    private val adviceViewModel by viewModels<AdviceViewModel> {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityAdviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupAction()
    }

    private fun setupRecyclerView() {
        binding.adviceRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAction() {
        adviceViewModel.adviceResult()
        adviceViewModel.adviceResult.observe(this) { adviceList ->
            binding.adviceRecyclerView.adapter = AdviceAdapter(adviceList)
        }
    }
}
