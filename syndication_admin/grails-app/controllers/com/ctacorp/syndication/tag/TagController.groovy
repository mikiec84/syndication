package com.ctacorp.syndication.tag

import com.ctacorp.syndication.media.FAQ
import com.ctacorp.syndication.media.Html
import com.ctacorp.syndication.media.Collection
import com.ctacorp.syndication.media.Image
import com.ctacorp.syndication.media.Infographic
import com.ctacorp.syndication.media.MediaItem
import com.ctacorp.syndication.media.PDF
import com.ctacorp.syndication.media.QuestionAndAnswer
import com.ctacorp.syndication.media.Tweet
import com.ctacorp.syndication.media.Video
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_PUBLISHER'])
class TagController {
    def tagService

    def index(Long prepopulateTagId) {
        listTagHelper(prepopulateTagId)
    }

    def show(Long id, Integer max) {

        def mediaItems = []
        def total = 0
        def tag = tagService.getTag(id)
        def syndicationIdsForTag = tag?.contentItems?.collect { it.syndicationId } ?: []

        params.max = Math.min(max ?: 10, 20)

        if(syndicationIdsForTag) {
            mediaItems = MediaItem.findAllByIdInList(syndicationIdsForTag, params)
        }

        if(mediaItems) {
            total = MediaItem.countByIdInList(syndicationIdsForTag)
        }

        [tag: tag, mediaItemsList: mediaItems, total: total]
    }

    def showMediaItem(MediaItem mi) {
        switch (mi) {
            case Collection: redirect controller: "collection", action: "show", id: mi.id; break
            case FAQ: redirect controller: "FAQ", action: "show", id: mi.id; break
            case Html: redirect controller: "html", action: "show", id: mi.id; break
            case Image: redirect controller: "image", action: "show", id: mi.id; break
            case PDF: redirect controller: "PDF", action: "show", id: mi.id; break
            case Infographic: redirect controller: "infographic", action: "show", id: mi.id; break
            case QuestionAndAnswer: redirect controller: "questionAndAnswer", action: "show", id: mi.id; break
            case Tweet: redirect controller: "tweet", action: "show", id: mi.id; break
            case Video: redirect controller: "video", action: "show", id: mi.id; break
            default: log.error("Tried to show media for an unsupported type: ${mi}")
                break
        }
    }

    def search() {
        render template: "tagList", model: listTagHelper()
    }

    def create() {
        def languages = tagService.getAllActiveTagLanguages()
        def tagTypes = tagService.getTagTypes()

        [languages: languages, tagTypes: tagTypes, tagName: params.tagName]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_MANAGER'])
    def edit(Long id) {
        def languages = tagService.getAllActiveTagLanguages()
        def tagTypes = tagService.getTagTypes()
        def tag = tagService.getTag(id)
        [languages: languages, tagTypes: tagTypes, tag: tag]
    }

    def save(String name, Long tagType, Long language) {

        if(tagService.findTagsByTagName(name, [languageId: language, tagTypeId: tagType]).size() > 0) {

            flash.error = "Tag already exists"
            redirect action: 'create'
            return
        }

        def tag = tagService.createTag(name, tagType, language)

        if (tag && !tag.errors) {
            flash.message = "Tag [${params.name}] created!"
        } else {

            flash.errors = tag?.errors
            render action: 'create'
            return
        }

        redirect action: 'index', params: [prepopulateTagId: tag.id]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_MANAGER'])
    def update(Long id, String name, Long language, Long tagType) {

        if(tagService.findTagsByTagName(name, [languageId: language, tagTypeId: tagType]).size() > 0) {

            flash.error = "Tag already exists"
            redirect action: 'edit'
            return
        }

        def resp = tagService.updateTag(id, params.name, language, tagType)

        if (resp?.message == "Invalid Values") {
            flash.errors = resp.details.errors
            redirect action: 'edit', id: id
            return
        }

        flash.message = "Tag [${params.name}] Updated!"
        redirect action: 'index'
    }

    @Secured(['ROLE_ADMIN', 'ROLE_MANAGER'])
    def delete(Long id) {
        String tagName = tagService.getTag(id).name
        tagService.deleteTag(id)
        flash.message = "Tag [${tagName}] deleted."
        redirect action: 'index'
    }

    @Secured(['ROLE_ADMIN', 'ROLE_MANAGER'])
    def tagger() {
        def tagData = tagService.getTagInfoForMediaShowViews(new MediaItem(), params)

        if (!tagService.status()) {
            flash.error = message(code: 'tag.failure.UNREACHABLE')
        }
        render view: 'tagger', model: [tags            : tagData.tags,
                                       languages       : tagData.languages,
                                       tagTypes        : tagData.tagTypes,
                                       languageId      : params.languageId,
                                       tagTypeId       : params.tagTypeId,
                                       selectedLanguage: tagData.selectedLanguage,
                                       selectedTagType : tagData.selectedTagType
        ]
    }

    def tagItems(String tagIds, String mediaIds) {
        if (tagIds.isEmpty() || mediaIds.isEmpty()) {
            flash.error = "Select atleast one media Item and one Tag!"
            redirect action: "tagger", model: [successfullyTaggedItems: ""]
            return
        }
        def taggedItems = tagService.tagMultiple(tagIds, mediaIds, params)
        if (!taggedItems) {
            flash.error = "Unable to Tag media items!"
            redirect action: "tagger", model: [successfullyTaggedItems: ""]
            return
        }

        def mediaItems = MediaItem.restrictToSet(taggedItems*.syndicationId.join(",")).list()
        flash.message = "Items Successfully Tagged!"
        redirect action: "tagger", model: [successfullyTaggedItems: mediaItems]
    }

    def getTagsForSyndicationId(Long syndicationId, Long languageId, Long tagTypeId) {
        respond tagService.getTagsForSyndicationId(syndicationId, languageId, tagTypeId)
    }

    def setTags(String tagIds, Long mediaId, Long languageId, Long tagTypeId) {
        def currentTags = tagService.getTagsForSyndicationId(mediaId)
        def successfullyTaggedItem = tagService.setTags(tagIds, mediaId, params)
        MediaItem mi = MediaItem.load(successfullyTaggedItem.syndicationId)
        if (currentTags || !tagIds.isEmpty()) {
            flash.message = "Tags have been updated"
        }
        redirect controller:"mediaItem", action: "show", params:[id:mi.id, languageId: languageId, tagTypeId: tagTypeId]
    }

    // -----------------------------------------------
    // Token input & ajax actions                    |
    // -----------------------------------------------
    def mediaSearch(String q) {
        def items
        if (q.isInteger()) {
            items = MediaItem.findAllByNameIlikeOrId("${q}%", q as Long, [max: 25, order: 'asc', sort: 'name'])
        } else {
            items = MediaItem.findAllByNameIlike("${q}%", [max: 25, order: 'asc', sort: 'name'])
        }
        def resp = []
        items.each { mi ->
            resp << [id: mi.id, name: "${mi.id} - ${mi.name}"]
        }

        render resp as JSON
    }

    def tagSearch(String q) {
        def tags
        def resp = []
        if (q.isInteger()) {
            tags = tagService.findTagsByTagName(q, params)
            tags << tagService.getTag(q as Long)
        } else {
            tags = tagService.findTagsByTagName(q, params)
        }

        tags.each { tag ->
            resp << [id: tag.id, name: "${tag.name}", lowerName:tag.name.toLowerCase() as String]
        }

        if (!resp.lowerName.contains(q.toLowerCase()) && !q.isInteger()) {
            resp << [id: "'" + "${q}" + "'", name: "${q} - Create New"]
        }

        render resp as JSON
    }

    def ajaxTagList() {
        render template: "tagList", model: listTagHelper()
    }

    private Map listTagHelper(Long prepopulateTagId = null) {
        def tags
        def total
        def prepopulatedTagName
        def languages = tagService.getAllActiveTagLanguages()
        def tagTypes = tagService.getTagTypes()
        def selectedLanguage = languages.find { "${it.id}" == "${params.languageId}" }?.id ?: 1
        def selectedTagType = tagTypes.find { "${it.id}" == "${params.typeId}" }?.id ?: 1

        if (tagService.status()) {
            if (prepopulateTagId) {
                tags = [tagService.getTag(prepopulateTagId)]
                selectedLanguage = tags.language.id[0]
                selectedTagType = tags.type.id[0]
                prepopulatedTagName = tags[0].name
                total = 1
            } else if (params.tagName) { //used if column sortable is used after an ajax call
                prepopulatedTagName = params.tagName
                params["name"] = params.tagName
                def tagInfo = tagService.listTags(params)
                tags = tagInfo.tags
                total = tagInfo.total
            } else {
                params["languageId"] = selectedLanguage
                params["typeId"] = selectedTagType
                def tagInfo = tagService.listTags(params)

                tags = tagInfo?.tags
                total = tagInfo?.total
            }
        } else {
            flash.error = message(code: 'tag.failure.UNREACHABLE')
        }

        [
                tags               : tags,
                total              : total,
                prepopulatedTagName: prepopulatedTagName,
                tagName            : params.tagName,
                languages          : languages,
                tagTypes           : tagTypes,
                languageId         : params.languageId,
                typeId             : params.typeId,
                selectedLanguage   : selectedLanguage,
                selectedTagType    : selectedTagType,
        ]
    }
}