<h1 id="specifying-a-linked-data-dataset-for-aggregation-by-europeana">Specifying a linked data dataset for aggregation by Europeana</h1>
<p>Cultural heritage institutions typically publish linked data that covers more resources than the cultural heritage digital objects that they provide to Europeana. Therefore, it is necessary that data providers make available linked data descriptions of the datasets for aggregation by Europeana.<br>
Several vocabularies are available nowadays to describe datasets. Europeana supports three vocabularies which are suitable to fulfill the requirements for aggregation of linked data: <a href="https://www.w3.org/TR/void/">VoID</a>, <a href="https://www.w3.org/TR/vocab-dcat/">DCAT</a>, and <a href="http://schema.org/Dataset">Schema.org</a>.</p>
<p>Data providers may use classes and properties from any of the three vocabularies to describe each of their datasets. To enable Europeana to aggregate and ingest a dataset, the linked data resource of the dataset should follow the following points:</p>
<ul>
<li><strong>Must</strong> be accessible by its URI.</li>
<li><strong>Must</strong> be encoded in RDF.</li>
<li><strong>Must</strong> have a title property.</li>
<li><strong>Must</strong> specify the technical mechanism that allows the dataset to be automatically harvested by Europeana.</li>
<li><strong>May</strong> specify a machine readable license that applies to all metadata</li>
</ul>
<p>The sections bellow provide details on how of these points can be provided.</p>
<h1 id="dataset-rdf-resource-accessible-by-its-uri">Dataset RDF resource accessible by its URI</h1>
<p>The description of the dataset in RDF must itself be published as linked data.<br>
For ingesting the dataset in Europeana, this URI must be provided to Europeana. It will function as the entry point for the Europeana linked data crawler to reach all RDF descriptions of the the cultural heritage objects that form the dataset.<br>
The dataset description is used during the first ingestion of the dataset in to Europeana, and later, for its incremental updates.<br>
The data provider should maintain the dataset description updated over time, if incremental updates of the dataset  are done in the future Europeana.</p>
<h1 id="dataset-resource-encoded-in-a-supported-rdf-serialization">Dataset resource encoded in a supported RDF serialization</h1>
<p>The Europeana LD Harvester accesses the RDF resource of the dataset by sending an HTTP request to the URI that includes the Accept header with the supported mime -types for RDF serialization. The response may use any of the supported mime-types to send the RDF description of the dataset.<br>
The following are the supported mime-types:</p>

<table>
<thead>
<tr>
<th>Format</th>
<th>Mime-type</th>
<th>Specification</th>
</tr>
</thead>
<tbody>
<tr>
<td>RDF/XML</td>
<td>application/rdf+xml</td>
<td><a href="https://www.w3.org/TR/rdf-syntax-grammar/">https://www.w3.org/TR/rdf-syntax-grammar/</a></td>
</tr>
<tr>
<td>Turtle</td>
<td>application/x-turtle or text/turtle</td>
<td><a href="https://www.w3.org/TR/turtle/">https://www.w3.org/TR/turtle/</a></td>
</tr>
<tr>
<td>Notation3 (N3)</td>
<td>text/n3</td>
<td><a href="https://www.w3.org/TeamSubmission/n3/">https://www.w3.org/TeamSubmission/n3/</a></td>
</tr>
<tr>
<td>N-Triples</td>
<td>application/n-triples</td>
<td><a href="https://www.w3.org/TR/n-triples/">https://www.w3.org/TR/n-triples/</a></td>
</tr>
<tr>
<td>JSON-LD</td>
<td>application/ld+json</td>
<td><a href="https://www.w3.org/TR/json-ld/">https://www.w3.org/TR/json-ld/</a></td>
</tr>
</tbody>
</table><h1 id="title-of-the-dataset">Title of the dataset</h1>
<p>The RDF resource of the dataset must have a title, and the title may be repeated for other languages. The titles should be provided by one of these properties: dc:title, dcterms:title,  schema:name. The language of the title should be represented in a xml:lang attribute of the title property.</p>
<h1 id="specifying-the-technical-mechanism-for-ld-harvesting">Specifying the technical mechanism for LD harvesting</h1>
<p>A LD dataset for Europeana, is formed, in its core, by RDF resources of the class edm:ProvidedCHO. In addition, a dataset contains all other resources used to describe the cultural object and aggregation metadata, as specified in the EDM (i.e. resources of types such as ore:Aggregation, edm:WebResource, edm:Agent, etc.).<br>
All these resources will be harvested by Europeana’s LD harvester. The harvester will use the RDF description of the dataset to know which RDF resources to harvest and the mechanism to harvest them.<br>
Data providers may choose one of the mechanisms, typically used for LD:</p>
<ul>
<li>Option A - Dataset distribution containing all data within the dataset.</li>
<li>Option B - Listing of the URIs of all the cultural objects’s RDF resources in the dataset.</li>
<li>Option C - SPARQL endpoint that can list the URIs of all  cultural objects’s RDF resources in the dataset.</li>
</ul>
<p>The mechanism that should be applied to a LD dataset is indicated by the data provider in the properties of the RDF description of the dataset, using any of the supported vocabularies: <a href="https://www.w3.org/TR/void/">VoID</a>, <a href="https://www.w3.org/TR/vocab-dcat/">DCAT</a>, and <a href="http://schema.org/Dataset">Schema.org</a>.<br>
The following subsections provide details on how each of these mechanisms can be specified.</p>
<h2 id="option-a---specifying-a-downloadable-dataset-distribution">Option A - Specifying a downloadable dataset distribution</h2>
<p>All three vocabularies are capable of representing the required information for allowing Europeana to automatically obtain a dataset by  downloading a distribution containing all data within the dataset.<br>
The following table points to the most relevant parts of the vocabularies that specify how a dataset distribution can be represented.</p>

<table>
<thead>
<tr>
<th>Vocabulary</th>
<th>Specifications parts</th>
</tr>
</thead>
<tbody>
<tr>
<td>VoID</td>
<td>See section “<a href="https://www.w3.org/TR/void/#dumps">3.3 RDF data dumps</a>” describing the void:dataDump property.</td>
</tr>
<tr>
<td>DCAT</td>
<td>See section “<a href="https://www.w3.org/TR/vocab-dcat/#class-distribution">5.4 Class: Distribution</a>”, particularly the properties dcat:downloadURL and dcat:mediaType.</td>
</tr>
<tr>
<td><a href="http://Schema.org">Schema.org</a></td>
<td>see the definition of the property <a href="http://schema.org/distribution">schema:distribution</a> of the <a href="http://schema.org/Dataset">schema:Dataset</a> class.<br> see also the class <a href="http://schema.org/DataDownload">schema:DataDownload</a> and its properties <a href="http://schema.org/contentUrl">schema:contentUrl</a> and <a href="http://schema.org/encodingFormat">schema:encodingFormat</a></td>
</tr>
</tbody>
</table><p>For the requirements of Europeana, when using dataset distributions, data providers must follow the following points:</p>
<ul>
<li>The files that constitute the data dump of the dataset, must contain the RDF data encoded in one the RDF encodings suported by Europeana:  <a href="https://www.w3.org/TR/rdf-syntax-grammar/">RDF/XML</a>, <a href="https://www.w3.org/TeamSubmission/n3/">Notation3</a>, <a href="https://www.w3.org/TR/n-triples/">N-Triples</a>, <a href="https://www.w3.org/TR/turtle/">Turtle</a> or <a href="https://www.w3.org/TR/json-ld/">JSON-LD</a>.</li>
<li>The files may be compressed. Currently, Europeana supports only the GZip compression algorithm.</li>
<li>When using DCAT or <a href="http://Schema.org">Schema.org</a>, the values of properties dcat:mediaType and schema:encodingFormat should only use mime-types supported by Europeana for RDF encoding: ‘<em>application/rdf+xml</em>’, ‘<em>text/n3</em>’, ‘<em>application/n-triples</em>’, ‘<em>application/x-turtle</em>’, or ‘<em>application/ld+son</em>’.</li>
</ul>
<h2 id="option-b---specifying-a-listing-of-uris">Option B - Specifying a listing of URIs</h2>
<p>Only the <a href="https://www.w3.org/TR/void/">VoID</a> vocabulary includes a property to indicate a RDF resource that lists all the resources within a dataset.<br>
VoID defines the property void:rootResource, that may be used by Europeana data providers to provide this information. See section “<a href="https://www.w3.org/TR/void/#root-resource">3.4 Root resources</a>” describing the  void:rootResource property, for the general use of the property.<br>
For the requirements of Europeana, when using a listing of URIs, data providers must provide void:rootResource properties that contains the URIs of the cultural objects.<br>
These URIs should point to RDF resources in EDM, and they should have one of the types <a href="http://www.openarchives.org/ore/1.0/datamodel#Aggregation">ore:Aggregation</a> or <a href="http://www.europeana.eu/schemas/edm/ProvidedCHO">edm:ProvidedCHO</a>.<br>
Europeana R&amp;D  is nowadays piloting with LD using <a href="http://Schema.org">Schema.org</a>. In these particular contexts, these URIs should point to instances of <a href="http://schema.org/CreativeWork">schema:CreativeWork</a> or ones of its subclasses (e.g., <a href="http://schema.org/Painting">schema:Painting</a>, <a href="http://schema.org/Book">schema:Book</a>, <a href="http://schema.org/Sculpture">schema:Sculpture</a>, etc.).</p>
<h2 id="option-c---specifying-a-sparql-service">Option C - Specifying a SPARQL Service</h2>
<p><a href="https://www.w3.org/TR/void/">VoID</a> and <a href="https://www.w3.org/TR/vocab-dcat/">DCAT</a> provide ways to specify the endpoint URL of a SPARQL service that serves the dataset. <a href="http://Schema.org">Schema.org</a>, however, is not able to accurately express all the required technical details.<br>
It is important to note that, it may not be possible to use SPARQL as the distribution mechanism in several cases. If the SPARQL endpoint serves more data than the data provider wishes to deliver to Europeana, none of the three vocabularies allows a data provider to specify a named graph or a triple pattern that would allow Europeana to select the subset to be harvested. Investigation is in progress, for ways to specify, in metadata, a subset within a SPARQL endpoint.<br>
Using VoID, the URL of the SPARQL endpoint is specified using the property <a href="https://www.w3.org/TR/void/#sparql">void:sparqlEndpoint</a> in the dataset RDF resource.<br>
When using DCAT, there are two valid options to specify the URL of the SPARQL endpoint. The simplest option is to specify the URL in the  dcat:Distribution using necessarily two properties: dcat:accessURL and dcterms:conformsTo. dcat:accessURL specifies the SPARQL endpoint URL, and dcterms:conformsTo specifies that the standard served by the dcat:accessURL is SPARQL.<br>
The second DCAT option allows more details to be provided about the SPARQL endpoint, by describing it as a dcat:DataService, and reference in the dcat:Distribution the dcat:DataService with the dcat:dataService property. The RDF resource of the dcat:DataService, must have at least two properties that provide the same information as in the first option: dcat:endpointURL and dcterms:conformsTo.<br>
Regarding the value to be used in the dcterms:conformsTo properties, the Europeana LD harvester recognizes any of the URLs from the namespaces defined in the SPARQL protocol:</p>
<ul>
<li><a href="http://www.w3.org/2005/09/sparql-protocol-types/#">http://www.w3.org/2005/09/sparql-protocol-types/#</a></li>
<li><a href="http://www.w3.org/2005/sparql-results#">http://www.w3.org/2005/sparql-results#</a></li>
<li><a href="http://www.w3.org/2005/08/sparql-protocol-query/#">http://www.w3.org/2005/08/sparql-protocol-query/#</a></li>
</ul>
<h1 id="dataset-level-license">Dataset level license</h1>
<p>The RDF resource of the dataset may optionally indicate a license that applies to the whole dataset. If the dataset provides the licensing information, individual metadata records may still override it, by specifying a license as defined in EDM.<br>
The license for a whole dataset should be specified in <a href="http://dublincore.org/documents/dcmi-terms/#terms-license">dcterms:license</a> or <a href="http://schema.org/license">schema:license</a> (with Europeana supported licenses’ URIs). Following the recommendations of DCAT, the property should be applied to the Distribution of the dataset. The property may also be applied in the Dataset resource, since this option may be required when specifying the dataset using VoID, since no Distribution resource exists in such cases.</p>
<h1 id="examples">Examples</h1>
<p>This section contains illustrative examples of RDF descriptions of datasets, prepared accordingly to the requirements of Europeana.</p>
<h2 id="example-of-a-dataset-available-via-a-downloadable-distribution">Example of a dataset available via a downloadable distribution</h2>
<p>The next example contains a RDF description of a dataset available via a downloadable distribution. In this example, the  <a href="https://www.w3.org/TR/vocab-dcat/">DCAT</a> vocabulary is used.</p>
<pre><code>&lt;?xml version="1.0"?&gt;
&lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dcat="http://www.w3.org/ns/dcat#"
         xmlns:dcterms="http://purl.org/dc/terms/"&gt;
  &lt;rdf:Description rdf:about="http://example.org/dataset/children_books"&gt;
      &lt;rdf:type rdf:resource="http://www.w3.org/ns/dcat#Dataset"&gt;
      &lt;dcterms:title&gt;Children books&lt;/dcterms:title&gt;
      &lt;dcat:distribution&gt;
        &lt;rdf:Description rdf:about="http://example.org/dataset_distribution/children_books/"&gt;
          &lt;rdf:type rdf:resource="http://www.w3.org/ns/dcat#Distribution"&gt;
          &lt;dcat:downloadURL rdf:resource="http://example.org/downloads/our_dataset_2018-April.xml.gz"/&gt;
          &lt;dcat:mediaType&gt;application/rdf+xml&lt;/dcat:mediaType&gt; 
          &lt;dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/&gt;
        &lt;/rdf:Description&gt;
      &lt;/dcat:distribution&gt;
  &lt;/rdf:Description&gt;
&lt;/rdf:RDF&gt;
</code></pre>
<p>The next example contains the description the same dataset available via a downloadable distribution. In this example, the <a href="http://schema.org/Dataset">Schema.org</a> vocabulary is used.</p>
<pre><code>&lt;?xml version="1.0"?&gt;
&lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
         xmlns:schema="http://schema.org/"&gt;
  &lt;rdf:Description rdf:about="http://example.org/dataset/children_books"&gt;
      &lt;rdf:type rdf:resource="http://schema.org/Dataset"&gt;
      &lt;schema:name&gt;Children books&lt;/schema:name&gt;
      &lt;schema:distribution&gt;
        &lt;rdf:Description rdf:about="http://example.org/dataset_distribution/children_books/"&gt;
          &lt;rdf:type rdf:resource="http://schema.org/DataDownload"&gt;
          &lt;schema:contentUrl rdf:resource="http://example.org/downloads/our_dataset_2018-April.xml.gz"/&gt;
          &lt;schema:encodingFormat&gt;application/rdf+xml&lt;/schema:encodingFormat&gt; 
          &lt;schema:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/&gt;
        &lt;/rdf:Description&gt;
      &lt;/schema:distribution&gt;
  &lt;/rdf:Description&gt;
&lt;/rdf:RDF&gt;
</code></pre>
<p>The next example contains the description the same dataset available via a downloadable distribution. In this example, the <a href="https://www.w3.org/TR/void/">VoID</a> vocabulary is used.</p>
<pre><code>&lt;?xml version="1.0"?&gt;
&lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:void="http://rdfs.org/ns/void#"
         xmlns:dcterms="http://purl.org/dc/terms/"&gt;
  &lt;rdf:Description rdf:about="http://example.org/dataset/children_books"&gt;
      &lt;rdf:type rdf:resource="http://rdfs.org/ns/void#Dataset"&gt;
      &lt;dcterms:title&gt;Children books&lt;/dc:title&gt;
      &lt;void:void:dataDump rdf:about="http://example.org/downloads/our_dataset_2018-April.xml.gz"/&gt;
      &lt;dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/&gt;
  &lt;/rdf:Description&gt;
&lt;/rdf:RDF&gt;
</code></pre>
<h2 id="example-of-a-dataset-available-via-a-listing-of-uris">Example of a dataset available via a listing of URIs</h2>
<p>The next example contains a RDF description of a dataset available via a  listing of URIs. In this example, the   <a href="https://www.w3.org/TR/void/">VoID</a> vocabulary is used.</p>
<pre><code>&lt;?xml version="1.0"?&gt;
&lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:void="http://rdfs.org/ns/void#"
         xmlns:dcterms="http://purl.org/dc/terms/"&gt;
  &lt;rdf:Description rdf:about="http://example.org/dataset/children_books"&gt;
      &lt;rdf:type rdf:resource="http://rdfs.org/ns/void#Dataset"&gt;
      &lt;dcterms:title&gt;Children books&lt;/dc:title&gt;
      &lt;dcterms:license rdf:resource="http://creativecommons.org/publicdomain/mark/1.0/"/&gt;
      &lt;void:rootResource rdf:about="http://example.org/Aggregation/cho_abc"/&gt;
      &lt;void:rootResource rdf:about="http://example.org/Aggregation/cho_def"/&gt;
      &lt;void:rootResource rdf:about="http://example.org/Aggregation/cho_zyz"/&gt;
  &lt;/rdf:Description&gt;
&lt;/rdf:RDF&gt;
</code></pre>

