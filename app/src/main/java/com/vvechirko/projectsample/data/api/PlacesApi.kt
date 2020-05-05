package com.vvechirko.projectsample.data.api

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface PlacesApi {

    //    https://maps.googleapis.com/maps/api/place/autocomplete/json
    //    ?key=xxx
    //    &language=uk-UA
    //    &input=наукова%207
    //    &types=address
    //    &location=49.8397,24.0297
    //    &radius=20000
    //    &strictbounds
    //    &components=country%3Aua
    //    &sessiontoken=3928d3a0-3644-494b-b931-41978c715c8a
    @GET("place/autocomplete/json")
    suspend fun autoComplete(
        @QueryMap map: Map<String, @JvmSuppressWildcards Any>
    ): CompleteResult


    //    https://maps.googleapis.com/maps/api/place/details/json
    //    ?key=xxx
    //    &language=uk-UA
    //    &placeid=ChIJjYmEzb7nOkcRRkj1OqSE4KQ
    //    &fields=formatted_address%2Cplace_id%2Cgeometry%2Flocation%2Cname
    //    &sessiontoken=3928d3a0-3644-494b-b931-41978c715c8a
    @GET("place/details/json")
    suspend fun placeDetails(
        @QueryMap map: Map<String, @JvmSuppressWildcards Any>
    ): PlaceResult


    //    https://maps.googleapis.com/maps/api/geocode/json
    //    &key=xxx
    //    ?latlng=49.81001450,23.98350260
    //    &result_type=street_address
    @GET("geocode/json")
    suspend fun geocodeLocation(
        @QueryMap map: Map<String, @JvmSuppressWildcards Any>
    ): GeocodeResult
}