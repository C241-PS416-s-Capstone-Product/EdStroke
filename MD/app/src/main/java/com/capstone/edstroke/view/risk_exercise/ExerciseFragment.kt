package com.capstone.edstroke.view.risk_exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.capstone.edstroke.R

class ExerciseFragment : Fragment(), View.OnClickListener {

    private var selectedExercise: String? = null
    private lateinit var card1: CardView
    private lateinit var card2: CardView
    private lateinit var nextButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        card1 = view.findViewById(R.id.card1)
        card2 = view.findViewById(R.id.card2)
        nextButton = view.findViewById(R.id.nextButton)

        card1.setOnClickListener {
            selectExercise("Shoulder Range of Motion")
            applySelectionEffect(card1)
            removeSelectionEffect(card2)
        }

        card2.setOnClickListener {
            selectExercise("Mini-Lunge")
            applySelectionEffect(card2)
            removeSelectionEffect(card1)
        }

        nextButton.setOnClickListener(this)
    }

    private fun selectExercise(exercise: String) {
        selectedExercise = exercise
    }

    private fun applySelectionEffect(cardView: CardView) {
        cardView.cardElevation = 16f
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_blue))
    }

    private fun removeSelectionEffect(cardView: CardView) {
        cardView.cardElevation = 8f
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
    }

    override fun onClick(v: View) {
        selectedExercise?.let {
            val bundle = Bundle().apply {
                putString("selectedExercise", it)
            }
            (activity as? RehabExerciseActivity)?.loadExercisePrepareFragment(bundle)
        }
    }
}
