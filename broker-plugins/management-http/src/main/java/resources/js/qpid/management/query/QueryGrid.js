/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/dom-construct",
        "dojo/json",
        "dojo/on",
        "dojo/parser",
        "dojox/html/entities",
        "dgrid/Grid",
        "dgrid/Keyboard",
        "dgrid/Selection",
        "dgrid/extensions/Pagination",
        "dgrid/extensions/ColumnResizer",
        "qpid/management/query/QueryStore",
        "dojo/keys"],
    function (declare,
              lang,
              domConstruct,
              json,
              on,
              parser,
              entities,
              Grid,
              Keyboard,
              Selection,
              Pagination,
              ColumnResizer,
              QueryStore,
              keys)
    {
        var QueryGrid = declare("qpid.management.query.QueryGrid",
            [Grid, Keyboard, Selection, Pagination, ColumnResizer],
            {
                management: null,
                controller: null,
                _store: null,
                _sort: [],
                _lastHeaders: [],

                postscript: function (args)
                {
                    this._store = new QueryStore(args);

                    var settings = lang.mixin({
                        collection: this._store,
                        rowsPerPage: 100,
                        selectionMode: 'single',
                        cellNavigation: true,
                        className: 'dgrid-autoheight',
                        pageSizeOptions: [10, 20, 30, 40, 50, 100, 1000],
                        adjustLastColumn: true
                    }, args);
                    /* initialise grid */
                    this.inherited(arguments, [settings, arguments[1]]);
                },
                postCreate: function ()
                {
                    this.inherited(arguments);
                    this.on('.dgrid-row:dblclick', lang.hitch(this, this._onRowClick));
                    this.on('.dgrid-row:keypress', lang.hitch(this, function (event)
                    {
                        if (event.keyCode === keys.ENTER)
                        {
                            this._onRowClick(event);
                        }
                    }));

                    this.on('dgrid-sort', lang.hitch(this, function (event)
                    {
                        for (var i = 0; i < this._sort.length; ++i)
                        {
                            if (this._sort[i].property == event.sort[0].property)
                            {
                                this._sort.splice(i, 1);
                                break;
                            }
                        }
                        this._sort.splice(0, 0, event.sort[0]);
                        this._updateOrderByExpression();
                    }));
                    this.on('dgrid-refresh-complete', lang.hitch(this, function ()
                    {
                        this.updateSortArrow(this._sort, true);
                    }));
                    this._store.on('queryCompleted', lang.hitch(this, function (event)
                    {
                        on.emit(this.domNode, 'queryCompleted', event);
                    }));
                },
                setCategory: function (category)
                {
                    this._store.category = category;
                },
                setParentObject: function (parentObject)
                {
                    this._store.parentObject = parentObject;
                },
                getSelect: function ()
                {
                    return this._store.selectClause;
                },
                setSelect: function (selectClause)
                {
                    this._store.selectClause = selectClause;
                },
                getWhere: function ()
                {
                    return this._store.where;
                },
                setWhere: function (whereClause)
                {
                    this._store.where = whereClause;
                },
                getOrderBy: function ()
                {
                    return this._store.orderBy;
                },
                setOrderBy: function (orderBy)
                {
                    this._store.orderBy = orderBy;
                    this._sort = [];
                },
                setUseCachedResults: function (value)
                {
                    this._store.useCachedResults = value;
                },
                setSort: function (value)
                {
                    this._sort = lang.clone(value);
                    this._store.orderBy = this._buildOrderBy(this._sort);
                },
                getSort: function ()
                {
                    return lang.clone(this._sort);
                },
                _buildOrderBy: function(sort)
                {
                    var orderByExpression = "";
                    if (sort && sort.length)
                    {
                        var orders = [];
                        for (var i = 0; i < sort.length; ++i)
                        {
                            orders.push(sort[i].property + (sort[i].descending ? " desc" : ""));
                        }
                        orderByExpression = orders.join(",");
                    }
                    return orderByExpression;
                },
                _updateOrderByExpression: function ()
                {
                    var orderByExpression = this._buildOrderBy(this._sort);
                    this._store.orderBy = orderByExpression;
                    on.emit(this.domNode, "orderByChanged", {orderBy: orderByExpression});
                },
                _onRowClick: function (event)
                {
                    var row = this.row(event);
                    var promise = this.management.get({url: "service/structure"});
                    promise.then(lang.hitch(this, function (data)
                    {
                        var findObject = function findObject(structure, parent, type)
                        {
                            var item = {
                                id: structure.id,
                                name: structure.name,
                                type: type,
                                parent: parent
                            };
                            if (item.id == row.id)
                            {
                                return item;
                            }
                            else
                            {
                                for (var fieldName in structure)
                                {
                                    var fieldValue = structure[fieldName];
                                    if (lang.isArray(fieldValue))
                                    {
                                        var fieldType = fieldName.substring(0, fieldName.length - 1);
                                        for (var i = 0; i < fieldValue.length; i++)
                                        {
                                            var object = fieldValue[i];
                                            var result = findObject(object, item, fieldType);
                                            if (result != null)
                                            {
                                                return result;
                                            }
                                        }
                                    }
                                }
                                return null;
                            }
                        };

                        var item = findObject(data, null, "broker");
                        if (item != null)
                        {
                            this.controller.show(item.type, item.name, item.parent, item.id);
                        }
                    }));
                }
            });

        return QueryGrid;
    });