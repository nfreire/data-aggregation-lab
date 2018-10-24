---


---

<h1 id="open-data-acquisition-lab">Open-Data-Acquisition-Lab</h1>
<p>Experimental project on a framework for acquisition of data from open data sources, and supporting several technologies and protocols.  The Data Aggregation Lab is a work in progress. It aims to gather information from data providers and aggregators, share experimental results, apply prototypes, and provide demonstrators.</p>
<p>Current R&amp;D activities address:</p>
<ul>
<li>Aggregation mechanisms for linked data</li>
<li>Aggregation mechanisms for IIIF based on Activity Streams 2.0</li>
<li>Aggregation mechanisms for IIIF based on IIIF Collections</li>
<li>Aggregation mechanisms for IIIF based on Sitemaps</li>
<li>Aggregation mechanisms specific for <a href="http://Schema.org">Schema.org</a> metadata (e.g. HTML Crawlers)</li>
<li>Aggregation mechanisms for the WWW (HTML meta, RDFa, RDFaLite, Microdata, etc)</li>
<li>Conversion of <a href="http://Schema.org">Schema.org</a> metadata to EDM</li>
<li>Metadata profiling</li>
</ul>
<p>Current operations available in the workbench (for data providers):</p>
<ul>
<li>Register a LOD dataset for aggregation by Europeana - Allows data providers and aggregators to send us the description of a dataset where we will experiment the LOD aggregation mechanisms.<br>
The guidelines for describing a LOD dataset for Europeana are available in <a href="https://github.com/nfreire/Open-Data-Acquisition-Framework/blob/master/opaf-documentation/SpecifyingLodDatasetForEuropeana.md">this document</a>.</li>
<li>Register a IIIF dataset for aggregation by Europeana - Allows data providers and aggregators to send us the description of a dataset where we will experiment the WWW crawling aggregation mechanisms for extracting structured data within HTML pages.<br>
IIIF datasets can be used for aggregation via several harvesting methods: <a href="http://preview.iiif.io/api/discovery/api/discovery/0.1/">IIIF Change Discovery API v0.1</a>, IIIF Collections, and Sitemaps.</li>
<li>Register a WWW dataset for aggregation by Europeana - Allows data providers and aggregators to send us the description of a dataset where we will experiment the HTML Crawling and aggregation of micro formats (RDFa, Microdata, etc) and JSON-LD (for example for <a href="http://Schema.org">Schema.org</a> data).<br>
(a sitemaps.xml file is required)</li>
</ul>
<p>Current operations available in the workbench (for Europeana aggregation):</p>
<ul>
<li>Manage and process datasets - Allows the Europeana Aggregation team to manage the registered datasets, execute harvests, metadata analysis and conversion.</li>
</ul>

