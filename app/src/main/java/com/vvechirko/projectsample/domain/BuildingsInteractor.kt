package com.vvechirko.projectsample.domain

import com.vvechirko.projectsample.data.api.MenuApi
import com.vvechirko.projectsample.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BuildingsInteractor(
    private val api: MenuApi,
    private val db: AppDatabase
) {
        suspend fun get() = withContext(Dispatchers.IO) {
            // clear all tables besides orders
//            db.cartDao.clear()
//            db.positionsDao.clear()
//            db.locationsDao.clear()

            api.getBuildings(mapOf("page" to 1)).also {
//                db.locationsDao.insert(transform(it))
            }
        }
}