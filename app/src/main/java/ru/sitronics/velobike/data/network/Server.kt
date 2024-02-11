package ru.sitronics.velobike.data.network

import ru.sitronics.velobike.BuildConfig

const val REGISTER_URL = "${BuildConfig.BASE_URL_OLD}/profile/register"//?source=mobile_app"
const val GET_PROFILE_URL = "${BuildConfig.BASE_URL_OLD}/v2/profile"
const val TARIFFS_URL = "${BuildConfig.BASE_URL_OLD}/billing/tariffs"
const val TARIFF_URL = "${BuildConfig.BASE_URL_OLD}/billing/tariffs/{tariffId}"
const val GET_CARDS_URL = "${BuildConfig.BASE_URL_OLD}/billing/list_cards?source=mobile"
const val PAY_TARIFF_URL = "${BuildConfig.BASE_URL_OLD}/billing/uniteller_initialization_fiscal_recurrent"
