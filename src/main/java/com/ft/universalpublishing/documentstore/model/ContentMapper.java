package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.AlternativeTitles;
import com.ft.universalpublishing.documentstore.model.read.Comments;
import com.ft.universalpublishing.documentstore.model.read.Content.Builder;
import com.ft.universalpublishing.documentstore.model.read.Copyright;
import com.ft.universalpublishing.documentstore.model.read.Uri;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import org.joda.time.DateTime;

import java.util.TreeSet;
import java.util.stream.Collectors;

public class ContentMapper {

    public static final String THING = "http://www.ft.com/thing/";

    private final IdentifierMapper identifierMapper;
    private final TypeResolver typeResolver;
    private final BrandsMapper brandsMapper;
    private final StandoutMapper standoutMapper;
    private final String apiUrlPrefix;

    public ContentMapper(final IdentifierMapper identifierMapper,
                         final TypeResolver typeResolver,
                         final BrandsMapper brandsMapper,
                         final StandoutMapper standoutMapper,
                         final String apiHost) {
        this.identifierMapper = identifierMapper;
        this.typeResolver = typeResolver;
        this.brandsMapper = brandsMapper;
        this.standoutMapper = standoutMapper;
        this.apiUrlPrefix = "http://" + apiHost + "/content/";
    }

    public com.ft.universalpublishing.documentstore.model.read.Content map(final Content source) {
        Builder builder = new Builder()
                .withId(THING + source.getUuid())
                .withTitle(source.getTitle())
                .withType(typeResolver.resolveType(source))
                .withTitle(source.getTitle())
                .withDescription(source.getDescription())
                .withBodyXml(source.getBody())
                .withOpeningXml(source.getOpening())
                .withByline(source.getByline())
                .withPublishedDate(new DateTime(source.getPublishedDate().getTime()))
                .withRequestUrl(apiUrlPrefix + source.getUuid())
                .withBinaryUrl(source.getExternalBinaryUrl())
                .withBrands(brandsMapper.map(source.getBrands()))
                .withRealtime(source.isRealtime())
                .withPublishReference(source.getPublishReference())
                .withPixelWidth(source.getPixelWidth())
                .withPixelHeight(source.getPixelHeight())
                .withStandout(standoutMapper.map(source.getStandout()))
                .withStandfirst(source.getStandfirst());


        if (source.getLastModified() != null) {
            builder = builder.withLastModifiedDate(new DateTime(source.getLastModified().getTime()));
        }

        if (source.getIdentifiers() != null) {
            builder = builder.withIdentifiers(source.getIdentifiers().stream()
                    .map(identifierMapper::map)
                    .collect(Collectors.toCollection(TreeSet::new)));
        }
        if (source.getMembers() != null) {
            builder = builder.withMembers(source.getMembers().stream()
                    .map(member -> new Uri(apiUrlPrefix + member.getUuid()))
                    .collect(Collectors.toCollection(TreeSet::new)));
        }
        if (source.getMainImage() != null) {
            builder = builder.withMainImage(new Uri(apiUrlPrefix + source.getMainImage()));
        }
        if (source.getComments() != null) {
            builder = builder.withComments(new Comments(source.getComments().isEnabled()));
        }

        if(source.getCopyright() != null) {
            builder.withCopyright(new Copyright(source.getCopyright().getNotice()));
        }
        
        if(source.getAlternativeTitles()!=null){
        	builder.withAlternativeTitles(AlternativeTitles.builder().withPromotionalTitle(source.getAlternativeTitles().getPromotionalTitle()).build());
        }
        	
        

        return builder.build();
    }
}
