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

package org.opencb.cellbase.server.ws.genomic;

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * @author imedina
 */
@Path("/{version}/{species}/genomic/chromosome")
@Produces("application/json")
@Api(value = "Genome Sequence", description = "Genome Sequence RESTful Web Services API")
public class ChromosomeWSServer extends GenericRestWSServer {


    public ChromosomeWSServer(@PathParam("version") String version, @PathParam("species") String species,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    public Response getModel() {
        return createModelResponse(Chromosome.class);
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the chromosome objects", response = QueryResponse.class)
    public Response getChromosomesAll() {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(dbAdaptor.getGenomeInfo(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/list")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the chromosomes names", response = QueryResponse.class)
    public Response getChromosomes() {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            QueryOptions options = new QueryOptions();
            options.put("include", "chromosomes.name");
            return createOkResponse(dbAdaptor.getGenomeInfo(options));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/info")
    public Response getChromosomes(@PathParam("chromosomeName") String query) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(dbAdaptor.getAllByChromosomeIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/size")
    public Response getChromosomeSize(@PathParam("chromosomeName") String query) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            QueryOptions options = new QueryOptions();
            options.put("include", "size");
            return createOkResponse(dbAdaptor.getChromosomeById(query, options));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/cytoband")
    public Response getByChromosomeName(@PathParam("chromosomeName") String query) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(dbAdaptor.getAllCytobandsByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    public Response defaultMethod() {
        return help();
    }
    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }
}
