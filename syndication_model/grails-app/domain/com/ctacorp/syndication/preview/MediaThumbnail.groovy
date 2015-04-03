package com.ctacorp.syndication.preview

import com.ctacorp.syndication.media.MediaItem

class MediaThumbnail {
    Date dateCreated
    Date lastUpdated
    byte[] imageData

    static belongsTo = [mediaItem: MediaItem]

    static constraints = {
        imageData nullable: false, maxSize: 1 * 1024 * 1024 //1MB
        mediaItem nullable: false
    }
}
