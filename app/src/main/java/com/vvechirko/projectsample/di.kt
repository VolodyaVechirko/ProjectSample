package com.vvechirko.projectsample

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val APP = "app"
private const val PLACE = "place"

fun App.initKoin() = startKoin {
    androidContext(applicationContext)

    modules(listOf(
        module {
            factory {
                OkHttpClient.Builder().apply {
//                    addInterceptor(HeadersInterceptor())
//                    addInterceptor(SentryInterceptor())
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().also {
                            it.level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }.build()
            }

            factory {
                GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//                    .registerTypeAdapter(Voucher::class.java, VoucherDeserializer())
//                    .registerTypeAdapter(PlaceDetails::class.java, PlaceDetailsDeserializer())
//                    .registerTypeAdapter(PlaceComplete::class.java, PlaceCompleteDeserializer())
//                    .registerTypeAdapter(PlaceResult::class.java, PlaceResultDeserializer())
                    .create()
            }

            factory(named(APP)) {
                Retrofit.Builder()
                    .client(get())
                    .addConverterFactory(GsonConverterFactory.create(get()))
                    .baseUrl(BuildConfig.BASE_API)
                    .build()
            }

            factory(named(PLACE)) {
                Retrofit.Builder()
                    .client(get())
                    .addConverterFactory(GsonConverterFactory.create(get()))
                    .baseUrl(BuildConfig.PLACE_API)
                    .build()
            }
        },
        module {
//            single { get<Retrofit>(named(APP)).create<MenuApi>() }
//            single { get<Retrofit>(named(APP)).create<OrderApi>() }
//
//            factory { get<Retrofit>(named(PLACE)).create<PlacesApi>() }
//            factory { TimeProvider() }
//
//            single {
//                Room.databaseBuilder(get(), AppDatabase::class.java, "db")
//                    .fallbackToDestructiveMigration()
//                    .build()
//            }
        },
        module {
//            factory { PlacesInteractor(get()) }
//            factory { LocationsInteractor(get(), get()) }
//            factory { CategoriesInteractor(get()) }
//            factory { PositionsInteractor(get(), get()) }
//            factory { CartInteractor(get(), get()) }
//            factory { OrderInteractor(get(), get(), get()) }
        },
        module {
//            viewModel { MainViewModel(get()) }
//            viewModel { AddressViewModel(get()) }
//            viewModel { LocationsViewModel(get()) }
//            viewModel { (locationId: String) ->
//                CategoriesViewModel(locationId, get())
//            }
//            viewModel { (locationId: String, categoryId: String) ->
//                PositionsViewModel(locationId, categoryId, get())
//            }
//            viewModel { (locationId: String, positionId: String) ->
//                PositionViewModel(locationId, positionId, get(), get())
//            }
//            viewModel { (locationId: String) ->
//                CartViewModel(locationId, get())
//            }
//            viewModel { PreparePaymentViewModel(get()) }
//            viewModel { (locationId: String) ->
//                PaymentViewModel(locationId, get(), get())
//            }
//            viewModel { WaitingViewModel(get()) }
//            viewModel { OrderDoneViewModel(get()) }
        }
    ))
}