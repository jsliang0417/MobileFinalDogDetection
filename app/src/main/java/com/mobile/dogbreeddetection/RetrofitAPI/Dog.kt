package com.mobile.dogbreeddetection.RetrofitAPI

data class Dog(
    val breed_facts: List<BreedFact>,
    val confidence_score: String,
    val dog_type: String
)
