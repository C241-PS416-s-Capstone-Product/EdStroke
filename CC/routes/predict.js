import express from 'express';
import * as tf from '@tensorflow/tfjs-node';

const router = express.Router();

let model;

// Load the model
async function loadModel() {
    try {
        const modelUrl = 'https://storage.googleapis.com/edstroke_bucket/Risk%20Assasment%201/model3.json';
        model = await tf.loadLayersModel(modelUrl);
        console.log('Model loaded successfully');
    } catch (error) {
        console.error('Error loading model:', error);
    }
}

loadModel();

// Label Encoders
const labelEncoders = {
    'gender': ['Female', 'Male', 'Other'],
    'ever_married': ['No', 'Yes'],
    'work_type': ['children', 'Govt_job', 'Never_worked', 'Private', 'Self-employed'],
    'Residence_type': ['Rural', 'Urban'],
    'smoking_status': ['formerly smoked', 'never smoked', 'smokes', 'Unknown']
};

function encodeLabels(input, encoders) {
    return input.map((value, index) => {
        const encoder = encoders[index];
        if (encoder) {
            return encoder.indexOf(value);
        }
        return value;
    });
}

// Prediction route
router.post('/', async (req, res) => {
    if (!model) {
        return res.status(500).json({ error: 'Model not loaded yet. Please try again later.' });
    }

    const { gender, age, hypertension, heart_disease, ever_married, work_type, Residence_type, avg_glucose_level, bmi, smoking_status } = req.body;

    // Preprocess the input data
    let inputData = [[gender, parseFloat(age), parseInt(hypertension), parseInt(heart_disease), ever_married, work_type, Residence_type, parseFloat(avg_glucose_level), parseFloat(bmi), smoking_status]];

    console.log('Original Input Data:', inputData);

    // Encode categorical data
    inputData = inputData.map(row => encodeLabels(row, [
        labelEncoders['gender'],
        null,  // age is not encoded
        null,  // hypertension is not encoded
        null,  // heart_disease is not encoded
        labelEncoders['ever_married'],
        labelEncoders['work_type'],
        labelEncoders['Residence_type'],
        null,  // avg_glucose_level is not encoded
        null,  // bmi is not encoded
        labelEncoders['smoking_status']
    ]));

    console.log('Encoded Input Data:', inputData);

    // Convert input data to tensor and make prediction
    const inputTensor = tf.tensor2d(inputData);
    console.log('Input Tensor:', inputTensor.arraySync());

    const prediction = model.predict(inputTensor);
    const probability = prediction.dataSync()[0];
    console.log('Prediction:', prediction.arraySync());

    const result = probability > 0.5 ? 'Stroke' : 'No Stroke';

    res.json({
        result: result,
        probability: probability * 100
    });
});

export default router;
