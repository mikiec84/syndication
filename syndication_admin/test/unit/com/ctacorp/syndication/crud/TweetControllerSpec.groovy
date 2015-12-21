package com.ctacorp.syndication.crud

import com.ctacorp.syndication.CmsManagerKeyService
import com.ctacorp.syndication.Language
import com.ctacorp.syndication.MediaItemSubscriber
import com.ctacorp.syndication.MediaItemsService
import com.ctacorp.syndication.Source
import com.ctacorp.syndication.TagService
import com.ctacorp.syndication.FeaturedMedia
import com.ctacorp.syndication.media.Collection
import com.ctacorp.syndication.media.Tweet
import com.ctacorp.syndication.social.TwitterAccount
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import solr.operations.SolrIndexingService
import spock.lang.Specification

@TestFor(TweetController)
@Mock([Tweet,TwitterAccount, MediaItemSubscriber, FeaturedMedia])
class TweetControllerSpec extends Specification {

    def mediaItemsService = Mock(MediaItemsService)
    def cmsManagerKeyService = Mock(CmsManagerKeyService)
    def solrIndexingService = Mock(SolrIndexingService)
    def tagService = Mock(TagService)

    def setup() {
        controller.mediaItemsService = mediaItemsService
        controller.mediaItemsService.metaClass.updateItemAndSubscriber = {Tweet Tweet, subId ->if(Tweet.save(flush:true)){return Tweet} else{return Tweet}}

        controller.cmsManagerKeyService = cmsManagerKeyService
        controller.solrIndexingService = solrIndexingService
        controller.tagService = tagService
        Collection.metaClass.static.findAll = {String query, java.util.Collection social  -> []}
        request.contentType = FORM_CONTENT_TYPE
    }

    def populateValidParams(params) {
        assert params != null
        //mediaItem required attributes
        params["name"] = 'someValidName'
        params["sourceUrl"] = 'http://www.example.com/jhgfjhg'
        params["language"] = new Language()
        params["source"] = new Source()
        //Tweet required attributes
        def twitterAccount = TwitterAccount.findOrSaveByAccountName("admin")
        params["account"] = twitterAccount
        params["tweetId"] = 1234
        params["messageText"] = "Tweet message we want the world to see"
        params["mediaUrl"] = null
        params["tweetDate"] = new Date()
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            1 * controller.mediaItemsService.getIndexResponse(params, Tweet) >> {[mediaItemList:Tweet.list(), mediaItemInstanceCount: Tweet.count()]}
            !model.tweetInstanceList
            model.tweetInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            1 * controller.cmsManagerKeyService.listSubscribers()
            model.tweetInstance!= null
    }

    void "Test the save action correctly persists an instance"() {
        setup:""
            populateValidParams(params)
            def tweetInstance = new Tweet(params).save(flush:true)
        when:"The save action is executed with an invalid instance"
            def invalidTweet = new Tweet()
            request.method = 'POST'
            controller.save(invalidTweet)

        then:"The create view is rendered again with the correct model"
            model.tweetInstance != null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            controller.save(tweetInstance)

        then:"A redirect is issued to the show action"
            1 * controller.solrIndexingService.inputMediaItem(tweetInstance)
            response.redirectedUrl == '/tweet/show/1'
            controller.flash.message != null
            Tweet.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        setup:""
            populateValidParams(params)
            def Tweet = new Tweet(params).save(flush:true)
            params.languageId = "1"
            params.tagTypeId = "1"

        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            1 * controller.tagService.getTagInfoForMediaShowViews(null, params) >> {[tags:[],languages:[],tagTypes:[], selectedLanguage:[], selectedTagType:[]]}
            response.status == 404

        when:"A domain instance is passed to the show action"
            controller.show(Tweet)

        then:"A model is populated containing the domain instance"
            model.tweetInstance == Tweet
            1 * controller.tagService.getTagInfoForMediaShowViews(Tweet, params) >> {[tags:[],languages:[],tagTypes:[], selectedLanguage:[], selectedTagType:[]]}
            model.tags == []
            model.languages == []
            model.tagTypes == []
            model.languageId == "1"
            model.tagTypeId == "1"
            model.selectedLanguage == []
            model.selectedTagType == []
            model.collections == []
            model.apiBaseUrl == grailsApplication.config.syndication.serverUrl + grailsApplication.config.syndication.apiPath
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def Tweet = new Tweet(params)
            controller.edit(Tweet)

        then:"A model is populated containing the domain instance"
            1 * controller.cmsManagerKeyService.listSubscribers()
            model.tweetInstance == Tweet

    }

    void "Test the update action performs an update on a valid domain instance"() {
        setup:""
            populateValidParams(params)
            def tweetInstance = new Tweet(params).save(flush:true)

        when:"Update is called for a domain instance that doesn't exist"
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/tweet/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def invalidTweet = new Tweet()
            controller.update(invalidTweet)

        then:"The edit view is rendered again with the invalid instance"
            response.redirectedUrl == '/tweet/edit'
            flash.errors != null

        when:"A valid domain instance is passed to the update action"
            response.reset()
            controller.update(tweetInstance)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/tweet/show/$tweetInstance.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.method = 'POST'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/tweet/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def tweetInstance = new Tweet(params).save(flush: true)

        then:"It exists"
            Tweet.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(tweetInstance)

        then:"The instance is deleted"
            1 * mediaItemsService.removeInvisibleMediaItemsFromUserMediaLists(tweetInstance,true)
            1 * controller.solrIndexingService.removeMediaItem(tweetInstance)
            1 * controller.mediaItemsService.delete(tweetInstance.id)
            response.redirectedUrl == '/tweet/index'
            flash.message != null
    }
}