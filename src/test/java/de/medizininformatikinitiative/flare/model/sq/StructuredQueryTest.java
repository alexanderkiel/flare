package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_THAN;
import static org.assertj.core.api.Assertions.assertThat;

class StructuredQueryTest {

    public static final TermCode UNIT = new TermCode("http://unitsofmeasure.org", "g/dL", "g/dL");
    public static final TermCode UNIT_WITH_OTHER_SYSTEM = new TermCode("someOtherUnitSystem", "ug/dL", "ug/dL");
    private static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71",
                                                    "Malignant neoplasm of brain");

    @Test
    void deserializeJson() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ]
                      }
                    ]
                  ]
                }
                """;

        var query = new ObjectMapper().readValue(s, StructuredQuery.class);

        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71))))));
    }


    @Test
    void deserializeJson_withQuantityComparatorWithoutUnit() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                               "selectedConcepts": [],
                               "type": "quantity-comparator",
                               "value": 1,
                               "comparator": "gt"
                            }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = ComparatorFilterPart.of(GREATER_THAN, BigDecimal.valueOf(1));
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }

    @Test
    void deserializeJson_withQuantityComparatorWithUnitWithoutSystem() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                               "selectedConcepts": [],
                               "type": "quantity-comparator",
                               "unit": {
                                 "code": "g/dL",
                                 "display": "g/dL"
                               },
                               "value": 1,
                               "comparator": "gt"
                            }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = ComparatorFilterPart.of(GREATER_THAN, BigDecimal.valueOf(1), UNIT);
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }

    @Test
    void deserializeJson_withQuantityComparatorWithUnitWithSystem() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                               "selectedConcepts": [],
                               "type": "quantity-comparator",
                               "unit": {
                                 "code": "g/dL",
                                 "display": "g/dL",
                                 "system" : "someOtherUnitSystem"
                               },
                               "value": 1,
                               "comparator": "gt"
                            }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = ComparatorFilterPart.of(GREATER_THAN, BigDecimal.valueOf(1), UNIT_WITH_OTHER_SYSTEM);
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }


    @Test
    void deserializeJson_withQuantityRangeWithoutUnit() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                                    "selectedConcepts": [],
                                    "type": "quantity-range",
                                    "minValue": 3,
                                    "maxValue": 4
                            }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = RangeFilterPart.of(BigDecimal.valueOf(3), BigDecimal.valueOf(4));
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }

    @Test
    void deserializeJson_withQuantityRangeWithoutSystem() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                                    "selectedConcepts": [],
                                    "type": "quantity-range",
                                    "unit": {
                                      "code": "ug/dL",
                                      "display": "ug/dL"
                                    },
                                    "minValue": 3,
                                    "maxValue": 4
                            }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = RangeFilterPart.of(BigDecimal.valueOf(3), BigDecimal.valueOf(4), UNIT);
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }

    @Test
    void deserializeJson_withQuantityRangeWithSystem() throws JsonProcessingException {
        var s = """
                {
                  "inclusionCriteria": [
                    [
                      {
                        "termCodes": [
                          {
                            "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                            "code": "C71",
                            "display": "Malignant neoplasm of brain"
                          }
                        ],
                             "valueFilter": {
                                    "selectedConcepts": [],
                                    "type": "quantity-range",
                                    "unit": {
                                      "code": "ug/dL",
                                      "display": "ug/dL",
                                      "system" : "someOtherUnitSystem"
                                    },
                                    "minValue": 3,
                                    "maxValue": 4
                             }
                      }
                    ]
                  ]
                }
                """;
        var query = new ObjectMapper().readValue(s, StructuredQuery.class);
        FilterPart filterPart = RangeFilterPart.of(BigDecimal.valueOf(3), BigDecimal.valueOf(4), UNIT_WITH_OTHER_SYSTEM);
        assertThat(query).isEqualTo(StructuredQuery.of(CriterionGroup.of(CriterionGroup.of(Criterion.of(Concept.of(C71),
                                                                                                        new ValueFilter(filterPart))))));
    }
}
