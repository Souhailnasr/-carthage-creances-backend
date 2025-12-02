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

# Obtenir le répertoire du script
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# Chemins des fichiers de modèles (relatifs au script)
MODEL_CLASS_PATH = os.path.join(SCRIPT_DIR, 'model_classification.pkl')
MODEL_REG_PATH = os.path.join(SCRIPT_DIR, 'model_regression.pkl')
FEATURE_COLS_PATH = os.path.join(SCRIPT_DIR, 'feature_columns.pkl')

def main():
    try:
        # Vérifier que le fichier d'entrée est fourni
        if len(sys.argv) < 2:
            print(json.dumps({
                "error": "Fichier d'entrée manquant",
                "etatFinal": "NOT_RECOVERED",
                "riskScore": 100.0,
                "riskLevel": "Élevé"
            }))
            sys.exit(1)
        
        input_file = sys.argv[1]
        
        # Vérifier que les fichiers de modèles existent
        if not os.path.exists(MODEL_CLASS_PATH):
            print(json.dumps({
                "error": f"Modèle de classification non trouvé: {MODEL_CLASS_PATH}",
                "etatFinal": "NOT_RECOVERED",
                "riskScore": 100.0,
                "riskLevel": "Élevé"
            }))
            sys.exit(1)
        
        if not os.path.exists(MODEL_REG_PATH):
            print(json.dumps({
                "error": f"Modèle de régression non trouvé: {MODEL_REG_PATH}",
                "etatFinal": "NOT_RECOVERED",
                "riskScore": 100.0,
                "riskLevel": "Élevé"
            }))
            sys.exit(1)
        
        if not os.path.exists(FEATURE_COLS_PATH):
            print(json.dumps({
                "error": f"Fichier feature_columns non trouvé: {FEATURE_COLS_PATH}",
                "etatFinal": "NOT_RECOVERED",
                "riskScore": 100.0,
                "riskLevel": "Élevé"
            }))
            sys.exit(1)
        
        # Charger les modèles
        model_class = joblib.load(MODEL_CLASS_PATH)
        model_reg = joblib.load(MODEL_REG_PATH)
        feature_cols = joblib.load(FEATURE_COLS_PATH)
        
        # Lire les données réelles depuis le fichier JSON
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Préparer les données pour la prédiction
        df = pd.DataFrame([data])
        
        # Réindexer avec les colonnes attendues par le modèle (remplir avec 0.0 si manquant)
        df = df.reindex(columns=feature_cols, fill_value=0.0)
        
        # Vérifier que toutes les colonnes sont présentes
        missing_cols = set(feature_cols) - set(df.columns)
        if missing_cols:
            # Ajouter les colonnes manquantes avec des valeurs par défaut
            for col in missing_cols:
                df[col] = 0.0
        
        # Réorganiser les colonnes dans l'ordre attendu
        df = df[feature_cols]
        
        # Prédire
        pred_class = model_class.predict(df)[0]
        pred_score = model_reg.predict(df)[0]
        
        # Déterminer le niveau de risque
        if pred_score < 30:
            risk_level = "Faible"
        elif pred_score < 70:
            risk_level = "Moyen"
        else:
            risk_level = "Élevé"
        
        # Mapper la classe prédite à l'état final
        STATE_MAP = {
            0: "RECOVERED_TOTAL",
            1: "RECOVERED_PARTIAL",
            2: "NOT_RECOVERED"
        }
        
        etat_final = STATE_MAP.get(int(pred_class), "NOT_RECOVERED")
        
        # Retourner le résultat en JSON
        result = {
            "etatFinal": etat_final,
            "riskScore": float(pred_score),
            "riskLevel": risk_level
        }
        
        print(json.dumps(result, ensure_ascii=False))
        
    except FileNotFoundError as e:
        print(json.dumps({
            "error": f"Fichier non trouvé: {str(e)}",
            "etatFinal": "NOT_RECOVERED",
            "riskScore": 100.0,
            "riskLevel": "Élevé"
        }))
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(json.dumps({
            "error": f"Erreur de parsing JSON: {str(e)}",
            "etatFinal": "NOT_RECOVERED",
            "riskScore": 100.0,
            "riskLevel": "Élevé"
        }))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({
            "error": f"Erreur lors de la prédiction: {str(e)}",
            "etatFinal": "NOT_RECOVERED",
            "riskScore": 100.0,
            "riskLevel": "Élevé"
        }))
        sys.exit(1)

if __name__ == "__main__":
    main()

