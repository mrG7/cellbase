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

package org.opencb.cellbase.core.db.api.core;

import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface GeneDBAdaptor extends FeatureDBAdaptor {


    QueryResult next(String id, QueryOptions options);

    QueryResult getAllById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    /**
     * This method search the given 'id' in the XRefs array
     * @param id Any possible XRef id
     * @param options
     * @return Any gene found having that Xref id
     */
    QueryResult getAllByXref(String id, QueryOptions options);

    List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options);


    QueryResult getAllBiotypes(QueryOptions options);


    QueryResult getAllTargetsByTf(String tfId, QueryOptions queryOptions);

    List<QueryResult> getAllTargetsByTfList(List<String> tfIdList, QueryOptions queryOptions);


//	QueryResult getAllByTfName(String tfName);
//	List<QueryResult> getAllByTfNameList(List<String> tfNameList);

//	public List<Gene> getAllByMiRnaMature(String mirbaseId);
//	public List<List<Gene>> getAllByMiRnaMatureList(List<String> mirbaseIds);

}
