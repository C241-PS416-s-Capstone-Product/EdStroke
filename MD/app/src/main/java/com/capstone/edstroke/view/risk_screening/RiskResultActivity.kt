package com.capstone.edstroke.view.risk_screening

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.edstroke.R
import com.capstone.edstroke.data.response.PredictResponse
import com.capstone.edstroke.databinding.ActivityRiskResultBinding
import com.capstone.edstroke.view.main.MainActivity
import java.text.DecimalFormat
import kotlin.text.*

class RiskResultActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRiskResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val storyItem = intent.getParcelableExtra<PredictResponse>(EXTRA_RESULT_RISK)

        storyItem?.let { item ->
            detailView(item)
        }

        binding.btnBackDashboard.setOnClickListener {
            val intent = Intent(this@RiskResultActivity, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
//
//        setSupportActionBar(toolbar)
//        assert(supportActionBar != null)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    private fun detailView(item: PredictResponse) {
        val decimalFormat = DecimalFormat("#.##")
        val formattedValue = decimalFormat.format(item.probability)
        binding.tvRiskResult.text = formattedValue
    }

    companion object {
        const val EXTRA_RESULT_RISK = "EXTRA_RESULT_RISK"
    }
}