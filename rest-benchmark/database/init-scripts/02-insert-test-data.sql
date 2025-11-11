-- =============================================
-- DONNÉES RÉALISTES POUR BENCHMARK E-COMMERCE
-- =============================================

-- 🏷️ Catégories réalistes (électronique, vêtements, maison, etc.)
INSERT INTO category (code, name)
SELECT
    'CAT' || LPAD(seq::text, 4, '0'),
    CASE (seq % 10)
        WHEN 0 THEN 'Électronique - ' || (ARRAY['Portable', 'Smartphone', 'Tablette', 'Audio', 'Gaming'])[seq % 5 + 1]
        WHEN 1 THEN 'Vêtements - ' || (ARRAY['Homme', 'Femme', 'Enfant', 'Sport', 'Accessoires'])[seq % 5 + 1]
        WHEN 2 THEN 'Maison - ' || (ARRAY['Cuisine', 'Décoration', 'Jardin', 'Bricolage', 'Literie'])[seq % 5 + 1]
        WHEN 3 THEN 'Sport - ' || (ARRAY['Fitness', 'Running', 'Yoga', 'Vélo', 'Randonnée'])[seq % 5 + 1]
        WHEN 4 THEN 'Loisirs - ' || (ARRAY['Livres', 'Jeux', 'Musique', 'Art', 'Collection'])[seq % 5 + 1]
        WHEN 5 THEN 'Beauté - ' || (ARRAY['Soin', 'Maquillage', 'Parfum', 'Cheveux', 'Corps'])[seq % 5 + 1]
        WHEN 6 THEN 'Auto - ' || (ARRAY['Pièces', 'Accessoires', 'Entretien', 'Carrosserie', 'Intérieur'])[seq % 5 + 1]
        WHEN 7 THEN 'High-Tech - ' || (ARRAY['Ordinateur', 'Connecté', 'Photo', 'Réseau', 'Stockage'])[seq % 5 + 1]
        WHEN 8 THEN 'Bébé - ' || (ARRAY['Puériculture', 'Jouets', 'Alimentation', 'Hygiène', 'Vêtements'])[seq % 5 + 1]
        ELSE 'Autre - ' || (ARRAY['Bureau', 'Voyage', 'Animalerie', 'Instruments', 'Bijoux'])[seq % 5 + 1]
        END
FROM generate_series(1, 2000) as seq;

-- 📦 Items réalistes avec noms de produits crédibles
INSERT INTO item (sku, name, price, stock, description, category_id)
SELECT
    -- SKU professionnel
    CASE (cat_id % 10)
        WHEN 0 THEN 'ELEC-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 1 THEN 'FASH-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 2 THEN 'HOME-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 3 THEN 'SPT-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 4 THEN 'LEIS-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 5 THEN 'BEAU-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 6 THEN 'AUTO-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 7 THEN 'TECH-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        WHEN 8 THEN 'BABY-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        ELSE 'MISC-' || cat_id || '-' || LPAD(item_seq::text, 3, '0')
        END as sku,

    -- Noms de produits réalistes
    CASE (cat_id % 10)
        -- Électronique
        WHEN 0 THEN
            CASE (item_seq % 8)
                WHEN 0 THEN 'Smartphone ' || (ARRAY['Galaxy', 'iPhone', 'Pixel', 'Xiaomi', 'OnePlus'])[item_seq % 5 + 1] || ' ' || (2020 + item_seq % 4)
                WHEN 1 THEN 'Ordinateur Portable ' || (ARRAY['ThinkPad', 'MacBook', 'ZenBook', 'Surface', 'Dell'])[item_seq % 5 + 1]
                WHEN 2 THEN 'Casque Audio ' || (ARRAY['Sony WH-1000XM', 'Bose QuietComfort', 'Sennheiser', 'JBL', 'Audio-Technica'])[item_seq % 5 + 1]
                WHEN 3 THEN 'Montre Connectée ' || (ARRAY['Apple Watch', 'Samsung Galaxy Watch', 'Fitbit', 'Garmin', 'Huawei'])[item_seq % 5 + 1]
                WHEN 4 THEN 'Tablette ' || (ARRAY['iPad', 'Galaxy Tab', 'Surface Pro', 'Lenovo Tab', 'Fire HD'])[item_seq % 5 + 1]
                WHEN 5 THEN 'Enceinte Bluetooth ' || (ARRAY['JBL Flip', 'Sonos One', 'Bose SoundLink', 'Marshall', 'Ultimate Ears'])[item_seq % 5 + 1]
                WHEN 6 THEN 'Console de Jeu ' || (ARRAY['PlayStation', 'Xbox', 'Nintendo Switch', 'Steam Deck', 'Oculus'])[item_seq % 5 + 1]
                ELSE 'Écran ' || (ARRAY[24, 27, 32, 34, 49])[item_seq % 5 + 1] || ' pouces 4K'
                END

        -- Vêtements
        WHEN 1 THEN
            CASE (item_seq % 7)
                WHEN 0 THEN 'T-shirt ' || (ARRAY['Basique', 'Manches Longues', 'Col V', 'Oversize', 'Sport'])[item_seq % 5 + 1]
                WHEN 1 THEN 'Jean ' || (ARRAY['Slim', 'Droit', 'Bootcut', 'Délavé', 'Noir'])[item_seq % 5 + 1]
                WHEN 2 THEN 'Robe ' || (ARRAY['Été', 'Soirée', 'Bureau', 'Midi', 'Longue'])[item_seq % 5 + 1]
                WHEN 3 THEN 'Veste ' || (ARRAY['Cuir', 'Denim', 'Bomber', 'Blazer', 'Doudoune'])[item_seq % 5 + 1]
                WHEN 4 THEN 'Chaussures ' || (ARRAY['Baskets', 'Running', 'Bottes', 'Sandales', 'Escarpins'])[item_seq % 5 + 1]
                WHEN 5 THEN 'Accessoire ' || (ARRAY['Ceinture', 'Sac', 'Bijou', 'Lunettes', 'Chapeau'])[item_seq % 5 + 1]
                ELSE 'Sous-vêtement ' || (ARRAY['Boxer', 'Culotte', 'Soutien-gorge', 'Bas', 'Pyjama'])[item_seq % 5 + 1]
                END

        -- Maison
        WHEN 2 THEN
            CASE (item_seq % 6)
                WHEN 0 THEN 'Meuble ' || (ARRAY['Canapé', 'Table', 'Chaise', 'Armoire', 'Étagère'])[item_seq % 5 + 1]
                WHEN 1 THEN 'Électroménager ' || (ARRAY['Réfrigérateur', 'Lave-vaisselle', 'Four', 'Lave-linge', 'Aspirateur'])[item_seq % 5 + 1]
                WHEN 2 THEN 'Décoration ' || (ARRAY['Tableau', 'Vase', 'Luminaire', 'Tapis', 'Coussin'])[item_seq % 5 + 1]
                WHEN 3 THEN 'Cuisine ' || (ARRAY['Casserole', 'Poêle', 'Couteau', 'Ustensile', 'Robot'])[item_seq % 5 + 1]
                WHEN 4 THEN 'Literie ' || (ARRAY['Matelas', 'Oreiller', 'Couette', 'Drap', 'Couverture'])[item_seq % 5 + 1]
                ELSE 'Jardin ' || (ARRAY['Chaise', 'Table', 'Parasol', 'Barbecue', 'Outillage'])[item_seq % 5 + 1]
                END

        -- Autres catégories (patterns similaires)
        ELSE
            'Produit ' || (ARRAY['Premium', 'Standard', 'Deluxe', 'Économique', 'Professionnel'])[item_seq % 5 + 1] ||
            ' Catégorie ' || (cat_id % 10)
        END as name,

    -- Prix réalistes selon la catégorie
    CASE (cat_id % 10)
        WHEN 0 THEN (RANDOM() * 1990 + 10)::numeric(10,2)  -- Électronique: 10-2000€
        WHEN 1 THEN (RANDOM() * 190 + 10)::numeric(10,2)   -- Vêtements: 10-200€
        WHEN 2 THEN (RANDOM() * 990 + 10)::numeric(10,2)   -- Maison: 10-1000€
        WHEN 7 THEN (RANDOM() * 2990 + 50)::numeric(10,2)  -- High-Tech: 50-3000€
        ELSE (RANDOM() * 490 + 5)::numeric(10,2)           -- Autres: 5-500€
        END as price,

    -- Stock réaliste
    CASE
        WHEN (cat_id % 10) IN (0, 7) THEN FLOOR(RANDOM() * 50)::int      -- Électronique/High-Tech: stock limité
        WHEN (cat_id % 10) = 1 THEN FLOOR(RANDOM() * 200)::int           -- Vêtements: stock moyen
        ELSE FLOOR(RANDOM() * 100)::int                                  -- Autres: stock standard
        END as stock,

    -- Descriptions détaillées et réalistes
    CASE
        WHEN random() < 0.2 THEN NULL  -- 20% sans description
        ELSE
            CASE (cat_id % 10)
                WHEN 0 THEN
                    'Smartphone haut de gamme avec écran AMOLED 6.7", processeur octa-core, 128GB stockage, double caméra 48MP, batterie 5000mAh. Compatible 5G. Garantie 2 ans.'
                WHEN 1 THEN
                    'Vêtement en coton bio de haute qualité. Coupe moderne et confortable. Lavable en machine. Disponible en plusieurs coloris. Taille standard.'
                WHEN 2 THEN
                    'Meuble design en bois massif. Facile à monter. Dimensions: ' ||
                    (ARRAY[80, 120, 160, 200, 240])[item_seq % 5 + 1] || 'cm. Finition résistante. Livraison gratuite.'
                WHEN 7 THEN
                    'Ordinateur portable professionnel. Processeur Intel Core i7, 16GB RAM, SSD 512GB, écran 15.6" FHD. Idéal pour gaming et création.'
                ELSE
                    'Produit de qualité supérieure. Fabriqué avec des matériaux durables. Satisfaction garantie ou remboursé sous 30 jours.'
                END ||
            CASE WHEN random() < 0.5 THEN
                     ' Livraison offerte dès 50€ d''achat. Retour gratuit sous 14 jours.'
                 ELSE '' END
        END as description,

    cat_id as category_id

FROM
    (SELECT id as cat_id FROM category ORDER BY id) cats,
    generate_series(1, 50) as item_seq;

-- 📈 Mise à jour des statistiques pour l'optimiseur
ANALYZE category;
ANALYZE item;

-- ✅ Vérification des données insérées
SELECT
    '🎯 DONNÉES RÉALISTES CRÉÉES' as result,
    (SELECT COUNT(*) FROM category) || ' catégories' as categories,
    (SELECT COUNT(*) FROM item) || ' items' as items,
    'Prix moyen: ' || (SELECT AVG(price) FROM item)::numeric(10,2) || '€' as avg_price,
    'Stock moyen: ' || (SELECT AVG(stock) FROM item)::numeric(10,1) as avg_stock;