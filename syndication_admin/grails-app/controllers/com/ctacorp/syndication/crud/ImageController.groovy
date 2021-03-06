
/*
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA) All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package com.ctacorp.syndication.crud

import com.ctacorp.syndication.Language
import grails.util.Holders
import org.springframework.web.multipart.commons.CommonsMultipartFile

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.NOT_FOUND

import com.ctacorp.syndication.FeaturedMedia
import com.ctacorp.syndication.MediaItemSubscriber
import com.ctacorp.syndication.media.Collection

import com.ctacorp.syndication.media.Image
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

@Secured(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_PUBLISHER'])
@Transactional(readOnly = true)
class ImageController {

    def mediaItemsService
    def tagService
    def cmsManagerKeyService
    def springSecurityService
    def config = Holders.config

    static allowedMethods = [save: "POST", update: "PUT", delete: "POST"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def indexResponse = mediaItemsService.getIndexResponse(params, Image)
        respond indexResponse.mediaItemList, model: [imageInstanceCount: indexResponse.mediaItemInstanceCount, mediaType:"Image"]
    }

    def show(Image imageInstance) {
        def tagData = tagService.getTagInfoForMediaShowViews(imageInstance, params)

        respond imageInstance, model:[tags:tagData.tags,
                                           languages        :tagData.languages,
                                           tagTypes         :tagData.tagTypes,
                                           languageId       :params.languageId,
                                           tagTypeId        :params.tagTypeId,
                                           selectedLanguage :tagData.selectedLanguage,
                                           selectedTagType  :tagData.selectedTagType,
                                           collections      :Collection.findAll("from Collection where ? in elements(mediaItems)", [imageInstance]),
                                           apiBaseUrl       :config?.API_SERVER_URL + config?.SYNDICATION_APIPATH,
                                           subscriber       :cmsManagerKeyService.getSubscriberById(MediaItemSubscriber.findByMediaItem(imageInstance)?.subscriberId)
        ]
    }

    def create() {
        def subscribers = cmsManagerKeyService.listSubscribers()
        Image image = new Image(params)
        image.language = Language.findByIsoCode("eng")
        respond image, model: [subscribers:subscribers, formats:["jpg", "png"]]
    }

    @Transactional
    def save(Image imageInstance) {
        if (imageInstance == null) {
            notFound()
            return
        }

        imageInstance =  mediaItemsService.updateItemAndSubscriber(imageInstance, params.long('subscriberId'))
        if(imageInstance.hasErrors()){
            flash.errors = imageInstance.errors.allErrors.collect { [message: g.message([error: it])] }
            respond imageInstance, view:'create', model:[subscribers:cmsManagerKeyService.listSubscribers(), formats:["jpg", "png"]]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'imageInstance.label', default: 'Image'), [imageInstance.name]])
                redirect imageInstance
            }
            '*' { respond imageInstance, [status: CREATED] }
        }
    }

    def edit(Image imageInstance) {
        def subscribers = cmsManagerKeyService.listSubscribers()
        respond imageInstance, model: [subscribers:subscribers, currentSubscriber:cmsManagerKeyService.getSubscriberById(MediaItemSubscriber.findByMediaItem(imageInstance)?.subscriberId), formats:["jpg", "png"]]
    }

    @Transactional
    def update(Image imageInstance) {
        if (imageInstance == null) {
            notFound()
            return
        }

        imageInstance =  mediaItemsService.updateItemAndSubscriber(imageInstance, params.long('subscriberId'))
        if(imageInstance.hasErrors()){
            flash.errors = imageInstance.errors.allErrors.collect { [message: g.message([error: it])] }
            redirect action:'edit', id:params.id
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Image.label', default: 'Image'), [imageInstance.name]])
                redirect imageInstance
            }
            '*'{ respond imageInstance, [status: OK] }
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_PUBLISHER'])
    @Transactional
    def delete(Image imageInstance) {
        if (imageInstance == null) {
            notFound()
            return
        }

        def featuredItem = FeaturedMedia.findByMediaItem(imageInstance)
        if(featuredItem){
            featuredItem.delete()
        }

        mediaItemsService.removeInvisibleMediaItemsFromUserMediaLists(imageInstance, true)
        mediaItemsService.delete(imageInstance.id)

        request.withFormat {
            form {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Image.label', default: 'Image'), [imageInstance.name]])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'imageInstance.label', default: 'Image'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
