# Specifying a LOD dataset for aggregation by Europeana

Cultural heritage institutions typically publish linked data that covers more resources than the cultural heritage digital objects provided to Europeana. Therefore, it is necessary that data providers make available linked data descriptions of the datasets for aggregation by Europeana.
Several vocabularies are available nowadays to describe datasets. Europeana supports three vocabularies which are suitable to fulfill the requirements for aggregation of linked data: [VoID](https://www.w3.org/TR/void/), [DCAT](https://www.w3.org/TR/vocab-dcat/), and [Schema.org](http://schema.org/Dataset).

Data providers may use classes and properties from any of the three vocabularies to describe each of their datasets. To enable Europeana to aggregate and ingest a dataset, the linked data resource of the dataset should follow the following points:
 - **Must** be accessible by its URI.
 - **Must** be encoded in RDF.
 - **Must** have a title property.
 - **Must** specify the technical mechanism that allows the dataset to be automatically harvested by Europeana.
 - **May** specify a machine readable license that applies to all metadata  

The sections bellow provide details on how of these points can be provided.

# Dataset RDF resource accessible by its URI

The description of the dataset in RDF must itself be published as linked open data. 
When ingesting the dataset in Europeana, the URI of the dataset must be provided to Europeana. It will function as the entry point for the Europeana LOD Harvester to reach all linked data descriptions of the the cultural heritage objects that belong in the dataset. 
The dataset description is used during the first ingestion of the dataset in to Europeana, and later, for incremental updates of the dataset.
The data provider must maintain the dataset description updated over time, to allow incremental updates of the dataset in Europeana. 

# Dataset resource encoded in a supported RDF format

The Europeana LOD Harvester accesses the RDF resource of the dataset by sending an HTTP request to the URI that includes the Accept header with the supported mime -types for RDF encoding. The response may use any of the supported encodings to send the RDF description of the dataset.
The following are the supported mime-types:

| Format | Mime-type | Specification
|--|--|--|
| RDF/XML | application/rdf+xml | https://www.w3.org/TR/rdf-syntax-grammar/
| JSON-LD | application/ld+json | [https://www.w3.org/TR/json-ld/](https://www.w3.org/TR/json-ld/) 
| Turtle | application/x-turtle or text/turtle | [https://www.w3.org/TR/turtle/](https://www.w3.org/TR/turtle/) |

# Title of the dataset

The RDF resource of the dataset must have a title, and the title may be provided in several languages. The titles should be provided by one of these properties: dc:title, dcterms:title,  schema:name. The language of the title should be represented in a xml:lang attribute of the title property.

# Specifying the technical mechanism for LOD harvesting

A LOD dataset for Europeana, is constituted, in its core, by RDF resources of the class edm:ProvidedCHO. In addition, a dataset contains all other resources used to describe the cultural object and aggregation metadata, as specified in the EDM (i.e. resources of types such as ore:Aggregation, edm:WebResource, edm:Agent, etc.).
All these resources will be harvested by Europeana's LOD harvester. The harvester will use the RDF description of the dataset to know which RDF resources to harvest and the mechanism to harvest them. 
Data providers may choose one of the mechanisms, typically used for LOD: 
 - Dataset distribution containing all data within the dataset.
 - Listing of the URIs of all the cultural objects’s RDF resources in the dataset.
 
The mechanism that should be applied to a LOD dataset is indicated by the data provider in the properties of the RDF description of the dataset, using any of the supported vocabularies: [VoID](https://www.w3.org/TR/void/), [DCAT](https://www.w3.org/TR/vocab-dcat/), and [Schema.org](http://schema.org/Dataset).

## Option A - Specifying a downloadable dataset distribution 
All three vocabularies are capable of representing the required information for allowing Europeana to automatically obtain a dataset by  downloading a distribution containing all data within the dataset.
The following table points to the most relevant parts of the vocabularies that specify how a dataset distribution can be represented.

| Vocabulary| Specifications parts|
|--|--|
| VoID | See section "[3.3 RDF data dumps](https://www.w3.org/TR/void/#dumps)" describing the void:dataDump property. |
| DCAT | See section "[5.4 Class: Distribution](https://www.w3.org/TR/vocab-dcat/#class-distribution)", particularly the properties dcat:downloadURL and dcat:mediaType.
| Schema.org | see the definition of the property [schema:distribution](http://schema.org/distribution) of the [schema:Dataset](http://schema.org/Dataset) class.<br> see also the class [schema:DataDownload](http://schema.org/DataDownload) and its properties [schema:contentUrl](http://schema.org/contentUrl) and [schema:encodingFormat](http://schema.org/encodingFormat) |

For the requirements of Europeana, when using dataset distributions, data providers must follow the following points:
 - The files that constitute the data dump of the dataset, must contain the RDF data encoded in one the RDF encodings suported by Europeana:  [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/), [JSON-LD](https://www.w3.org/TR/json-ld/)  or [Turtle](https://www.w3.org/TR/turtle/) 
- The files may be compressed. Currently, Europeana supports only the GZip compression algorithm.
- When using DCAT or Schema.org, the values of properties dcat:mediaType and schema:encodingFormat should only use mime-types supported by Europeana for RDF encoding: '*application/rdf+xml*', '*application/ld+son*', or '*application/x-turtle*'.
## Option B - Specifying a listing of URIs
Only the VoID](https://www.w3.org/TR/void/) vocabulary includes a property to indicate a RDF resource that lists all the resources within a dataset.
VoID defines the property void:rootResource, that may be used by Europeana data providers to provide this information. See section "[3.4 Root resources](https://www.w3.org/TR/void/#root-resource)" describing the  void:rootResource property, for the general use of the property.
For the requirements of Europeana, when using a listing of URIs, data providers must provide void:rootResource properties that contains the URI’s of the cultural objects’s. In EDM, these URI's should point to RDF resources of one of the types [ore:Aggregation](http://www.openarchives.org/ore/1.0/datamodel#Aggregation) or [edm:ProvidedCHO](http://www.europeana.eu/schemas/edm/ProvidedCHO). In Schema.org, these URI's should point to instances of [schema:CreativeWork](http://schema.org/CreativeWork) or ones of its subclasses (e.g., [schema:Paiting](http://schema.org/Painting), [schema.Book](http://schema.org/Book), [schema:Sculpture](http://schema.org/Sculpture), etc.). 
# Dataset level license
The RDF resource of the dataset may optionally indicate a license that applies to the whole dataset. If the dataset provides the licensing information, individual metadata records may still override it, by specifying a license as defined in EDM.
The license for a whole dataset should be specified in [dcterms:license](http://dublincore.org/documents/dcmi-terms/#terms-license) or [schema:license](http://schema.org/license) (with Europeana supported licenses' URIs). Following the recommendations of DCAT, the property should be applied to the Distribution of the dataset. The property may also be applied in the Dataset resource, particularly this option may be required when specifying the dataset using VoID, since no Distribution resource exists in such cases (we consider this case this issue an area of specific attention for us, and we have the issue under discussion at the [DCAT mailing list](https://lists.w3.org/Archives/Public/public-dxwg-comments/2018Apr/0001.html))

# Examples
This section contains illustrative examples of RDF descriptions of datasets, prepared accordingly to the requirements of Europeana.
## Example of a dataset available via a downloadable distribution
The next example contains a RDF description of a dataset available via a downloadable distribution. In this example, the  [DCAT](https://www.w3.org/TR/vocab-dcat/) vocabulary is used.

    <?xml version="1.0"?>
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dcat="http://www.w3.org/ns/dcat#"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="http://example.org/dataset/children_books">
          <rdf:type rdf:resource="http://www.w3.org/ns/dcat#Dataset">
          <dcterms:title>Children books</dcterms:title>
          <dcat:distribution>
            <rdf:Description rdf:about="http://example.org/dataset_distribution/children_books/">
              <rdf:type rdf:resource="http://www.w3.org/ns/dcat#Distribution">
              <dcat:downloadURL rdf:resource="http://example.org/downloads/our_dataset_2018-April.xml.gz"/>
              <dcat:mediaType>application/rdf+xml</dcat:mediaType> 
              <dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/>
            </rdf:Description>
          </dcat:distribution>
      </rdf:Description>
    </rdf:RDF>


The next example contains the description the same dataset available via a downloadable distribution. In this example, the [Schema.org](http://schema.org/Dataset) vocabulary is used.

    <?xml version="1.0"?>
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
             xmlns:schema="http://schema.org/">
      <rdf:Description rdf:about="http://example.org/dataset/children_books">
          <rdf:type rdf:resource="http://schema.org/Dataset">
          <schema:name>Children books</schema:name>
          <schema:distribution>
            <rdf:Description rdf:about="http://example.org/dataset_distribution/children_books/">
              <rdf:type rdf:resource="http://schema.org/DataDownload">
              <schema:contentUrl rdf:resource="http://example.org/downloads/our_dataset_2018-April.xml.gz"/>
              <schema:encodingFormat>application/rdf+xml</schema:encodingFormat> 
              <schema:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/>
            </rdf:Description>
          </schema:distribution>
      </rdf:Description>
    </rdf:RDF>

The next example contains the description the same dataset available via a downloadable distribution. In this example, the [VoID](https://www.w3.org/TR/void/) vocabulary is used.

    <?xml version="1.0"?>
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:void="http://rdfs.org/ns/void#"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="http://example.org/dataset/children_books">
          <rdf:type rdf:resource="http://rdfs.org/ns/void#Dataset">
          <dcterms:title>Children books</dc:title>
          <void:void:dataDump rdf:about="http://example.org/downloads/our_dataset_2018-April.xml.gz"/>
          <dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/>
      </rdf:Description>
    </rdf:RDF>

## Example of a dataset available via a listing of URIs

The next example contains a RDF description of a dataset available via a  listing of URIs. In this example, the   [VoID](https://www.w3.org/TR/void/) vocabulary is used.

    <?xml version="1.0"?>
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:void="http://rdfs.org/ns/void#"
             xmlns:dcterms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="http://example.org/dataset/children_books">
          <rdf:type rdf:resource="http://rdfs.org/ns/void#Dataset">
          <dcterms:title>Children books</dc:title>
          <dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/>
          <void:rootResource rdf:about="http://example.org/Aggregation/cho_abc"/>
          <void:rootResource rdf:about="http://example.org/Aggregation/cho_def"/>
          <void:rootResource rdf:about="http://example.org/Aggregation/cho_zyz"/>
      </rdf:Description>
    </rdf:RDF>

<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE5NDA2NzY1NzUsLTIxMTc0MjIxMDIsLT
E5ODA4NTQ5MDYsNTQwMDk0MjU2LC0xMDU1NTM5NDM2LDI3MjEw
NjU2OCwtMTE3NTk2NDIyNiw2NjE5MTQ2NTMsMTU4MDA4MTcyLC
0xNDM0OTM3NzgwLDU4NzY3MjU4MywyMTMwNDEwNTQzLDEyMDQw
NDE1MCwxMTI2MTM2NjcxLDE2ODA2NzI5ODUsLTE1NzI3OTIzNz
csLTE2NTEzODMwMTAsNDI2OTIwMjQ4LC04MzY1NzgzNjIsLTE3
OTI5NDIxODVdfQ==
-->