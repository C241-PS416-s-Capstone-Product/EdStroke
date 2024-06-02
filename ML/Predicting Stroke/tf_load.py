import json
import tensorflow as tf
import numpy as np

# Load the model architecture
with open("stroke_prediction_model.json", "r") as json_file:
    model_json = json_file.read()

model = tf.keras.models.model_from_json(model_json)

# Load the model weights
with open("stroke_prediction_model_weights.json", "r") as json_file:
    weights_list = json.load(json_file)

weights = [np.array(w) for w in weights_list]
model.set_weights(weights)

# Compile the model (if necessary)
model.compile(optimizer='adam',
              loss='binary_crossentropy',
              metrics=['accuracy'])

print("Model loaded from JSON files.")

# Alternatively, load the entire model from the .keras file
model = tf.keras.models.load_model('stroke_prediction_model.keras')
print("Model loaded from .keras file.")
