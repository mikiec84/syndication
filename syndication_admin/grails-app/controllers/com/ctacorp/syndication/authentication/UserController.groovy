

/*
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package com.ctacorp.syndication.authentication

import com.ctacorp.syndication.microsite.MicrositeRegistration
import grails.converters.JSON

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.NOT_FOUND

import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

@Transactional(readOnly = true)
@Secured(["ROLE_ADMIN", "ROLE_MANAGER", "ROLE_PUBLISHER"])
class UserController {
    def adminUserService
    def springSecurityService
    def cmsManagerKeyService
    def passwordService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE", resetPassword: "POST"]

    @Secured(["ROLE_ADMIN"])
    def index(Integer max) {
        params.max = Math.min(max ?: 20, 100)
        params.sort = params.sort ?: "user.id"
        return adminUserService.indexResponse(params)
    }

    def breakdown(){
        def emails = User.list()*.username
        emails = emails.collect{(it =~ /@([\w\W]+)/)[0][1]}
        emails = emails.sort()

        def totalEmails = emails.size()

        def org = emails.findAll{it.toLowerCase().endsWith(".org")}
        def gov = emails.findAll{it.toLowerCase().endsWith(".gov")}
        def net = emails.findAll{it.toLowerCase().endsWith(".net")}
        def edu = emails.findAll{it.toLowerCase().endsWith(".edu")}
        def com = emails.findAll{it.toLowerCase().endsWith(".com")}
        def us = emails.findAll{it.toLowerCase().endsWith(".us")}
        def evElse = emails - org - gov - net - edu - com - us

        emails = emails.unique()
        def totalUniqueEmails = emails.size()

        def uorg = emails.findAll{it.toLowerCase().endsWith(".org")}
        def ugov = emails.findAll{it.toLowerCase().endsWith(".gov")}
        def unet = emails.findAll{it.toLowerCase().endsWith(".net")}
        def uedu = emails.findAll{it.toLowerCase().endsWith(".edu")}
        def ucom = emails.findAll{it.toLowerCase().endsWith(".com")}
        def uus = emails.findAll{it.toLowerCase().endsWith(".us")}
        def uevElse = emails - org - gov - net - edu - com - us

        [
                totalEmails         : totalEmails,
                totalUniqueEmailes  : totalUniqueEmails,
                org                 : org,
                gov                 : gov,
                net                 : net,
                edu                 : edu,
                com                 : com,
                us                  : us,
                evElse              : evElse,
                uorg                : uorg,
                ugov                : ugov,
                unet                : unet,
                uedu                : uedu,
                ucom                : ucom,
                uus                 : uus,
                uevElse             : uevElse
        ]
    }

    def domainDistribution(){
        def emails = User.list()*.username
        emails = emails.collect{(it =~ /@([\w\W]+)/)[0][1]}
        emails = emails.sort()

        def org = emails.findAll{it.contains(".org")}.size()
        def gov = emails.findAll{it.contains(".gov")}.size()
        def net = emails.findAll{it.contains(".net")}.size()
        def edu = emails.findAll{it.contains(".edu")}.size()
        def com = emails.findAll{it.contains(".com")}.size()
        def evElse = (emails.size() - org - gov - net - edu - com)

        def data = [
                [
                        label: "org",
                        value: org
                ],
                [
                        label: "gov",
                        value: gov
                ],
                [
                        label: "net",
                        value: net
                ],
                [
                        label: "edu",
                        value: edu
                ],
                [
                        label: "com",
                        value: com
                ],
                [
                        label: "other",
                        value: evElse
                ]
        ]
        render data as JSON
    }

    @Secured(["ROLE_ADMIN"])
    def show(User userInstance) {
        boolean allowDelete = userInstance?.id != springSecurityService.currentUser.id

        respond userInstance, model: [allowDelete: allowDelete]
    }
    
    @Secured(["ROLE_ADMIN"])
    def create() {
        def roles = Role.list()

        def subscribers = cmsManagerKeyService.listSubscribers()
        respond new User(params), model: [subscribers:subscribers, roles:roles, currentRoleId: params?.authority]
    }

    @Secured(["ROLE_ADMIN"])
    @Transactional
    def save(User userInstance) {
        if (userInstance == null) {
            notFound()
            return
        }

        def passwordValidation = passwordService.validatePassword(params.password, params.passwordRepeat)
        userInstance.validate()
        if (userInstance.hasErrors() || (Role.get(params.authority).authority == "ROLE_PUBLISHER" && !params.subscriberId) || !passwordValidation.valid) {
            flash.errors = []
            if(passwordValidation.validationMessage){
                userInstance.errors.rejectValue("password", "password does not follow guidelines", passwordValidation.validationMessage)
            }
            flash.errors = userInstance.errors.allErrors.collect{[message:g.message([error : it])]}
            if(Role.get(params.authority).authority == "ROLE_PUBLISHER" && !params.subscriberId){flash.errors << [message:"Select a Key"]}
            def roles = Role.list()
            if(UserRole.findByUser(springSecurityService.currentUser).role.authority == "ROLE_MANAGER"){
                roles = Role.findAllByAuthorityInList(adminUserService.getManagersAuthorityRoles())
            }

            respond userInstance, view:'create', model: [subscribers:cmsManagerKeyService.listSubscribers(), roles:roles, currentRoleId: params?.authority]
            return
        }

        adminUserService.saveUserAndRole(userInstance, params.long('authority'))

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'userInstance.label', default: 'User'), userInstance.id])
                redirect userInstance
            }
            '*' { respond userInstance, [status: CREATED] }
        }
    }

    @Secured(["ROLE_ADMIN"])
    def edit(User userInstance) {
        def roles = Role.list()
        def subscribers = cmsManagerKeyService.listSubscribers()
        respond userInstance, model: [subscribers:subscribers, currentSubscriber:subscribers.find{it.id as Long == userInstance?.subscriberId}?.id, roles: roles, currentRoleId:userInstance?.authorities?.getAt(0)?.id, fake:userInstance?.username]
    }

    def editMyAccount(){
        render view: "editMyAccount", model:[userInstance: springSecurityService.currentUser]
    }

    @Transactional
    def updateMyAccount(User userInstance){

        if (userInstance == null) {
            notFound()
            return
        }

        def passwordValidation = passwordService.validatePassword(params.password, params.passwordRepeat)
        userInstance.validate()
        if (userInstance.hasErrors() || !passwordValidation.valid) {

            if(passwordValidation.validationMessage){
                userInstance.errors.rejectValue("password", "password does not follow guidelines", passwordValidation.validationMessage)
            }
            flash.errors = userInstance.errors.allErrors.collect{[message:g.message([error : it])]}

            respond userInstance.errors, view: 'editMyAccount'
            transactionStatus.setRollbackOnly()
            return
        }
        userInstance.save(flush:true)

        flash.message = "Your account has been successfully updated!"
        redirect controller:"dashboard", action: "syndDash"
    }

    @Secured(["ROLE_ADMIN"])
    @Transactional
    def update(User userInstance) {
        if (userInstance == null) {
            notFound()
            return
        }

        def passwordValidation = passwordService.validatePassword(params.password, params.passwordRepeat)
        userInstance.validate()
        if (userInstance.hasErrors() || (Role.get(params.authority).authority == "ROLE_PUBLISHER" && !params.subscriberId) || !passwordValidation.valid) {
            flash.errors = []
            if(passwordValidation.validationMessage){
                userInstance.errors.rejectValue("password", "password does not follow guidelines", passwordValidation.validationMessage)
            }
            flash.errors = userInstance.errors.allErrors.collect{[message:g.message([error : it])]}
            if(Role.get(params.authority).authority == "ROLE_PUBLISHER" && !params.subscriberId){flash.errors << [message:"Select a Key"]}
            redirect action:'edit', id:userInstance.id, model: [subscribers:cmsManagerKeyService.listSubscribers()]
            transactionStatus.setRollbackOnly()
            return
        }

        adminUserService.saveUserAndRole(userInstance, params.long('authority'))

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'User.label', default: 'User'), userInstance.id])
                redirect userInstance
            }
            '*' { respond userInstance, [status: OK] }
        }
    }

    @Secured(["ROLE_ADMIN"])
    @Transactional
    def resetPassword(Long userId) {
        User user = User.get(userId)
        if(!user){
            response.status = 400
            render "Unable to resetPassword"
            return
        }
        def success = adminUserService.resetPassword(user)
        if(!success) {
            response.status = 400
            render "Unable to reset password"
            return
        }

        render "Sent password reset email to ${user}"
    }

    @Secured(["ROLE_ADMIN"])
    @Transactional
    def delete(User userInstance) {

        if (userInstance == null) {
            notFound()
            return
        }

        if(MicrositeRegistration.findByUser(userInstance)){
            MicrositeRegistration.findByUser(userInstance).delete()
        }

        adminUserService.deleteUser(userInstance)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'User.label', default: 'User'), userInstance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'userInstance.label', default: 'User'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}