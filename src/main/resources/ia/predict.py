#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de prédiction IA pour les dossiers de recouvrement
Utilise les modèles de classification et de regression pour prédire l'état final et le score de risque
"""

import sys
import json
import os
import joblib
import pandas as pd

# Obtenir le répertoire du script (pour charger les modèles depuis le même dossier)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# Chemins des fichiers de modèles (relatifs au script)
MODEL_CLASS_PATH = os.path.join(SCRIPT_DIR, 'model_classification.pkl')
MODEL_REG_PATH = os.path.join(SCRIPT_DIR, 'model_regression.pkl')
FEATURE_COLS_PATH = os.path.join(SCRIPT_DIR, 'feature_columns.pkl')

# Charger les modèles et les noms de features
model_class = joblib.load(MODEL_CLASS_PATH)
model_reg = joblib.load(MODEL_REG_PATH)
feature_cols = joblib.load(FEATURE_COLS_PATH)

# Lire les données depuis le fichier JSON
input_file = sys.argv[1]
with open(input_file, 'r') as f:
    data = json.load(f)

# Préparer le DataFrame
df = pd.DataFrame([data])
df = df.reindex(columns=feature_cols, fill_value=0.0)

# Prédire l'état final
pred_class = model_class.predict(df)[0]
STATE_MAP = {0: "RECOVERED_TOTAL", 1: "RECOVERED_PARTIAL", 2: "NOT_RECOVERED"}
etat_final = STATE_MAP[int(pred_class)]

# Prédire le score de risque
risk_score = model_reg.predict(df)[0]
risk_score = float(max(0.0, min(100.0, risk_score)))
risk_level = "Faible" if risk_score < 30 else ("Moyen" if risk_score < 70 else "Élevé")

# Retourner le résultat en JSON
result = {
    "etatFinal": etat_final,
    "riskScore": risk_score,
    "riskLevel": risk_level
}
print(json.dumps(result))