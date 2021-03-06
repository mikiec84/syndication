package com.ctacorp.syndication.marshal

import com.ctacorp.grails.swagger.annotations.Definition
import com.ctacorp.grails.swagger.annotations.DefinitionProperty
import com.ctacorp.grails.swagger.annotations.DefinitionPropertyType

/**
 * Created by nburk on 4/18/17.
 */
@Definition
class TagTypeMarshaller {
    @DefinitionProperty(name='id', type = DefinitionPropertyType.INTEGER)
    def typeId
    @DefinitionProperty(name='name', type = DefinitionPropertyType.STRING)
    def typeName
}
