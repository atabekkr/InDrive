package com.aralhub.indrive.mapper

interface Mapper<in I, out O> {
    fun map(input: I): O
}