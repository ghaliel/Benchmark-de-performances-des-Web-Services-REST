-- Vérification des données
SELECT
    (SELECT COUNT(*) FROM category) as total_categories,
    (SELECT COUNT(*) FROM item) as total_items,
    (SELECT COUNT(*) FROM item)::float / (SELECT COUNT(*) FROM category) as avg_items_per_category;

-- Exemple de données
SELECT '=== 5 PREMIÈRES CATÉGORIES ===' as info;
SELECT id, code, name FROM category ORDER BY id LIMIT 5;

SELECT '=== 10 PREMIERS ITEMS ===' as info;
SELECT i.id, i.sku, i.name, i.price, c.code as category_code
FROM item i
         JOIN category c ON i.category_id = c.id
ORDER BY i.id LIMIT 10;