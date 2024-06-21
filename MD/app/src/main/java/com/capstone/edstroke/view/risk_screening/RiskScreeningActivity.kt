package com.capstone.edstroke.view.risk_screening

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.edstroke.R
import com.capstone.edstroke.data.request.RiskScreeningRequest
import com.capstone.edstroke.databinding.ActivityRiskScreeningBinding
import com.capstone.edstroke.view.ViewModelFactory


class RiskScreeningActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiskScreeningBinding
    private val viewModel by viewModels<RiskViewModel> {
        ViewModelFactory(this)
    }
    private lateinit var gender: String
    private var age: Int = 0
    private var hypertension: Boolean = false
    private var heartDisease: Boolean = false
    private lateinit var everMarried: String
    private var avgGlucoseLevel: Int = 0
    private var bmi: Int = 0
    private lateinit var workType: String
    private lateinit var residenceType: String
    private lateinit var smokingStatus: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRiskScreeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        submitData()


    }

    private fun submitData() {
        val ageInput = binding.edtAge.text.toString().trim()
        val bmiInput = binding.edtBmi.text.toString().trim()
        val avgGlucoseLevelInput = binding.edtGlucose.text.toString().trim()

        if (ageInput.isNotEmpty()) {
            age = try {
                ageInput
                    .toInt() // Convert input to Double (or Int, as needed)
                // Process the number, for example:
            } catch (e: NumberFormatException) {
                0
            }
        }
        if (avgGlucoseLevelInput.isNotEmpty()) {
            avgGlucoseLevel = try {
                avgGlucoseLevelInput
                    .toInt() // Convert input to Double (or Int, as needed)
                // Process the number, for example:
            } catch (e: NumberFormatException) {
                0
            }
        }
        if (bmiInput.isNotEmpty()) {
            bmi = try {
                bmiInput
                    .toInt() // Convert input to Double (or Int, as needed)
                // Process the number, for example:
            } catch (e: NumberFormatException) {
                0
            }
        }
        binding.btnEdit.setOnClickListener {

            when {
                binding.edtAge.text.isEmpty() -> {
                    Toast.makeText(
                        this@RiskScreeningActivity,
                        "Age must be filled in",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.edtBmi.text.isEmpty() -> {
                    Toast.makeText(
                        this@RiskScreeningActivity,
                        "Body Mass Index (BMI) must be filled in",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.edtGlucose.text.isEmpty() -> {
                    Toast.makeText(
                        this@RiskScreeningActivity,
                        "Average Glucose Level must be filled in",
                        Toast.LENGTH_SHORT
                    ).show()
                }


                else -> {
                    val hypertensionValue = if (hypertension) 1 else 0
                    val heartDiseaseValue = if (heartDisease) 1 else 0

                    val riskRequest = RiskScreeningRequest(
                        gender = gender,
                        age = age,
                        hypertension = hypertensionValue,
                        heartDisease = heartDiseaseValue,
                        everMarried = everMarried,
                        avgGlucoseLevel = avgGlucoseLevel,
                        bmi = bmi,
                        workType = workType,
                        ResidenceType = residenceType,
                        smokingStatus = smokingStatus,
                    )
                    viewModel.riskScreeningResult(riskRequest)
                    viewModel.isError.observe(this) { errorMessage ->
                        if (!errorMessage.isNullOrEmpty()) {
                            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                    viewModel.riskResult.observe(this) { response ->
                        AlertDialog.Builder(this).apply {
                            setTitle("Yeah!")
                            setMessage(response.result.toString())
                            setPositiveButton(getString(R.string.next)) { _, _ ->
                                val intent = Intent(
                                    this@RiskScreeningActivity,
                                    RiskResultActivity::class.java
                                )
                                intent.putExtra(
                                    RiskResultActivity.EXTRA_RESULT_RISK,
                                    response
                                )
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }
                }

            }

        }
    }

    private fun setupAction() {


        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            genderButtonSelected(group, checkedId)
        }

        binding.rgHipertension.setOnCheckedChangeListener { group, checkedId ->
            hypertensionButtonSelected(group, checkedId)
        }
        binding.rgHeartDesease.setOnCheckedChangeListener { group, checkedId ->
            heartDiseaseButtonSelected(group, checkedId)
        }
        binding.rgMarried.setOnCheckedChangeListener { group, checkedId ->
            marriedButtonSelected(group, checkedId)
        }
        binding.rgWorkedType.setOnCheckedChangeListener { group, checkedId ->
            workTypeButtonSelected(group, checkedId)
        }
        binding.rgResidence.setOnCheckedChangeListener { group, checkedId ->
            residenceButtonSelected(group, checkedId)
        }

        binding.rgSmoker.setOnCheckedChangeListener { group, checkedId ->
            smokerButtonSelected(group, checkedId)
        }
    }

    private fun smokerButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        smokingStatus = when (selectedButton.id) {
            binding.rdActiveSmoker.id -> "smokes"
            binding.rdFormerlySmoker.id -> "formerly smoked"
            binding.rdNeverWork.id -> "never smoked"
            else -> "never smoked"
        }
    }

    private fun residenceButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        residenceType = when (selectedButton.id) {
            binding.rdUrbanResidence.id -> "Urban"
            binding.rdRuralResidence.id -> "Rural"
            else -> "Rural"
        }
    }

    private fun workTypeButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        workType = when (selectedButton.id) {
            binding.rdGovtWork.id -> "Govt_job"
            binding.rdPrivateWork.id -> "Private"
            binding.rdSelfEmployeedWork.id -> "Self-employed"
            binding.rdNeverWork.id -> "Never_worked"
            else -> "Never_worked"
        }
    }

    private fun marriedButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        everMarried = when (selectedButton.id) {
            binding.rdYesMarried.id -> "Yes"
            binding.rdNoMarried.id -> "No"
            else -> "No"
        }
    }

    private fun heartDiseaseButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        heartDisease = when (selectedButton.id) {
            binding.rdYesHeartDisease.id -> true
            binding.rdNoHeartDisease.id -> false
            else -> false
        }
    }

    private fun hypertensionButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedButton: RadioButton = findViewById(checkedId)
        hypertension = when (selectedButton.id) {
            binding.rdYesHipertension.id -> true
            binding.rdNoHipertension.id -> false
            else -> false
        }
    }

    private fun genderButtonSelected(group: RadioGroup?, checkedId: Int) {
        val selectedGender: RadioButton = findViewById(checkedId)
        gender = when (selectedGender.id) {
            binding.rdMale.id -> "Male"
            binding.rdFemale.id -> "Female"
            else -> "Male"
        }
    }
}
