from pathlib import Path
from math import atan2, cos, sin, pi
from PIL import Image, ImageDraw, ImageFont


OUT = Path("diagrammes_pfe_design")
OUT.mkdir(exist_ok=True)

W, H = 1920, 1080

COLORS = {
    "bg": "#F6F8FB",
    "ink": "#0F172A",
    "muted": "#475569",
    "line": "#CBD5E1",
    "navy": "#12355B",
    "blue": "#2563EB",
    "cyan": "#0891B2",
    "teal": "#0F766E",
    "green": "#16A34A",
    "amber": "#D97706",
    "orange": "#EA580C",
    "purple": "#7C3AED",
    "rose": "#E11D48",
    "slate": "#334155",
    "white": "#FFFFFF",
}


def font(size, bold=False):
    names = [
        r"C:\Windows\Fonts\segoeuib.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf",
    ]
    for name in names:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            pass
    return ImageFont.load_default()


F_TITLE = font(48, True)
F_SUB = font(24)
F_H = font(28, True)
F_BODY = font(23)
F_SMALL = font(18)
F_TINY = font(15)


def new_canvas(title, subtitle):
    im = Image.new("RGB", (W, H), COLORS["bg"])
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, W, 118], fill=COLORS["navy"])
    d.text((70, 28), title, fill=COLORS["white"], font=F_TITLE)
    d.text((72, 82), subtitle, fill="#C7D2FE", font=F_SUB)
    d.rounded_rectangle([1645, 34, 1848, 86], radius=18, fill="#0F766E")
    d.text((1692, 48), "PFE TPE", fill=COLORS["white"], font=font(22, True))
    return im, d


def text_size(d, text, f):
    b = d.textbbox((0, 0), text, font=f)
    return b[2] - b[0], b[3] - b[1]


def wrap_text(d, text, f, max_w):
    words = text.split()
    lines, cur = [], ""
    for word in words:
        nxt = word if not cur else f"{cur} {word}"
        if text_size(d, nxt, f)[0] <= max_w:
            cur = nxt
        else:
            if cur:
                lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def shadow_box(d, box, radius=28, fill="#FFFFFF", outline="#D8E0EA", width=2):
    x1, y1, x2, y2 = box
    for i, alpha in enumerate([28, 18, 10]):
        off = 7 + i * 3
        d.rounded_rectangle([x1 + off, y1 + off, x2 + off, y2 + off], radius=radius, fill="#DDE5F0")
    d.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def card(d, box, title, lines=None, color="#2563EB", icon=None, title_size=27, body_size=21):
    lines = lines or []
    x1, y1, x2, y2 = box
    shadow_box(d, box, radius=24, fill=COLORS["white"], outline="#D7DEE8")
    d.rounded_rectangle([x1, y1, x2, y1 + 56], radius=24, fill=color)
    d.rectangle([x1, y1 + 28, x2, y1 + 56], fill=color)
    if icon:
        d.text((x1 + 24, y1 + 14), icon, fill=COLORS["white"], font=font(24, True))
        tx = x1 + 62
    else:
        tx = x1 + 24
    d.text((tx, y1 + 13), title, fill=COLORS["white"], font=font(title_size, True))
    yy = y1 + 78
    for line in lines:
        if isinstance(line, tuple):
            txt, c = line
        else:
            txt, c = line, COLORS["muted"]
        for part in wrap_text(d, txt, font(body_size), x2 - x1 - 54):
            d.text((x1 + 28, yy), part, fill=c, font=font(body_size))
            yy += body_size + 8
        yy += 4


def pill(d, box, text, fill="#E0F2FE", outline="#7DD3FC", color="#0F172A", f=None):
    f = f or font(21, True)
    d.rounded_rectangle(box, radius=999, fill=fill, outline=outline, width=2)
    tw, th = text_size(d, text, f)
    x1, y1, x2, y2 = box
    d.text(((x1 + x2 - tw) / 2, (y1 + y2 - th) / 2 - 2), text, fill=color, font=f)


def arrow(d, p1, p2, color="#64748B", width=5, label=None, label_pos=0.5, curved=False):
    x1, y1 = p1
    x2, y2 = p2
    d.line([p1, p2], fill=color, width=width)
    ang = atan2(y2 - y1, x2 - x1)
    size = 18
    pts = [
        (x2, y2),
        (x2 - size * cos(ang - pi / 7), y2 - size * sin(ang - pi / 7)),
        (x2 - size * cos(ang + pi / 7), y2 - size * sin(ang + pi / 7)),
    ]
    d.polygon(pts, fill=color)
    if label:
        lx = x1 + (x2 - x1) * label_pos
        ly = y1 + (y2 - y1) * label_pos
        pad = 10
        f = font(17, True)
        tw, th = text_size(d, label, f)
        d.rounded_rectangle([lx - tw / 2 - pad, ly - th / 2 - pad, lx + tw / 2 + pad, ly + th / 2 + pad], radius=12, fill=COLORS["white"], outline="#CBD5E1")
        d.text((lx - tw / 2, ly - th / 2 - 2), label, fill=color, font=f)


def save(im, name):
    path = OUT / name
    im.save(path, quality=96)
    return path


def architecture_logique():
    im, d = new_canvas(
        "Architecture logique de la solution",
        "Angular + Spring Boot + Services métier + SQL Server"
    )
    x = [90, 410, 760, 1110, 1460]
    y = 190
    h = 670
    cols = [
        ("Utilisateurs", ["Agence", "Monétique", "Inputer", "Authorizer", "Admin"], COLORS["slate"]),
        ("Présentation", ["Angular 14", "Routing + Guards", "Services HTTP", "Material Dashboard"], COLORS["blue"]),
        ("API REST", ["Spring Boot 3.2", "Controllers", "DTO + Validation", "Swagger/OpenAPI"], COLORS["orange"]),
        ("Métier", ["TPE & Commerçants", "Demandes & Affectations", "Pannes & Taux", "Audit + Notifications"], COLORS["teal"]),
        ("Persistance", ["JPA Repositories", "Hibernate", "SQL Server", "Import/Export"], COLORS["purple"]),
    ]
    boxes = []
    for i, (title, lines, color) in enumerate(cols):
        box = (x[i], y, x[i] + 290, y + h)
        boxes.append(box)
        card(d, box, title, lines, color=color, body_size=22)
    for i in range(len(boxes) - 1):
        arrow(d, (boxes[i][2] + 12, y + 335), (boxes[i + 1][0] - 12, y + 335), label=["HTTPS", "JSON", "Services", "ORM"][i])
    pill(d, [520, 910, 1400, 975], "Sécurité transversale : JWT, RBAC, permissions par écran, audit des actions", fill="#E0F2FE", outline="#0891B2", color="#0E7490")
    return save(im, "01_architecture_logique_design.png")


def architecture_physique():
    im, d = new_canvas(
        "Architecture physique de déploiement",
        "Postes utilisateurs, serveur applicatif, base de données et intégrations"
    )
    card(d, (80, 220, 415, 470), "Postes utilisateurs", ["Navigateur web", "Agence / Monétique", "Accès sécurisé HTTPS"], color=COLORS["slate"])
    card(d, (560, 185, 945, 505), "Serveur Frontend", ["Application Angular", "Assets statiques", "Interface responsive", "Guards + Interceptor JWT"], color=COLORS["blue"])
    card(d, (1065, 185, 1465, 505), "Serveur Backend", ["API Spring Boot", "Spring Security", "Services métier", "Swagger/OpenAPI"], color=COLORS["orange"])
    card(d, (1540, 220, 1840, 470), "SQL Server", ["Tables métier", "Audit logs", "Écritures comptables"], color=COLORS["purple"])
    card(d, (560, 640, 945, 870), "Fichiers & exports", ["Import Excel/CSV", "Fichier bancaire", "PDF / rapports"], color=COLORS["teal"])
    card(d, (1065, 640, 1465, 870), "Intégrations", ["Power BI", "Email notifications", "Dashboards KPI"], color=COLORS["green"])
    arrow(d, (415, 345), (560, 345), label="HTTPS")
    arrow(d, (945, 345), (1065, 345), label="REST JSON")
    arrow(d, (1465, 345), (1540, 345), label="JDBC")
    arrow(d, (1265, 505), (1265, 640), label="Données KPI")
    arrow(d, (752, 505), (752, 640), label="Imports")
    arrow(d, (945, 755), (1065, 755), label="Traitement")
    return save(im, "02_architecture_physique_design.png")


def use_case():
    im, d = new_canvas(
        "Diagramme de cas d'utilisation global",
        "Acteurs et fonctionnalités principales du système de gestion TPE"
    )
    d.rounded_rectangle([500, 180, 1420, 925], radius=42, fill="#FFFFFF", outline="#CBD5E1", width=3)
    d.text((710, 205), "Système de gestion du parc TPE", fill=COLORS["ink"], font=font(34, True))
    actors = [
        ("Agence", (105, 300), COLORS["blue"]),
        ("Monétique", (105, 610), COLORS["teal"]),
        ("Inputer", (1570, 300), COLORS["amber"]),
        ("Authorizer", (1570, 560), COLORS["rose"]),
        ("Admin", (1570, 820), COLORS["slate"]),
    ]
    for name, (ax, ay), color in actors:
        shadow_box(d, (ax, ay, ax + 255, ay + 118), radius=22, fill=COLORS["white"], outline="#D7DEE8")
        d.ellipse([ax + 20, ay + 25, ax + 70, ay + 75], fill="#EEF2FF", outline=color, width=4)
        d.line([ax + 45, ay + 77, ax + 45, ay + 100], fill=color, width=4)
        d.line([ax + 25, ay + 88, ax + 65, ay + 88], fill=color, width=4)
        d.text((ax + 90, ay + 40), name, fill=color, font=font(25, True))
    cases = [
        ("Créer demande TPE", 690, 300, COLORS["blue"]),
        ("Suivre demande", 1040, 300, COLORS["blue"]),
        ("Gérer TPE", 690, 450, COLORS["teal"]),
        ("Affecter terminal", 1040, 450, COLORS["teal"]),
        ("Déclarer / traiter panne", 690, 600, COLORS["green"]),
        ("Gérer taux", 1040, 600, COLORS["amber"]),
        ("Valider 4 yeux", 1040, 750, COLORS["rose"]),
        ("Dashboards & rapports", 690, 750, COLORS["purple"]),
    ]
    for label, cx, cy, color in cases:
        d.ellipse([cx - 150, cy - 48, cx + 150, cy + 48], fill="#F8FAFC", outline=color, width=4)
        lines = wrap_text(d, label, font(20, True), 230)
        sy = cy - (len(lines) * 24) / 2
        for line in lines:
            tw, _ = text_size(d, line, font(20, True))
            d.text((cx - tw / 2, sy), line, fill=COLORS["ink"], font=font(20, True))
            sy += 26
    for p1, p2, c in [
        ((360, 360), (540, 300), COLORS["blue"]), ((360, 360), (890, 300), COLORS["blue"]),
        ((360, 670), (540, 450), COLORS["teal"]), ((360, 670), (890, 450), COLORS["teal"]),
        ((360, 670), (540, 600), COLORS["green"]), ((360, 670), (540, 750), COLORS["purple"]),
        ((1570, 360), (1190, 600), COLORS["amber"]),
        ((1570, 620), (1190, 750), COLORS["rose"]),
        ((1570, 880), (1190, 750), COLORS["slate"]), ((1570, 880), (890, 750), COLORS["slate"]),
    ]:
        d.line([p1, p2], fill=c, width=3)
    return save(im, "03_cas_utilisation_global_design.png")


def composants():
    im, d = new_canvas(
        "Diagramme de composants",
        "Vue modulaire de l'application et des échanges entre composants"
    )
    comps = [
        ((90, 215, 450, 405), "Frontend Angular", ["Pages", "Components", "Services HTTP"], COLORS["blue"]),
        ((90, 550, 450, 790), "Sécurité UI", ["AuthGuard", "Interceptor JWT", "Permissions écran"], COLORS["slate"]),
        ((600, 175, 980, 360), "Controllers REST", ["Auth", "TPE", "Demandes", "Pannes", "Taux"], COLORS["orange"]),
        ((600, 450, 980, 650), "Services métier", ["Workflows", "Règles de gestion", "Audit"], COLORS["teal"]),
        ((600, 745, 980, 930), "DTO & Exceptions", ["Validation", "Mapping", "Gestion erreurs"], COLORS["rose"]),
        ((1130, 220, 1490, 410), "Repositories", ["Spring Data JPA", "Requêtes métier", "Pagination"], COLORS["purple"]),
        ((1130, 580, 1490, 790), "Intégrations", ["Excel/CSV", "PDF", "Email", "Power BI"], COLORS["green"]),
        ((1590, 360, 1840, 640), "SQL Server", ["Données", "Audit", "Postings"], COLORS["navy"]),
    ]
    for box, title, lines, color in comps:
        card(d, box, title, lines, color=color, body_size=21)
    arrow(d, (450, 310), (600, 270), label="REST")
    arrow(d, (450, 670), (600, 545), label="JWT")
    arrow(d, (790, 360), (790, 450), label="Appel")
    arrow(d, (980, 545), (1130, 315), label="JPA")
    arrow(d, (1490, 315), (1590, 500), label="SQL")
    arrow(d, (980, 545), (1130, 685), label="Exports")
    arrow(d, (790, 650), (790, 745), label="DTO")
    return save(im, "04_diagramme_composants_design.png")


def classes_metier():
    im, d = new_canvas(
        "Diagramme de classes métier simplifié",
        "Entités principales et relations du domaine TPE"
    )
    boxes = {
        "User": (80, 220, 380, 390, "Utilisateur", ["username", "email", "roles"], COLORS["slate"]),
        "Dem": (450, 220, 760, 435, "Demande", ["reference", "typeDemande", "urgence", "statut"], COLORS["orange"]),
        "Com": (830, 220, 1140, 410, "Commerçant", ["raisonSociale", "compte", "statut"], COLORS["teal"]),
        "TPE": (1210, 220, 1520, 410, "TPE", ["numeroSerie", "numeroTerminal", "statut"], COLORS["blue"]),
        "Pan": (1580, 220, 1840, 410, "Panne", ["reference", "diagnostic", "statut"], COLORS["rose"]),
        "Audit": (80, 650, 380, 820, "AuditLog", ["action", "entity", "date"], COLORS["purple"]),
        "Taux": (830, 650, 1140, 840, "Taux", ["ancienTaux", "nouveauTaux", "statut 4 yeux"], COLORS["amber"]),
        "Aff": (1210, 650, 1520, 865, "Affectation", ["dateAffectation", "actif", "dateMiseEnService"], COLORS["green"]),
    }
    anchors = {}
    for key, (x1, y1, x2, y2, title, attrs, color) in boxes.items():
        card(d, (x1, y1, x2, y2), title, attrs, color=color, body_size=20)
        anchors[key] = {"l": ((x1), (y1 + y2) / 2), "r": ((x2), (y1 + y2) / 2), "t": ((x1 + x2) / 2, y1), "b": ((x1 + x2) / 2, y2)}

    def relation(a, side_a, b, side_b, label, color="#64748B"):
        p1 = anchors[a][side_a]
        p2 = anchors[b][side_b]
        d.line([p1, p2], fill=color, width=4)

    relation("User", "r", "Dem", "l", "crée")
    relation("User", "b", "Audit", "t", "audit")
    relation("Dem", "r", "Com", "l", "concerne")
    relation("Com", "r", "TPE", "l", "possède")
    relation("TPE", "r", "Pan", "l", "déclare")
    relation("Com", "b", "Taux", "t", "tarifs")
    relation("Com", "b", "Aff", "t", "affecte")
    relation("TPE", "b", "Aff", "t", "lie")
    relation("Dem", "b", "Aff", "l", "génère")

    pill(d, [455, 900, 1465, 970], "Règles clés : un TPE affecté à un seul commerçant actif, TID unique, validation 4 yeux", fill="#FEF3C7", outline="#F59E0B", color="#92400E")
    return save(im, "05_diagramme_classes_metier_design.png")


def sequence_affectation():
    im, d = new_canvas(
        "Diagramme de séquence : demande, validation et affectation",
        "Flux principal entre Agence, Frontend, API, services métier et base de données"
    )
    xs = [190, 500, 810, 1120, 1430, 1700]
    labels = ["Agence", "Angular UI", "Demande API", "DemandeService", "AffectationService", "Base SQL"]
    colors = [COLORS["blue"], COLORS["teal"], COLORS["orange"], COLORS["orange"], COLORS["green"], COLORS["purple"]]
    for x, label, c in zip(xs, labels, colors):
        pill(d, [x - 115, 170, x + 115, 225], label, fill="#FFFFFF", outline=c, color=c)
        d.line([x, 230, x, 920], fill="#CBD5E1", width=3)
    steps = [
        (0, 1, 285, "Saisir demande TPE"),
        (1, 2, 350, "POST /api/demandes"),
        (2, 3, 415, "createDemande(dto)"),
        (3, 5, 480, "save statut=NOUVELLE"),
        (3, 1, 545, "201 Created"),
        (0, 1, 620, "Validation Monétique"),
        (1, 2, 685, "POST /demandes/{id}/valider"),
        (2, 3, 750, "validerDemande()"),
        (3, 4, 815, "affecterTPE()"),
        (4, 5, 880, "TPE=AFFECTE, demande=AFFECTEE"),
    ]
    for a, b, y, label in steps:
        color = "#64748B" if a < b else "#94A3B8"
        arrow(d, (xs[a], y), (xs[b], y), color=color, width=4, label=label)
    return save(im, "06_sequence_validation_affectation_design.png")


def workflow_4_yeux():
    im, d = new_canvas(
        "Workflow de validation 4 yeux",
        "Séparation des rôles Inputer et Authorizer pour les opérations sensibles"
    )
    stages = [
        ("1", "Saisie", "Inputer crée la demande/taux et complète les données monétiques", COLORS["amber"]),
        ("2", "Soumission", "Statut EN_COURS ou EN_ATTENTE_VALIDATION", COLORS["blue"]),
        ("3", "Contrôle", "Authorizer différent de l'Inputer vérifie la saisie", COLORS["rose"]),
        ("4", "Décision", "Validation ou rejet avec commentaire/motif", COLORS["teal"]),
        ("5", "Application", "Affectation TPE, taux actif, audit et notifications", COLORS["green"]),
    ]
    y = 390
    prev = None
    for i, (num, title, desc, color) in enumerate(stages):
        x = 115 + i * 360
        d.ellipse([x, y - 98, x + 120, y + 22], fill=color, outline="#FFFFFF", width=6)
        tw, th = text_size(d, num, font(42, True))
        d.text((x + 60 - tw / 2, y - 47 - th / 2), num, fill=COLORS["white"], font=font(42, True))
        card(d, (x - 55, y + 70, x + 245, y + 320), title, [desc], color=color, body_size=20)
        if prev:
            arrow(d, (prev + 245, y + 185), (x - 55, y + 185), color="#64748B", width=5)
        prev = x - 55
    pill(d, [370, 875, 1550, 945], "Contrôle automatique : si Inputer = Authorizer, l'API bloque la validation et journalise l'événement", fill="#FEE2E2", outline="#E11D48", color="#BE123C")
    return save(im, "07_workflow_4_yeux_design.png")


def scrum():
    im, d = new_canvas(
        "Méthodologie Agile Scrum",
        "Organisation itérative du projet PFE et incréments livrés"
    )
    card(d, (90, 235, 430, 470), "Product Backlog", ["Besoins Agence", "Besoins Monétique", "Contraintes sécurité"], color=COLORS["slate"])
    card(d, (560, 180, 900, 430), "Sprint Planning", ["Prioriser", "Découper", "Estimer"], color=COLORS["blue"])
    card(d, (1040, 180, 1380, 430), "Sprint", ["Développement", "Tests", "Corrections"], color=COLORS["teal"])
    card(d, (1490, 235, 1830, 470), "Incrément", ["Module fonctionnel", "Démo", "Feedback"], color=COLORS["green"])
    card(d, (560, 650, 900, 880), "Daily / Suivi", ["Blocages", "Avancement", "Qualité"], color=COLORS["amber"])
    card(d, (1040, 650, 1380, 880), "Review & Rétro", ["Validation", "Améliorations", "Nouveau sprint"], color=COLORS["purple"])
    arrow(d, (430, 355), (560, 305), label="sélection")
    arrow(d, (900, 305), (1040, 305), label="engagement")
    arrow(d, (1380, 305), (1490, 355), label="livraison")
    arrow(d, (1210, 430), (1210, 650), label="review")
    arrow(d, (1040, 765), (900, 765), label="ajustement")
    arrow(d, (730, 650), (730, 430), label="suivi")
    pill(d, [275, 930, 1645, 995], "Sprints du projet : socle technique -> TPE/commerçants -> demandes/affectations -> pannes/taux -> dashboards/finalisation", fill="#EDE9FE", outline="#7C3AED", color="#5B21B6")
    return save(im, "08_methodologie_scrum_design.png")


def main():
    generated = [
        architecture_logique(),
        architecture_physique(),
        use_case(),
        composants(),
        classes_metier(),
        sequence_affectation(),
        workflow_4_yeux(),
        scrum(),
    ]
    index = OUT / "README.md"
    index.write_text(
        "# Diagrammes PFE - version design\n\n"
        "Fichiers PNG 16:9 haute résolution, prêts à insérer dans PowerPoint.\n\n"
        + "\n".join(f"- {p.name}" for p in generated)
        + "\n",
        encoding="utf-8",
    )
    for p in generated:
        print(p)


if __name__ == "__main__":
    main()
