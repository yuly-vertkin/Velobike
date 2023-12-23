package ru.sitronics.velobike.data.repositories

interface ResponseDto<T> {
    fun toModel() : T
}