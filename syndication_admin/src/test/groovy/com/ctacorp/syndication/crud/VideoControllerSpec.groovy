package com.ctacorp.syndication.crud

import com.ctacorp.syndication.CmsManagerKeyService
import com.ctacorp.syndication.Language
import com.ctacorp.syndication.MediaItemSubscriber
import com.ctacorp.syndication.MediaItemsService
import com.ctacorp.syndication.Source
import com.ctacorp.syndication.TagService
import com.ctacorp.syndication.FeaturedMedia
import com.ctacorp.syndication.contentextraction.YoutubeService
import com.ctacorp.syndication.media.Collection
import com.ctacorp.syndication.media.MediaItem
import com.ctacorp.syndication.media.Video
import com.ctacorp.syndication_elasticsearch_plugin.ElasticsearchService
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import grails.buildtestdata.mixin.Build
import grails.util.Holders
import spock.lang.Specification

@TestFor(VideoController)
@Mock([Video, MediaItemSubscriber, FeaturedMedia])
@Build(Video)
class VideoControllerSpec extends Specification {

    def mediaItemsService = Mock(MediaItemsService)
    def cmsManagerKeyService = Mock(CmsManagerKeyService)
    def elasticsearchService = Mock(ElasticsearchService)
    def tagService = Mock(TagService)
    def youtubeService = Mock(YoutubeService)
    def config = Holders.config

    def setup(){
        //for all mediaItems
        controller.mediaItemsService = mediaItemsService
        //controller.mediaItemsService.metaClass.updateItemAndSubscriber = {Video video, subId ->if(video.save(flush:true)){return video} else{return video}}

        controller.cmsManagerKeyService = cmsManagerKeyService
        controller.elasticsearchService = elasticsearchService
        controller.tagService = tagService
        Collection.metaClass.static.findAll = {String query, java.util.Collection video  -> []}
        request.contentType = FORM_CONTENT_TYPE

        //video items specifically
        controller.youtubeService = youtubeService
    }

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //mediaItem required attributes
        params["name"] = 'someValidName'
        params["sourceUrl"] = 'http://www.example.com/jhgfjhg'
        params["language"] = new Language()
        params["source"] = new Source()
        //video required attributes
        params["duration"] = 500
        params
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            1 * controller.mediaItemsService.getIndexResponse(params, Video) >> {[mediaItemList:Video.list(), mediaItemInstanceCount: Video.count()]}
            !model.videoList
            model.videoInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.video!= null
            1 * controller.cmsManagerKeyService.listSubscribers()
    }

    void "Test the save action correctly persists an instance"() {
        setup:""
            controller.mediaItemsService = [updateItemAndSubscriber: { MediaItem mediaItem, Long subscriberId -> mediaItem.save(flush:true); mediaItem} ]
            populateValidParams(params)
            def video = Video.build()

        when:"The save action is executed with an invalid instance"
            Video invalidVideo = new Video()
            request.method = 'POST'
            controller.save(invalidVideo)

        then:"The create view is rendered again with the correct model"
            model.video!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            request.method = 'POST'
            controller.save(video)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/video/show/1'
            controller.flash.message != null
            Video.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        setup:""
            populateValidParams(params)
            def video = new Video(params).save(flush:true)
            params.languageId = "1"
            params.tagTypeId = "1"

        when:"The show action is executed with a null domain"
            1 * controller.tagService.getTagInfoForMediaShowViews(null, params) >> {[tags:[],languages:[],tagTypes:[], selectedLanguage:[], selectedTagType:[]]}
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            controller.show(video)

        then:"A model is populated containing the domain instance"
            1 * controller.tagService.getTagInfoForMediaShowViews(video, params) >> {[tags:[],languages:[],tagTypes:[], selectedLanguage:[], selectedTagType:[]]}
            model.video == video
            model.tags == []
            model.languages == []
            model.tagTypes == []
            model.languageId == "1"
            model.tagTypeId == "1"
            model.selectedLanguage == []
            model.selectedTagType == []
            model.collections == []
            model.apiBaseUrl == config.API_SERVER_URL + config.SYNDICATION_APIPATH
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def video = new Video(params)
            controller.edit(video)

        then:"A model is populated containing the domain instance"
            1 * controller.cmsManagerKeyService.listSubscribers()
            model.video == video
    }

    void "Test the update action performs an update on a valid domain instance"() {
        setup:""
            controller.mediaItemsService = [updateItemAndSubscriber: { MediaItem mediaItem, Long subscriberId -> mediaItem.save(flush:true); mediaItem} ]
            populateValidParams(params)
            def video = Video.build()

        when:"Update is called for a domain instance that doesn't exist"
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/video/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            Video invalidVideo = new Video()
            controller.update(invalidVideo)

        then:"The edit view is rendered again with the invalid instance"
            response.redirectedUrl == '/video/edit'
            flash.errors != null

        when:"A valid domain instance is passed to the update action"
            response.reset()
            controller.update(video)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/video/show/$video.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.method = 'POST'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/video/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def video = new Video(params).save(flush: true)

        then:"It exists"
            Video.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(video)

        then:"The instance is deleted"
            1 * mediaItemsService.removeInvisibleMediaItemsFromUserMediaLists(video,true)
            1 * controller.mediaItemsService.delete(video.id)
            response.redirectedUrl == '/video/index'
            flash.message != null
    }

    void "Test that the import action imports a valid videoUrl"() {
        setup:""
            def video = Video.build()
        when:"The import action is called with a null videoUrl"
            request.method = "POST"
            controller.importVideo(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/video/index'
            flash.message != null

        when:"A valid url is passed to the import method"
            controller.importVideo("http://www.youtube.com/watch?v=6yZkQqx1lag")

        then:
            1 * controller.youtubeService.getVideoInstanceFromUrl(_) >> video
            view == "create"

        then:"index the item"
            1 * elasticsearchService.indexMediaItem(video)
    }
}
