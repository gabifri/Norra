package com.example.ui

import androidx.compose.ui.graphics.Color

data class ResourceArticle(
    val id: String,
    val categoryId: String,
    val title: String,
    val categoryBadge: String,
    val tagColor: Color,
    val description: String,
    val longContent: String,
    val bulletPoints: List<String>
)

val libraryArticles = listOf(
    ResourceArticle(
        id = "dbt_impulses",
        categoryId = "coping",
        title = "Gérer les impulsions & vagues de dérégulation (DBT)",
        categoryBadge = "Palier au Trouble 🌋",
        tagColor = Color(0xFFFF5252),
        description = "La tolérance à la détresse consiste à survivre psychologiquement à la tempête émotionnelle sans aggraver la situation par des agissements blessants.",
        longContent = "La tolérance à la détresse est une compétence fondamentale de la thérapie comportementale dialectique (TCD). Elle ne sert pas à se sentir \"bien\" ou \"heureux\", mais plutôt à traverser une tempête émotionnelle intense sans aggraver la situation par des comportements autodestructeurs.\n\nLorsque la douleur émotionnelle atteint un pic d'intensité, la capacité de réfléchir rationnellement (le cortex préfrontal) est momentanément désactivée au profit du cerveau reptilien de survie. L'objectif est simplement d'attendre que la vague redescende sans créer de nouveaux problèmes.\n\nLes stratégies clés incluent :\n\n- L'acceptation radicale : reconnaître et accepter la réalité de la douleur présente sans la juger ni la combattre.\n- La distraction ciblée : engager son esprit dans une tâche mathématique, un puzzle, ou une conversation neutre.\n- L'auto-apaisement sensoriel : utiliser la vue, l'ouïe, l'odorat, le goût et le toucher pour rassurer physiologiquement le système nerveux parasympathique.",
        bulletPoints = listOf(
            "Accepte la vague : Résister à l'émotion ou s'en vouloir crée une double culpabilité. Répète-toi calmement : 'C'est inconfortable, mais je suis en sécurité.'",
            "La boîte d'auto-apaisement : Prépare une boîte chez toi contenant des objets stimulants pour tes 5 sens.",
            "La communication d'affirmation : Exprime ta douleur avec des phrases commençant par 'Je' ('Je me sens submergé') plutôt qu'en attaquant l'autre avec 'Tu'."
        )
    ),
    ResourceArticle(
        id = "anxiety_tca",
        categoryId = "coping",
        title = "Réguler l'Anxiété sans recourir aux TCA ou comportements néfastes",
        categoryBadge = "Palier au Trouble 🌋",
        tagColor = Color(0xFFFF5252),
        description = "L'anxiété se traduit souvent par des impulsions physiques violentes (Manger compulsivement, hyperactivité...).",
        longContent = "L'anxiété chronique ou les attaques de panique génèrent une énorme quantité d'adrénaline et de cortisol dans le corps. Face à cet afflux, nous cherchons instinctivement une soupape de décompression. Très souvent, les Troubles du Comportement Alimentaire (hyperphagie, restriction nerveuse), les addictions ou les actes compulsifs agissent comme des régulateurs d'urgence très efficaces sur le court terme, mais destructeurs à long terme.\n\nComprendre ce mécanisme permet de s'en détacher. Ton comportement n'est pas un manque de volonté, c'est une réaction d'adaptation à une souffrance intolérable. Pour déprogrammer cela, il faut offrir au cerveau des alternatives tout aussi efficaces mais inoffensives.\n\nLa technique du \"Surfing the urge\" (Surfer sur l'urgence) consiste à imaginer l'impulsion comme une vague de l'océan. Elle monte, atteint une crête, puis s'écrase. Tu n'as pas à bloquer la vague, simplement à \"tenir en équilibre\" pendant les 10 à 15 minutes où elle est la plus forte.",
        bulletPoints = listOf(
            "Délai de grâce (Surfing the urge) : Reporte l'action néfaste de 15 minutes. Pendant ce délai, engage-toi dans une activité complexe (dessin, puzzle, appel).",
            "Répondre au besoin racine : Est-ce de la vraie faim, ou as-tu froid, es-tu seul(e), es-tu fatigué(e) ? Résous le 'vrai' problème.",
            "Utilise un exercice TIPP (Ex: eau très froide sur le visage) pour provoquer le réflexe mammifère de plongée et casser l'obsession émotionnelle instantanément."
        )
    ),
    ResourceArticle(
        id = "stability_routine",
        categoryId = "daily",
        title = "Construire une routine de stabilité au quotidien",
        categoryBadge = "Vivre Avec 🌿",
        tagColor = Color(0xFF4CAF50),
        description = "Pour espacer les crises et stabiliser durablement l'humeur, le cerveau a besoin de repères biologiques solides.",
        longContent = "L'hygiène de vie et la routine quotidienne sont souvent perçues comme ennuyeuses ou contraignantes, mais pour un système nerveux atypique, hyperactif ou traumatisé, la routine est le plus grand des boucliers protecteurs.\n\nL'horloge circadienne (notre rythme veille/sommeil) régule la production de sérotonine (humeur) et de mélatonine (sommeil). Un sommeil chaotique multiplie par trois la vulnérabilité émotionnelle le lendemain. Créer un cadre prévisible rassure la partie primitive de ton cerveau, qui n'aura plus besoin d'être sur le qui-vive permanent.\n\nDe plus, l'isolation sociale et l'inertie physique aggravent les symptômes dépressifs. Une routine, même minimaliste, garantit un minimum d'auto-soin même les jours où la motivation est nulle.",
        bulletPoints = listOf(
            "Régularité biologique : Endors-toi et lève-toi à heure fixe. Les perturbations de sommeil dérèglent brutalement la vulnérabilité émotionnelle.",
            "Accumuler des émotions positives : Planifie chaque jour deux micro-plaisirs intentionnels (écouter ton morceau favori, savourer un thé au calme, respirer dehors).",
            "Découper en micro-tâches : Quand la paralysie anxieuse est là, ne vise pas la montagne. Rappelle-toi qu'un petit pas est une immense réussite."
        )
    ),
    ResourceArticle(
        id = "tcc_thoughts",
        categoryId = "daily",
        title = "Restructuration Cognitive (TCC) et Décatastrophisation",
        categoryBadge = "Vivre Avec 🌿",
        tagColor = Color(0xFF4CAF50),
        description = "Nos pensées influencent nos émotions. Apprendre à questionner ses pensées intrusives permet d'abaisser l'anxiété.",
        longContent = "La Thérapie Cognitivo-Comportementale (TCC) nous enseigne une vérité fondamentale : ce n'est pas uniquement la situation extérieure qui crée la détresse, mais surtout l'interprétation (la pensée) que nous en faisons.\n\nFace au stress, notre cerveau peut développer des 'distorsions cognitives' : la lecture de pensée (croire savoir que l'autre nous déteste), le scénario catastrophe (imaginer systématiquement la pire issue), ou la pensée tout-ou-rien.\n\nLa restructuration cognitive consiste à s'arrêter au moment où l'angoisse monte, d'isoler la pensée automatique qui l'a déclenchée, et de la soumettre à un tribunal intérieur : Quelles sont les preuves réelles pour soutenir cette pensée ? Y a-t-il une autre explication possible ? Et même si le pire arrivait, quelles seraient mes ressources pour y faire face ?",
        bulletPoints = listOf(
            "Vérifier les preuves : Demande-toi : « Quelles sont les preuves concrètes qui rendent cette pensée absolument vraie ? »",
            "La théorie de l'ami : Si ton meilleur ami affrontait la même situation et disait tes mots, que lui répondrais-tu avec bienveillance ?",
            "L'erreur fondamentale de prédiction : Reconnaître qu'on surestime systématiquement la gravité d'un événement et qu'on sous-estime notre capacité à le surmonter."
        )
    ),
    ResourceArticle(
        id = "self_compassion",
        categoryId = "compassion",
        title = "Cultiver l'auto-compassion bienveillante",
        categoryBadge = "Auto-Compassion ✨",
        tagColor = Color(0xFFE040FB),
        description = "Vivre avec une dérégulation émotionnelle s'accompagne souvent d'une critique féroce. Apprendre à se pardonner est crucial.",
        longContent = "L'auto-compassion n'est pas de l'apitoiement sur soi ni de l'égocentrisme. Selon la chercheuse Kristin Neff, elle repose sur trois piliers : la bienveillance envers soi-même face à l'échec (plutôt que la critique), la commune humanité (reconnaître que la souffrance fait partie de l'expérience humaine universelle), et la pleine conscience (observer sa souffrance sans l'ignorer ni l'exagérer).\n\nLes personnes neurodivergentes ou souffrant de troubles de l'humeur ont souvent internalisé les jugements de la société : 'Tu es trop sensible', 'Tu es paresseux', 'Tu fais exprès'. Résultat : une voix intérieure tyrannique qui punit chaque erreur. Remplacer cette voix critique par une voix de parent soutenant, aimant et validant est le chemin de guérison le plus durable.",
        bulletPoints = listOf(
            "Ton trouble n'est pas ton identité : Tu es une personne hypersensible confrontée à un système d'alerte surréactif. Ce n'est pas une faiblesse morale.",
            "Le journal des fiertés : Note chaque soir trois choses positives accomplies aujourd'hui (comme s'être levé, s'être hydraté, ou avoir souri).",
            "La pause compassion : Face à la douleur, dis-toi consciemment : 'C'est un moment de souffrance. La souffrance fait partie de la vie. Que puis-je faire pour moi-même maintenant ?'"
        )
    ),
    ResourceArticle(
        id = "high_sensitivity",
        categoryId = "daily",
        title = "Honorer et apprivoiser ta grande sensibilité",
        categoryBadge = "Vivre Avec 🌿",
        tagColor = Color(0xFF4CAF50),
        description = "Avoir des émotions intenses signifie aussi ressentir la beauté et l'art à un niveau exceptionnel.",
        longContent = "L'hypersensibilité émotionnelle ou neurosensorielle est souvent perçue uniquement comme un handicap, car le monde moderne sur-stimulant n'est pas conçu pour les systèmes nerveux délicats. Pourtant, cette même sensibilité qui rend la tristesse dévastatrice est aussi celle qui rend l'empathie profonde, l'intuition affûtée, et la joie transcendante.\n\nIl ne s'agit pas de tuer l'émotion pour devenir une machine froide, mais d'apprendre à surfer sur les vagues avec un meilleur équilibre. Honorer sa sensibilité, cest aussi poser des limites fermes avec son environnement, accepter d'avoir besoin de plus de temps de repos, de calme ou de solitude que la moyenne, sans ressentir la moindre culpabilité.",
        bulletPoints = listOf(
            "Vois l'émotion comme une météo : Le ciel s'assombrit et l'orage éclate, mais le ciel bleu est toujours immuable derrière les nuages. Ça passe toujours.",
            "Garde-toi du 'tout ou rien' : Ton esprit aime le noir ou blanc. Introduis la nuance grise : 'Ce n'est pas parfait mais c'est okay'.",
            "L'espace refuge : Crée un espace physique chez toi, inviolable, où tu peux te retirer chaque fois que tu es sur-sollicité(e)."
        )
    )
)
