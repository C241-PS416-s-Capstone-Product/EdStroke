from flask import Flask, request, render_template
import numpy as np
import tensorflow as tf
from sklearn.preprocessing import StandardScaler, LabelEncoder
import joblib

app = Flask(__name__)

# Load the trained model and scaler
model = tf.keras.models.load_model('stroke_prediction_model.h5')
scaler = joblib.load('scaler.pkl')

# Create label encoders for each categorical feature
label_encoders = {
    'gender': LabelEncoder().fit(['Female', 'Male', 'Other']),
    'ever_married': LabelEncoder().fit(['No', 'Yes']),
    'work_type': LabelEncoder().fit(['children', 'Govt_job', 'Never_worked', 'Private', 'Self-employed']),
    'Residence_type': LabelEncoder().fit(['Rural', 'Urban']),
    'smoking_status': LabelEncoder().fit(['formerly smoked', 'never smoked', 'smokes', 'Unknown'])
}

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/predict', methods=['POST'])
def predict():
    if request.method == 'POST':
        # Get data from form
        gender = request.form['gender']
        age = float(request.form['age'])
        hypertension = int(request.form['hypertension'])
        heart_disease = int(request.form['heart_disease'])
        ever_married = request.form['ever_married']
        work_type = request.form['work_type']
        Residence_type = request.form['Residence_type']
        avg_glucose_level = float(request.form['avg_glucose_level'])
        bmi = float(request.form['bmi'])
        smoking_status = request.form['smoking_status']

        # Preprocess the input data
        input_data = np.array([[gender, age, hypertension, heart_disease, ever_married, work_type, Residence_type, avg_glucose_level, bmi, smoking_status]])

        # Apply label encoding and scaling
        input_data[:, 0] = label_encoders['gender'].transform(input_data[:, 0])
        input_data[:, 4] = label_encoders['ever_married'].transform(input_data[:, 4])
        input_data[:, 5] = label_encoders['work_type'].transform(input_data[:, 5])
        input_data[:, 6] = label_encoders['Residence_type'].transform(input_data[:, 6])
        input_data[:, 9] = label_encoders['smoking_status'].transform(input_data[:, 9])
        
        input_data = scaler.transform(input_data.astype(float))

        # Make prediction
        probability = model.predict(input_data)[0][0]
        prediction = 'Stroke' if probability > 0.5 else 'No Stroke'
        probability_percentage = probability * 100

        # Return result
        result = f'{prediction} (Probability: {probability_percentage:.2f}%)'

        return render_template('result.html', result=result)

if __name__ == '__main__':
    app.run(debug=True)
