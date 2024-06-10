import express from 'express';
import * as tf from '@tensorflow/tfjs-node';
import fetch from 'node-fetch';
import db from '../db.js';
import { auth } from '../middleware.js';

const router = express.Router();

let model;
let scalerParams;

// Load the model
async function loadModel() {
    try {
        const modelUrl = 'https://storage.googleapis.com/edstroke_bucket/Risk%20Assasment%204/model.json';
        model = await tf.loadLayersModel(modelUrl);
        console.log('Model loaded successfully');
    } catch (error) {
        console.error('Error loading model:', error);
    }
}

// Load scaler parameters
async function loadScalerParams() {
    try {
        const scalerUrl = 'https://storage.googleapis.com/edstroke_bucket/Risk%20Assasment%204/scaler_params.json';
        const response = await fetch(scalerUrl);
        scalerParams = await response.json();
        console.log('Scaler parameters loaded successfully');
    } catch (error) {
        console.error('Error loading scaler parameters:', error);
    }
}

async function loadDependencies() {
    await loadModel();
    await loadScalerParams();
}

loadDependencies();

const labelEncoders = {
    'gender': ['Female', 'Male', 'Other'],
    'ever_married': ['No', 'Yes'],
    'work_type': ['children', 'Govt_job', 'Never_worked', 'Private', 'Self-employed'],
    'Residence_type': ['Rural', 'Urban'],
    'smoking_status': ['formerly smoked', 'never smoked', 'smokes', 'Unknown']
};

// Encode categorical data
function encodeLabels(input, encoders) {
    return input.map((value, index) => {
        const encoder = encoders[index];
        if (encoder) {
            return encoder.indexOf(value);
        }
        return value;
    });
}

// Scale input data using scaler parameters
function scaleInput(input) {
    const mean = tf.tensor(scalerParams.mean);
    const scale = tf.tensor(scalerParams.scale);

    const scaledInput = input.sub(mean).div(scale);
    return scaledInput;
}

// Prediction route with auth middleware
router.post('/', auth, async (req, res) => {
    if (!model || !scalerParams) {
        return res.status(500).json({ error: 'Model or scaler parameters not loaded yet. Please try again later.' });
    }

    const { gender, age, hypertension, heart_disease, ever_married, work_type, Residence_type, avg_glucose_level, bmi, smoking_status } = req.body;

    // Preprocess the input data
    let inputData = [[gender, parseFloat(age), parseInt(hypertension), parseInt(heart_disease), ever_married, work_type, Residence_type, parseFloat(avg_glucose_level), parseFloat(bmi), smoking_status]];

    console.log('Original Input Data:', inputData);

    inputData = inputData.map(row => encodeLabels(row, [
        labelEncoders['gender'],
        null,
        null,
        null,
        labelEncoders['ever_married'],
        labelEncoders['work_type'],
        labelEncoders['Residence_type'],
        null,
        null,
        labelEncoders['smoking_status']
    ]));

    console.log('Encoded Input Data:', inputData);

    // Convert input data to tensor
    let inputTensor = tf.tensor2d(inputData);
    console.log('Input Tensor:', inputTensor.arraySync());

    // Scale input data
    inputTensor = scaleInput(inputTensor);

    // Make prediction
    const prediction = model.predict(inputTensor);
    const probability = prediction.dataSync()[0];
    console.log('Prediction:', probability);

    const result = probability > 0.5 ? 'Stroke' : 'No Stroke';

    // Insert probability into the database
    try {
        db.query(
            'INSERT INTO risk_assessment (user_id, probability) VALUES (?, ?)',
            [req.user.id, probability],
            (err, results) => {
                if (err) {
                    console.error('Error inserting data:', err);
                    return res.status(500).json({ error: 'Error inserting data into the database' });
                }
                console.log('Data inserted successfully:', results);
                res.json({
                    result: result,
                    probability: probability * 100
                });
            }
        );
    } catch (error) {
        console.error('Error:', error);
        return res.status(500).json({ error: 'An unexpected error occurred' });
    }
});

export default router;
