/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.mongodb.db.variation;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.db.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.core.GeneMongoDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VariationMongoDBAdaptor extends MongoDBAdaptor implements VariationDBAdaptor {

    private MongoDBCollection mongoVariationPhenotypeDBCollection2;

    private int variationChunkSize = MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE;

    private GeneMongoDBAdaptor geneMongoDBAdaptor;

    public VariationMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("variation");
        mongoVariationPhenotypeDBCollection2 = mongoDataStore.getCollection("variation_phenotype");

        geneMongoDBAdaptor = new GeneMongoDBAdaptor(species, assembly, mongoDataStore);

        logger.debug("VariationMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult first() {
        return mongoDBCollection.find(new BasicDBObject(), new QueryOptions("limit", 1));
    }

    @Override
    public QueryResult count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult stats() {
        return null;
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;
    }

    public QueryResult next(String id, QueryOptions options) {
        QueryOptions _options = new QueryOptions();
        _options.put("include", Arrays.asList("chromosome", "start", "strand"));
        QueryResult queryResult = getById(id, _options);
        if(queryResult != null && queryResult.getResult() != null) {
            DBObject gene = (DBObject)queryResult.getResult().get(0);
            String chromosome = gene.get("chromosome").toString();
//            options.put("strand", gene.get("strand").toString());
            int start = Integer.parseInt(gene.get("start").toString());
            return next(chromosome, start, options);
        }
        return null;
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return next(chromosome, position+1, options, mongoDBCollection);
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }



    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("id").is(id);
            queries.add(builder.get());
        }
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllConsequenceTypes(QueryOptions options) {
        return new QueryResult("consequenceTypes", 0, consequenceTypes.size(), consequenceTypes.size(), null, null, consequenceTypes);
    }


    @Override
    public QueryResult getByGeneId(String id, QueryOptions options) {
        return getAllByGeneIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByGeneIdList(List<String> idList, QueryOptions options) {
        int offset = 5000;
        if (options != null && options.containsKey("offset")) {
            offset = options.getInt("offset");
        }

        List<Region> regions = new ArrayList<>(idList.size());
        QueryOptions geneQueryOptions = new QueryOptions("include", "chromosome,start,end");
        for (String id : idList) {
            QueryResult queryResult = geneMongoDBAdaptor.getAllById(id, geneQueryOptions);
            if (queryResult != null && queryResult.getResult().size() > 0) {
                DBObject gene = (DBObject)geneMongoDBAdaptor.getAllById(id, geneQueryOptions).getResult().get(0);
                regions.add(new Region(gene.get("chromosome").toString(),
                        Integer.parseInt(gene.get("start").toString()) - offset,
                        Integer.parseInt(gene.get("end").toString()) + offset));

            } else {
                regions.add(new Region("", -1, -1));
            }
        }
        return getAllByRegionList(regions, options);
    }


    @Override
    public QueryResult getByTranscriptId(String id, QueryOptions options) {
        return getAllByTranscriptIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByTranscriptIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("transcriptVariations.transcriptId").is(id);
            queries.add(builder.get());
        }
        return executeQueryList2(idList, queries, options);
    }


    @Override
    public QueryResult getAllPhenotypes(QueryOptions options) {
//        return executeDistinct("distinct", "phenotype", mongoVariationPhenotypeDBCollection);
        QueryBuilder builder = new QueryBuilder();
        if(options.containsKey("phenotype")) {
            String pheno = options.getString("phenotype");
            if(pheno != null && !pheno.equals("")) {
                builder = builder.start("phenotype").is(pheno);
            }
        }
        return executeQuery("result", builder.get(), options);
//        return executeQuery("result", builder.get(), options, mongoVariationPhenotypeDBCollection);
    }

    @Override
    public List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options) {
        QueryBuilder builder = null;
        List<DBObject> queries = new ArrayList<>();

        /**
         * If source is present in options is it parsed and prepare first,
         * otherwise ti will be done for each iteration of regions.
         */
        List<Object> source = options.getList("source", null);
        BasicDBList sourceIds = new BasicDBList();
        if (source != null && source.size() > 0) {
            sourceIds.addAll(source);
        }

//        List<Region> regions = Region.parseRegions(options.getString("region"));
        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {
            if(region != null && !region.equals("")) {
                // If regions is 1 position then query can be optimize using chunks
                if (region.getStart() == region.getEnd()) {
                    String chunkId = getChunkIdPrefix(region.getChromosome(), region.getStart(), variationChunkSize);
                    System.out.println(chunkId);
                    builder = QueryBuilder.start("_chunkIds").is(chunkId).and("end")
                            .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                } else {
                    builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                            .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                }

                if (sourceIds != null && sourceIds.size() > 0) {
                    builder = builder.and("source").in(sourceIds);
                }

                queries.add(builder.get());
                ids.add(region.toString());
            }
        }
        return executeQueryList2(ids, queries, options, mongoVariationPhenotypeDBCollection2);
    }

    @Override
    public QueryResult getAllByPhenotype(String phenotype, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("phenotype").is(phenotype);

        List<QueryResult> queryResults = new ArrayList<>();
        if(options.containsKey("variants")) {
            List<Object> variantList = options.getList("variants");
            List<GenomicVariant> variants = new ArrayList<>(variantList.size());
            for (int i = 0; i < variantList.size(); i++) {
                GenomicVariant genomicVariant = (GenomicVariant) variantList.get(i);
                variants.add(genomicVariant);
            }
        }

        return null;
    }

    @Override
    public List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options) {
        return null;
    }


    @Override
    public QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("phenotype").is(phenotype);
        return executeQuery(phenotype, builder.get(), options, mongoVariationPhenotypeDBCollection2);
    }

    @Override
    public List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(phenotypeList.size());
        for (String id : phenotypeList) {
            QueryBuilder builder = QueryBuilder.start("phenotype").is(id);
            queries.add(builder.get());
        }
        return executeQueryList2(phenotypeList, queries, options, mongoVariationPhenotypeDBCollection2);
    }
    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regions.size());

        String phenotype = options.getString("phenotype");
        if(phenotype != null && !phenotype.equals("")) {
            for (Region region : regions) {
                QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
                builder = builder.and("phenotype").is(phenotype);
                queries.add(builder.get());
                ids.add(region.toString());
            }
            return executeQueryList2(ids, queries, options, mongoVariationPhenotypeDBCollection2);
        }else {
            String consequenceTypes = options.getString("consequence_type", null);
            BasicDBList consequenceTypeDBList = new BasicDBList();
            if (consequenceTypes != null && !consequenceTypes.equals("")) {
                for (String ct : consequenceTypes.split(",")) {
                    consequenceTypeDBList.add(ct);
                }
            }

            for (Region region : regions) {
                //			QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getSequenceName()).and("end").greaterThan(region.getStart()).and("start").lessThan(region.getEnd());
                QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
                if (consequenceTypeDBList.size() > 0) {
                    builder = builder.and("transcriptVariations.consequenceTypes").in(consequenceTypeDBList);
                }
                queries.add(builder.get());
                ids.add(region.toString());
            }

            return executeQueryList2(ids, queries, options);
        }
    }

    @Override
    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }

    @Override
    public List<QueryResult> getIdByVariantList(List<GenomicVariant> variations, QueryOptions options){
        List<DBObject> queries = new ArrayList<>(variations.size());
        List<QueryResult> results;

        for (GenomicVariant variation : variations) {
            String chunkId = getChunkIdPrefix(variation.getChromosome(), variation.getPosition(), variationChunkSize);
            QueryBuilder builder = QueryBuilder.start("_chunkIds").is(chunkId).and("chromosome").is(variation.getChromosome()).and("start").is(variation.getPosition()).and("alternate").is(variation.getAlternative());
            if(variation.getReference() != null){
                builder = builder.and("reference").is(variation.getReference());
            }

            queries.add(builder.get());
        }

        results = executeQueryList2(variations, queries, options, mongoDBCollection);


        for (QueryResult result: results){
            List<String> idList = new LinkedList();

            BasicDBList idListObject = (BasicDBList) result.getResult();
            for (Object idObject : idListObject) {
                DBObject variantObject = (DBObject) idObject;
                idList.add(variantObject.get("id").toString());
            }

//            result.setResult(Joiner.on(",").skipNulls().join(idList));
            result.setResult(idList);
        }

        return results;
    }

    @Override
    public List<QueryResult> getAllByVariantList(List<GenomicVariant> variations, QueryOptions options){
        List<DBObject> queries = new ArrayList<>(variations.size());
        List<QueryResult> results;

        for (GenomicVariant variation : variations) {
            String chunkId = getChunkIdPrefix(variation.getChromosome(), variation.getPosition(), variationChunkSize);

            QueryBuilder builder = QueryBuilder.start("_chunkIds").is(chunkId).and("chromosome").is(variation.getChromosome()).and("start").is(variation.getPosition()).and("alternate").is(variation.getAlternative());

            if(variation.getReference() != null){
                builder = builder.and("reference").is(variation.getReference());
            }

            queries.add(builder.get());
        }

        results = executeQueryList2(variations, queries, options, mongoDBCollection);
//        results = executeQueryList(variations, queries, options, mongoDBCollection);

        return results;
    }
}
