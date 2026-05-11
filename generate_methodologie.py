import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import numpy as np

# Créer une figure avec fond blanc
fig, ax = plt.subplots(1, 1, figsize=(16, 9), dpi=120, facecolor='white')
ax.set_xlim(0, 10)
ax.set_ylim(0, 10)
ax.axis('off')

# Titre principal
ax.text(5, 9.5, 'MÉTHODOLOGIE ADOPTÉE', 
        fontsize=48, fontweight='bold', ha='center', va='top', color='#2C3E50')
ax.text(5, 9.0, 'Approche Agile / Scrum', 
        fontsize=28, ha='center', va='top', color='#34495E', style='italic')

# Ligne séparatrice
ax.plot([0.5, 9.5], [8.7, 8.7], color='#3498DB', linewidth=3)

# Couleurs pour les phases
colors = ['#2E7D87', '#5DADE2', '#F39C12', '#27AE60', '#9B59B6']
phase_names = ['PLANIFICATION\n& ANALYSE', 'CONCEPTION\nDÉTAILLÉE', 'DÉVELOPPEMENT', 'TESTS &\nVALIDATION', 'DEPLOYMENT']
icons = ['📋', '🏗️', '💻', '✅', '🚀']

# Activités pour chaque phase
activities = [
    ['• Recueil des besoins', '• Analyse métier', '• Définition specs'],
    ['• Design BDD', '• Architecture système', '• Maquettes UI'],
    ['• Dev Frontend Angular', '• Dev Backend Spring', '• Intégration API'],
    ['• Tests unitaires', '• Tests intégration', '• Validation UAT'],
    ['• Préparation prod', '• Documentation', '• Soutenance']
]

# Dessiner les 5 phases
y_start = 7.2
box_height = 2.2
box_width = 1.6
x_spacing = 1.9

for i in range(5):
    x = 0.8 + i * x_spacing
    
    # Boîte de phase
    box = FancyBboxPatch((x, y_start - box_height), box_width, box_height,
                          boxstyle="round,pad=0.1", 
                          edgecolor=colors[i], facecolor=colors[i], 
                          linewidth=2.5, alpha=0.9)
    ax.add_patch(box)
    
    # Icône
    ax.text(x + box_width/2, y_start - 0.35, icons[i], 
            fontsize=40, ha='center', va='center')
    
    # Nom de la phase
    ax.text(x + box_width/2, y_start - 0.8, phase_names[i], 
            fontsize=13, fontweight='bold', ha='center', va='center', 
            color='white', wrap=True)
    
    # Flèches entre phases
    if i < 4:
        arrow = FancyArrowPatch((x + box_width + 0.05, y_start - box_height/2),
                               (x + x_spacing - 0.05, y_start - box_height/2),
                               arrowstyle='->', mutation_scale=25, 
                               linewidth=2.5, color='#34495E', alpha=0.6)
        ax.add_patch(arrow)

# Section des activités détaillées (ci-dessous les phases)
y_activities = 4.5
ax.text(5, y_activities + 0.3, 'ACTIVITÉS CLÉS PAR PHASE', 
        fontsize=20, fontweight='bold', ha='center', color='#2C3E50')

y_activity_start = y_activities - 0.5
for i in range(5):
    x = 0.8 + i * x_spacing
    y = y_activity_start
    
    # Petit arc de couleur
    arc = FancyBboxPatch((x, y - 1.5), box_width, 1.5,
                         boxstyle="round,pad=0.05", 
                         edgecolor=colors[i], facecolor=colors[i], 
                         linewidth=1.5, alpha=0.15)
    ax.add_patch(arc)
    
    # Texte des activités
    for j, activity in enumerate(activities[i]):
        ax.text(x + 0.8, y - 0.25 - j*0.35, activity, 
                fontsize=9, ha='center', va='top', color='#2C3E50')

# Section inférieure - Caractéristiques de la méthodologie
y_bottom = 1.8
ax.text(5, y_bottom + 0.2, 'CARACTÉRISTIQUES DE NOTRE APPROCHE', 
        fontsize=18, fontweight='bold', ha='center', color='#2C3E50')

# 4 caractéristiques principales
features = [
    ['🔄', 'Itérations\nCourtes', '(1-2 semaines)'],
    ['👥', 'Feedback\nContinu', 'Parties prenantes'],
    ['🛠️', 'Outils\nModernes', 'Git, Jira, Angular'],
    ['✨', 'Qualité\nConstante', 'Tests & Validation']
]

feature_colors = ['#3498DB', '#E74C3C', '#9B59B6', '#27AE60']
x_feature_start = 1.2
feature_spacing = 2.0

for i, (feature) in enumerate(features):
    x = x_feature_start + i * feature_spacing
    
    # Cercle de fond
    circle = mpatches.Circle((x + 0.4, y_bottom - 0.7), 0.4, 
                            color=feature_colors[i], alpha=0.2, zorder=1)
    ax.add_patch(circle)
    
    # Icône
    ax.text(x + 0.4, y_bottom - 0.7, feature[0], 
            fontsize=28, ha='center', va='center', zorder=2)
    
    # Titre
    ax.text(x + 0.4, y_bottom - 1.3, feature[1], 
            fontsize=11, fontweight='bold', ha='center', va='top', color='#2C3E50')
    
    # Sous-titre
    ax.text(x + 0.4, y_bottom - 1.85, feature[2], 
            fontsize=9, ha='center', va='top', color='#7F8C8D', style='italic')

# Pied de page
ax.text(5, 0.15, '© ABC Banque Tunisie - Gestion Système TPE 2026', 
        fontsize=10, ha='center', va='bottom', color='#95A5A6')

plt.tight_layout()
plt.savefig('c:\\Users\\Nessim\\OneDrive\\Desktop\\projet\\Methodologie-Adoptee.png', 
            dpi=300, bbox_inches='tight', facecolor='white', edgecolor='none')
print("✅ Image créée : Methodologie-Adoptee.png (Résolution: 4800x2700 - 300 DPI)")
print("📊 Prête pour Canva !")
plt.show()
