
-- =========================================
-- SMARTMART POS
-- INITIAL DATA (SEED)
-- =========================================

-- Aturan diskon FreshSave
INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 7, 10
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 7
);

INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 3, 20
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 3
);

INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 1, 30
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 1
);
