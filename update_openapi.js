const fs = require('fs');

const openapiPath = './openapi.json';
let doc = JSON.parse(fs.readFileSync(openapiPath, 'utf8'));

// Common userId parameter
const userIdParam = {
    "name": "userId",
    "in": "query",
    "required": true,
    "schema": {
        "type": "integer",
        "format": "int64"
    }
};

const newPaths = {
    "/api/sop/template/create": {
        "post": {
            "tags": ["SOP"],
            "summary": "Create a new SOP template",
            "parameters": [userIdParam],
            "requestBody": {
                "required": true,
                "content": {
                    "application/json": {
                        "schema": {
                            "type": "object",
                            "properties": {
                                "name": { "type": "string" }
                            }
                        }
                    }
                }
            },
            "responses": { "200": { "description": "Returns templateId" } }
        }
    },
    "/api/sop/template/list": {
        "get": {
            "tags": ["SOP"],
            "summary": "List SOP templates",
            "parameters": [userIdParam],
            "responses": { "200": { "description": "List of templates" } }
        }
    },
    "/api/sop/template/detail/{templateId}": {
        "get": {
            "tags": ["SOP"],
            "summary": "Get template detail with nested categories and items",
            "parameters": [
                userIdParam,
                { "name": "templateId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "responses": { "200": { "description": "Template details" } }
        }
    },
    "/api/sop/template/update/{templateId}": {
        "put": {
            "tags": ["SOP"],
            "summary": "Update template name",
            "parameters": [
                userIdParam,
                { "name": "templateId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "requestBody": {
                "required": true,
                "content": { "application/json": { "schema": { "type": "object", "properties": { "name": { "type": "string" } } } } }
            },
            "responses": { "200": { "description": "Success" } }
        }
    },
    "/api/sop/template/delete/{templateId}": {
        "delete": {
            "tags": ["SOP"],
            "summary": "Delete template and all contents",
            "parameters": [
                userIdParam,
                { "name": "templateId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "responses": { "200": { "description": "Success" } }
        }
    },
    "/api/sop/category/create": {
        "post": {
            "tags": ["SOP Category"],
            "summary": "Create a category",
            "parameters": [userIdParam],
            "requestBody": {
                "required": true,
                "content": {
                    "application/json": {
                        "schema": {
                            "type": "object",
                            "properties": {
                                "templateId": { "type": "integer" },
                                "name": { "type": "string" },
                                "type": { "type": "string" }
                            }
                        }
                    }
                }
            },
            "responses": { "200": { "description": "Returns categoryId" } }
        }
    },
    "/api/sop/category/update/{categoryId}": {
        "put": {
            "tags": ["SOP Category"],
            "summary": "Update category name",
            "parameters": [
                userIdParam,
                { "name": "categoryId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "requestBody": {
                "required": true,
                "content": { "application/json": { "schema": { "type": "object", "properties": { "name": { "type": "string" } } } } }
            },
            "responses": { "200": { "description": "Success" } }
        }
    },
    "/api/sop/category/delete/{categoryId}": {
        "delete": {
            "tags": ["SOP Category"],
            "summary": "Delete category and its items",
            "parameters": [
                userIdParam,
                { "name": "categoryId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "responses": { "200": { "description": "Success" } }
        }
    },
    "/api/sop/item/create": {
        "post": {
            "tags": ["SOP Item"],
            "summary": "Create an item",
            "parameters": [userIdParam],
            "requestBody": {
                "required": true,
                "content": {
                    "application/json": {
                        "schema": {
                            "type": "object",
                            "properties": {
                                "categoryId": { "type": "integer" },
                                "itemKey": { "type": "string" },
                                "itemValue": { "type": "string" }
                            }
                        }
                    }
                }
            },
            "responses": { "200": { "description": "Returns itemId" } }
        }
    },
    "/api/sop/item/update/{itemId}": {
        "put": {
            "tags": ["SOP Item"],
            "summary": "Update item contents",
            "parameters": [
                userIdParam,
                { "name": "itemId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "requestBody": {
                "required": true,
                "content": {
                    "application/json": {
                        "schema": {
                            "type": "object",
                            "properties": {
                                "itemKey": { "type": "string" },
                                "itemValue": { "type": "string" }
                            }
                        }
                    }
                }
            },
            "responses": { "200": { "description": "Success" } }
        }
    },
    "/api/sop/item/delete/{itemId}": {
        "delete": {
            "tags": ["SOP Item"],
            "summary": "Delete an item",
            "parameters": [
                userIdParam,
                { "name": "itemId", "in": "path", "required": true, "schema": { "type": "integer", "format": "int64" } }
            ],
            "responses": { "200": { "description": "Success" } }
        }
    }
};

Object.assign(doc.paths, newPaths);

fs.writeFileSync(openapiPath, JSON.stringify(doc, null, 2));
console.log('openapi.json updated successfully.');
