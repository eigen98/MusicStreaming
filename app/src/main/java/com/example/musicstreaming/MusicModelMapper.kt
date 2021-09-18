package com.example.musicstreaming

import com.example.musicstreaming.service.MusicDto
import com.example.musicstreaming.service.MusicEntity

fun MusicEntity.mapper(id : Long) : MusicModel =


    MusicModel(
        id = id,
        streamUrl = this.streamUrl,
        coverUrl = this.coverUrl,
        track = this.track,
        artist = this.artist
    )

