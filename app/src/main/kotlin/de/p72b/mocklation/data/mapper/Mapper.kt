package de.p72b.mocklation.data.mapper

interface Mapper<INPUT, OUTPUT> {
    fun map(input: INPUT): OUTPUT
}