import pandas as pd
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from imblearn.over_sampling import SMOTE
import joblib
import json

# Load the dataset
data = pd.read_csv('healthcare-dataset-stroke-data.csv')

# Drop rows with missing values
data.dropna(inplace=True)

# Convert categorical columns to numeric
label_encoders = {}
for column in ['gender', 'ever_married', 'work_type', 'Residence_type', 'smoking_status']:
    label_encoders[column] = LabelEncoder()
    data[column] = label_encoders[column].fit_transform(data[column])

# Separate features and target
X = data.drop(columns=['id', 'stroke'])
y = data['stroke']

# Apply SMOTE to balance the dataset
smote = SMOTE(random_state=42)
X_resampled, y_resampled = smote.fit_resample(X, y)

# Split the data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X_resampled, y_resampled, test_size=0.2, random_state=42)

# Standardize the feature data
scaler = StandardScaler()
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

# Save the scaler
joblib.dump(scaler, 'scaler.pkl')

# Define the neural network architecture
model = tf.keras.Sequential([
    tf.keras.layers.InputLayer(input_shape=(X_train.shape[1],)),
    tf.keras.layers.Dense(128, activation='relu'),
    tf.keras.layers.Dropout(0.5),
    tf.keras.layers.Dense(64, activation='relu'),
    tf.keras.layers.Dropout(0.5),
    tf.keras.layers.Dense(32, activation='relu'),
    tf.keras.layers.Dropout(0.5),
    tf.keras.layers.Dense(1, activation='sigmoid')
])

# Compile the model
model.compile(optimizer='adam',
              loss='binary_crossentropy',
              metrics=['accuracy'])

# Train the model
history = model.fit(X_train, y_train, epochs=200, batch_size=32, validation_split=0.2)

# Save the trained model
model.save('stroke_prediction_model.h5')

# Save the model architecture in JSON format
model_json = model.to_json()
with open("stroke_prediction_model.json", "w") as json_file:
    json_file.write(model_json)

# Save the model weights
weights = model.get_weights()
weights_list = [w.tolist() for w in weights]
with open("stroke_prediction_model_weights.json", "w") as json_file:
    json.dump(weights_list, json_file)

# Save the entire model to a binary file
model.save('stroke_prediction_model.bin')

print("Model architecture, weights, and binary file have been saved.")
