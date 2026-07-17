package kongju.pickmeal.core.user.type;

import lombok.Getter;

@Getter
public enum DiseaseName {
    // 대사성 질환
    DIABETES(DiseaseCategory.METABOLIC),          // 당뇨
    HYPERLIPIDEMIA(DiseaseCategory.METABOLIC),    // 고지혈증
    OBESITY(DiseaseCategory.METABOLIC),           // 비만
    GOUT(DiseaseCategory.METABOLIC),              // 통풍

    // 심혈관계 질환
    HYPERTENSION(DiseaseCategory.CARDIOVASCULAR), // 고혈압
    HEART_DISEASE(DiseaseCategory.CARDIOVASCULAR),// 심장질환
    STROKE(DiseaseCategory.CARDIOVASCULAR),       // 뇌졸중

    // 소화기계 질환
    GASTRITIS(DiseaseCategory.DIGESTIVE),         // 위염
    GASTRIC_ULCER(DiseaseCategory.DIGESTIVE),     // 위궤양
    REFLUX_ESOPHAGITIS(DiseaseCategory.DIGESTIVE),// 역류성 식도염
    IRRITABLE_BOWEL_SYNDROME(DiseaseCategory.DIGESTIVE), // 과민성 대장 증후군

    // 호흡기계 질환
    ASTHMA(DiseaseCategory.RESPIRATORY),          // 천식
    RHINITIS(DiseaseCategory.RESPIRATORY),        // 비염
    COPD(DiseaseCategory.RESPIRATORY),            // 만성폐쇄성폐질환

    // 신장 질환
    KIDNEY_DISEASE(DiseaseCategory.KIDNEY),       // 신장질환
    CHRONIC_KIDNEY_DISEASE(DiseaseCategory.KIDNEY), // 만성 신부전

    // 간 질환
    FATTY_LIVER(DiseaseCategory.LIVER),           // 지방간
    HEPATITIS(DiseaseCategory.LIVER),             // 간염
    LIVER_CIRRHOSIS(DiseaseCategory.LIVER),       // 간경변

    // 내분비 질환
    HYPOTHYROIDISM(DiseaseCategory.ENDOCRINE),    // 갑상선 기능 저하증
    HYPERTHYROIDISM(DiseaseCategory.ENDOCRINE),   // 갑상선 기능 항진증

    // 면역/알러지 질환
    ATOPY(DiseaseCategory.IMMUNE),                // 아토피
    FOOD_ALLERGY(DiseaseCategory.IMMUNE),         // 식품 알러지
    AUTOIMMUNE_DISEASE(DiseaseCategory.IMMUNE),   // 자가면역질환

    // 근골격계 질환
    OSTEOPOROSIS(DiseaseCategory.MUSCULOSKELETAL),// 골다공증
    ARTHRITIS(DiseaseCategory.MUSCULOSKELETAL),   // 관절염

    // 신경계 질환
    MIGRAINE(DiseaseCategory.NEUROLOGIC),         // 편두통
    DEMENTIA(DiseaseCategory.NEUROLOGIC),         // 치매

    // 기타
    ANEMIA(DiseaseCategory.ETC),                  // 빈혈
    CANCER(DiseaseCategory.ETC),                  // 암
    ETC(DiseaseCategory.ETC);                     // 기타

    private final DiseaseCategory category;

    DiseaseName(DiseaseCategory category) {
        this.category = category;
    }

    public boolean isInCategory(DiseaseCategory category) {
        return this.category == category;
    }

}
