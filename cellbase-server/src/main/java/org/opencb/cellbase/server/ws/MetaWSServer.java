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

package org.opencb.cellbase.server.ws;

import io.swagger.annotations.Api;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{version}/meta")
@Produces("application/json")
@Api(value = "Meta", description = "Meta RESTful Web Services API")
public class MetaWSServer extends GenericRestWSServer {

    public MetaWSServer(@PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, uriInfo, hsr);
    }


    @GET
    @Path("/versions")
    public Response getVersion() {
        return createOkResponse(cellBaseConfiguration.getDownload(), MediaType.APPLICATION_JSON_TYPE);
    }

    @GET
    @Path("/species")
    public Response getSpecies() {
        return getAllSpecies();
    }

    /**
     * Auxiliar methods
     */
    @GET
    @Path("/{category}")
    public Response getCategory(@PathParam("category") String category) {
        if ("feature".equalsIgnoreCase(category)) {
            return createOkResponse("exon\ngene\nkaryotype\nprotein\nsnp\ntranscript");
        }
        if ("genomic".equalsIgnoreCase(category)) {
            return createOkResponse("position\nregion\nvariant");
        }
        if ("network".equalsIgnoreCase(category)) {
            return createOkResponse("pathway");
        }
        if ("regulatory".equalsIgnoreCase(category)) {
            return createOkResponse("mirna_gene\nmirna_mature\ntf");
        }
        return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
    }

    @GET
    @Path("/{species}/{category}/{subcategory}")
    public Response getSubcategory(@PathParam("species") String species, @PathParam("category") String category,
                                   @PathParam("subcategory") String subcategory) {
        return getCategory(category);
    }

    private Response getAllSpecies() {
        try {
            QueryResult queryResult = new QueryResult();
            queryResult.setId("species");
            queryResult.setDbTime(0);
            queryResult.setResult(Arrays.asList(cellBaseConfiguration.getSpecies()));
            return createOkResponse(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
