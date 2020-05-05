package com.vvechirko.projectsample.data.api

import com.vvechirko.projectsample.data.model.Building
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MenuApi {

    @GET("locations")
    suspend fun getBuildings(
        @QueryMap map: Map<String, @JvmSuppressWildcards Any>
    ): List<Building>

    @GET("locations/{id}")
    suspend fun getBuilding(
        @Path("id") id: String
    ): Building
}
