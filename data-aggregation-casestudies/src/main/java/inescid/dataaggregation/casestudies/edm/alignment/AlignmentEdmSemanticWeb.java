package inescid.dataaggregation.casestudies.edm.alignment;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ebucore;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Foaf;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.data.model.Svcs;
import inescid.dataaggregation.data.model.Wgs84;

public class AlignmentEdmSemanticWeb {

	public static Alignment create() {
		Alignment alig=new Alignment();
		ClassAlignment agentCls = alig.addClass(Edm.Agent.getURI());
		ClassAlignment placeCls = alig.addClass(Edm.Place.getURI());
		ClassAlignment timespanCls = alig.addClass(Edm.TimeSpan.getURI());
		ClassAlignment conceptCls = alig.addClass(Skos.Concept.getURI());
		ClassAlignment webResourceCls = alig.addClass(Edm.WebResource.getURI());
		ClassAlignment providedChoCls = alig.addClass(Edm.ProvidedCHO.getURI());
		ClassAlignment aggregationCls = alig.addClass(Ore.Aggregation.getURI());
		
		addSchemaorgEquivalentClasses(alig);
		
		PropertyAlignment creatorProp=new PropertyAlignment(Dc.creator, ValueType.ResourceOrLiteral);
		creatorProp.addAlignment(DcTerms.creator);
		creatorProp.addRangeClass(Edm.Agent);

		PropertyAlignment contributorProp=new PropertyAlignment(Dc.contributor, ValueType.ResourceOrLiteral);
		contributorProp.addAlignment(DcTerms.contributor);
		contributorProp.addRangeClass(Edm.Agent);
		
		PropertyAlignment coverageProp=new PropertyAlignment(Dc.coverage, ValueType.ResourceOrLiteral);
		coverageProp.addAlignment(DcTerms.coverage);
		coverageProp.addRangeClass(Edm.Agent);
		coverageProp.addRangeClass(Edm.Place);
		
		PropertyAlignment dateProp=new PropertyAlignment(Dc.date, ValueType.ResourceOrLiteral);
		dateProp.addAlignment(DcTerms.date);
		dateProp.addRangeClass(Edm.TimeSpan);
		
		PropertyAlignment descriptionProp=new PropertyAlignment(Dc.description, ValueType.ResourceOrLiteral);
		descriptionProp.addAlignment(DcTerms.description);
		
		PropertyAlignment formatProp=new PropertyAlignment(Dc.format, ValueType.ResourceOrLiteral);
		formatProp.addAlignment(DcTerms.format);

		PropertyAlignment identifierProp=new PropertyAlignment(Dc.identifier, ValueType.Literal);
		identifierProp.addAlignment(DcTerms.identifier);
		
		PropertyAlignment languageProp=new PropertyAlignment(Dc.language, ValueType.Literal);
		languageProp.addAlignment(DcTerms.language);
		
		PropertyAlignment publisherProp=new PropertyAlignment(Dc.publisher, ValueType.ResourceOrLiteral);
		publisherProp.addAlignment(DcTerms.publisher);
		publisherProp.addRangeClass(Edm.Agent);
		
		PropertyAlignment relationProp=new PropertyAlignment(Dc.relation, ValueType.ResourceOrLiteral);
		relationProp.addAlignment(DcTerms.relation);
		
		PropertyAlignment rightsProp=new PropertyAlignment(Dc.rights, ValueType.ResourceOrLiteral);
		rightsProp.addAlignment(DcTerms.rights);
		
		PropertyAlignment sourceProp=new PropertyAlignment(Dc.source, ValueType.ResourceOrLiteral);
		sourceProp.addAlignment(DcTerms.source);
		
		PropertyAlignment subjectProp=new PropertyAlignment(Dc.subject, ValueType.ResourceOrLiteral);
		subjectProp.addAlignment(DcTerms.subject);
		dateProp.addRangeClass(Edm.TimeSpan);
		dateProp.addRangeClass(Edm.Agent);
		dateProp.addRangeClass(Edm.Place);
		dateProp.addRangeClass(Skos.Concept);
		
		PropertyAlignment titleProp=new PropertyAlignment(Dc.title, ValueType.Literal);
		titleProp.addAlignment(DcTerms.title);
		
		PropertyAlignment typeProp=new PropertyAlignment(Dc.type, ValueType.ResourceOrLiteral);
		typeProp.addAlignment(DcTerms.type);
		
		//*** DCTERMS ***
		PropertyAlignment alternativeProp=new PropertyAlignment(DcTerms.alternative, ValueType.Literal);
		PropertyAlignment conformsToProp=new PropertyAlignment(DcTerms.conformsTo, ValueType.ResourceOrLiteral);
		PropertyAlignment createdProp=new PropertyAlignment(DcTerms.created, ValueType.ResourceOrLiteral);
		PropertyAlignment extentProp=new PropertyAlignment(DcTerms.extent, ValueType.ResourceOrLiteral);
		PropertyAlignment hasFormatProp=new PropertyAlignment(DcTerms.hasFormat, ValueType.ResourceOrLiteral);
		PropertyAlignment hasPartProp=new PropertyAlignment(DcTerms.hasPart, ValueType.ResourceOrLiteral);
		PropertyAlignment hasVersionProp=new PropertyAlignment(DcTerms.hasVersion, ValueType.ResourceOrLiteral);
		PropertyAlignment isFormatOfProp=new PropertyAlignment(DcTerms.isFormatOf, ValueType.ResourceOrLiteral);
		PropertyAlignment isPartOfProp=new PropertyAlignment(DcTerms.isPartOf, ValueType.ResourceOrLiteral);
		PropertyAlignment isReferencedByProp=new PropertyAlignment(DcTerms.isReferencedBy, ValueType.ResourceOrLiteral);
		PropertyAlignment isReplacedByProp=new PropertyAlignment(DcTerms.isReplacedBy, ValueType.ResourceOrLiteral);
		PropertyAlignment isRequiredByProp=new PropertyAlignment(DcTerms.isRequiredBy, ValueType.ResourceOrLiteral);
		PropertyAlignment issuedProp=new PropertyAlignment(DcTerms.issued, ValueType.ResourceOrLiteral);
		PropertyAlignment isVersionOfProp=new PropertyAlignment(DcTerms.isVersionOf, ValueType.ResourceOrLiteral);
		PropertyAlignment mediumProp=new PropertyAlignment(DcTerms.medium, ValueType.ResourceOrLiteral);
		PropertyAlignment provenanceProp=new PropertyAlignment(DcTerms.provenance, ValueType.ResourceOrLiteral);
		PropertyAlignment referencesProp=new PropertyAlignment(DcTerms.references, ValueType.ResourceOrLiteral);
		PropertyAlignment replacesProp=new PropertyAlignment(DcTerms.replaces, ValueType.ResourceOrLiteral);
		PropertyAlignment requiresProp=new PropertyAlignment(DcTerms.requires, ValueType.ResourceOrLiteral);
		PropertyAlignment spatialProp=new PropertyAlignment(DcTerms.spatial, ValueType.ResourceOrLiteral);
		PropertyAlignment tableOfContentsProp=new PropertyAlignment(DcTerms.tableOfContents, ValueType.ResourceOrLiteral);
		PropertyAlignment temporalProp=new PropertyAlignment(DcTerms.temporal, ValueType.ResourceOrLiteral);
		
		agentCls.addProperty(Skos.prefLabel, ValueType.Literal);
		agentCls.addProperty(Skos.altLabel, ValueType.Literal);
		agentCls.addProperty(Skos.note, ValueType.Literal);
		agentCls.addProperty(dateProp);
		agentCls.addProperty(identifierProp);
		agentCls.addProperty(hasPartProp.copyTo(ValueType.UriOrLiteral));
		agentCls.addProperty(isPartOfProp.copyTo(ValueType.UriOrLiteral));
		agentCls.addProperty(Edm.begin, ValueType.Literal);
		agentCls.addProperty(Edm.end, ValueType.Literal);
		agentCls.addProperty(Edm.hasMet, ValueType.Resource);
		agentCls.addProperty(Edm.isRelatedTo, ValueType.UriOrLiteral);
		agentCls.addProperty(Foaf.name, ValueType.Literal);
		agentCls.addProperty(RdaGr2.biographicalInformation, ValueType.Literal);
		agentCls.addProperty(RdaGr2.dateOfBirth, ValueType.Literal);
		agentCls.addProperty(RdaGr2.dateOfDeath, ValueType.Literal);
		agentCls.addProperty(RdaGr2.dateOfEstablishment, ValueType.Literal);
		agentCls.addProperty(RdaGr2.dateOfTermination, ValueType.Literal);
		agentCls.addProperty(RdaGr2.placeOfDeath, ValueType.Literal);
		agentCls.addProperty(RdaGr2.placeOfBirth, ValueType.Literal);
		agentCls.addProperty(RdaGr2.professionOrOccupation, ValueType.Literal);
		agentCls.addProperty(RdaGr2.gender, ValueType.Literal);
		agentCls.addProperty(Owl.sameAs, ValueType.Uri);
		
		placeCls.addProperty(Skos.prefLabel, ValueType.Literal);
		placeCls.addProperty(Skos.altLabel, ValueType.Literal);
		placeCls.addProperty(Skos.note, ValueType.Literal);
		placeCls.addProperty(Wgs84.lat, ValueType.Literal);
		placeCls.addProperty(Wgs84.long_, ValueType.Literal);
		placeCls.addProperty(Wgs84.alt, ValueType.Literal);
		placeCls.addProperty(hasPartProp.copyTo(ValueType.UriOrLiteral));
		placeCls.addProperty(isPartOfProp.copyTo(ValueType.UriOrLiteral));
		placeCls.addProperty(Edm.isNextInSequence, ValueType.Uri);
		placeCls.addProperty(Owl.sameAs, ValueType.Uri);

		timespanCls.addProperty(Skos.prefLabel, ValueType.Literal);
		timespanCls.addProperty(Skos.altLabel, ValueType.Literal);
		timespanCls.addProperty(Skos.note, ValueType.Literal);
		timespanCls.addProperty(Edm.begin, ValueType.Literal);
		timespanCls.addProperty(Edm.end, ValueType.Literal);
		timespanCls.addProperty(hasPartProp.copyTo(ValueType.UriOrLiteral));
		timespanCls.addProperty(isPartOfProp.copyTo(ValueType.UriOrLiteral));
		timespanCls.addProperty(Edm.isNextInSequence, ValueType.Uri);
		timespanCls.addProperty(Owl.sameAs, ValueType.Uri);

		conceptCls.addProperty(Skos.prefLabel, ValueType.Literal);
		conceptCls.addProperty(Skos.altLabel, ValueType.Literal);
		conceptCls.addProperty(Skos.note, ValueType.Literal);
		conceptCls.addProperty(Skos.broader, ValueType.Uri);
		conceptCls.addProperty(Skos.narrower, ValueType.Uri);
		conceptCls.addProperty(Skos.related, ValueType.Uri);
		conceptCls.addProperty(Skos.broadMatch, ValueType.Uri);
		conceptCls.addProperty(Skos.narrowMatch, ValueType.Uri);
		conceptCls.addProperty(Skos.relatedMatch, ValueType.Uri);
		conceptCls.addProperty(Skos.exactMatch, ValueType.Uri);
		conceptCls.addProperty(Skos.closeMatch, ValueType.Uri);
		conceptCls.addProperty(Skos.notation, ValueType.Literal);
		conceptCls.addProperty(Skos.inScheme, ValueType.Uri);
		conceptCls.addProperty(Skos.note, ValueType.Literal);

		webResourceCls.addProperty(Skos.note, ValueType.Literal);
		webResourceCls.addProperty(creatorProp);
		webResourceCls.addProperty(descriptionProp);
		webResourceCls.addProperty(formatProp);
		webResourceCls.addProperty(rightsProp);
		webResourceCls.addProperty(sourceProp);
		webResourceCls.addProperty(typeProp);
		webResourceCls.addProperty(conformsToProp);
		webResourceCls.addProperty(createdProp);
		webResourceCls.addProperty(extentProp);
		webResourceCls.addProperty(hasPartProp);
		webResourceCls.addProperty(isPartOfProp);
		webResourceCls.addProperty(isFormatOfProp);
		webResourceCls.addProperty(issuedProp);
		webResourceCls.addProperty(isReferencedByProp);
		webResourceCls.addProperty(Edm.isNextInSequence, ValueType.Resource).addRangeClass(Edm.WebResource);
		webResourceCls.addProperty(Edm.rights, ValueType.ResourceOrUri);
		webResourceCls.addProperty(Owl.sameAs, ValueType.Uri);
		webResourceCls.addProperty(Rdf.type, ValueType.Uri);
		webResourceCls.addProperty(Edm.codecName, ValueType.Literal);
		webResourceCls.addProperty(Edm.spatialResolution, ValueType.Literal);
		webResourceCls.addProperty(Edm.hasColorSpace, ValueType.Literal);
		webResourceCls.addProperty(Edm.componentColor, ValueType.Literal);
		webResourceCls.addProperty(Edm.preview, ValueType.ResourceOrUri).addRangeClass(Edm.WebResource);
		webResourceCls.addProperty(Ebucore.hasMimeType, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.fileSize, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.duration, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.width, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.height, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.sampleSize, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.bitRate, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.frameRate, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.orientation, ValueType.Literal);
		webResourceCls.addProperty(Ebucore.audioChannelNumber, ValueType.Literal);
		webResourceCls.addProperty(Svcs.has_service, ValueType.Uri);
		
		providedChoCls.addProperty(Edm.currentLocation, ValueType.ResourceOrLiteral).addRangeClass(Edm.Place);
		providedChoCls.addProperty(Edm.hasMet, ValueType.ResourceOrUri).addRangeClass(Edm.Place, Edm.Agent, Skos.Concept);
		providedChoCls.addProperty(Edm.hasType, ValueType.ResourceOrLiteral);
		providedChoCls.addProperty(Edm.incorporates, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.isDerivativeOf, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.isNextInSequence, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.isRelatedTo, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.isRepresentationOf, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.isSuccessorOf, ValueType.ResourceOrUri);
		providedChoCls.addProperty(Edm.realizes, ValueType.ResourceOrUri);
		providedChoCls.addProperty(contributorProp);
		providedChoCls.addProperty(creatorProp);
		providedChoCls.addProperty(dateProp);
		providedChoCls.addProperty(descriptionProp);
		providedChoCls.addProperty(formatProp);
		providedChoCls.addProperty(identifierProp);
		providedChoCls.addProperty(languageProp);
		providedChoCls.addProperty(publisherProp);
		providedChoCls.addProperty(relationProp);
		providedChoCls.addProperty(rightsProp);
		providedChoCls.addProperty(sourceProp);
		providedChoCls.addProperty(subjectProp);
		providedChoCls.addProperty(titleProp);
		providedChoCls.addProperty(typeProp);
		providedChoCls.addProperty(alternativeProp);
		providedChoCls.addProperty(conformsToProp);
		providedChoCls.addProperty(createdProp);
		providedChoCls.addProperty(extentProp);
		providedChoCls.addProperty(hasFormatProp);
		providedChoCls.addProperty(hasPartProp);
		providedChoCls.addProperty(hasVersionProp);
		providedChoCls.addProperty(isFormatOfProp);
		providedChoCls.addProperty(isPartOfProp);
		providedChoCls.addProperty(isReferencedByProp);
		providedChoCls.addProperty(isReplacedByProp);
		providedChoCls.addProperty(isRequiredByProp);
		providedChoCls.addProperty(issuedProp);
		providedChoCls.addProperty(isVersionOfProp);
		providedChoCls.addProperty(mediumProp);
		providedChoCls.addProperty(provenanceProp);
		providedChoCls.addProperty(referencesProp);
		providedChoCls.addProperty(replacesProp);
		providedChoCls.addProperty(requiresProp);
		providedChoCls.addProperty(spatialProp);
		providedChoCls.addProperty(tableOfContentsProp);
		providedChoCls.addProperty(temporalProp);
		
		aggregationCls.addProperty(Edm.aggregatedCHO, ValueType.Resource).addRangeClass(Edm.ProvidedCHO);
		aggregationCls.addProperty(Edm.dataProvider, ValueType.ResourceOrLiteral).addRangeClass(Edm.Agent);
		aggregationCls.addProperty(Edm.provider, ValueType.ResourceOrLiteral).addRangeClass(Edm.Agent);
		aggregationCls.addProperty(Edm.intermediateProvider, ValueType.ResourceOrLiteral).addRangeClass(Edm.Agent);
		aggregationCls.addProperty(Edm.hasView, ValueType.ResourceOrUri).addRangeClass(Edm.WebResource);
		aggregationCls.addProperty(Edm.isShownAt, ValueType.ResourceOrUri).addRangeClass(Edm.WebResource);
		aggregationCls.addProperty(Edm.isShownBy, ValueType.ResourceOrUri).addRangeClass(Edm.WebResource);
		aggregationCls.addProperty(Edm.object, ValueType.ResourceOrUri).addRangeClass(Edm.WebResource);
		aggregationCls.addProperty(Edm.rights, ValueType.ResourceOrUri).addRangeClass(RdfReg.CC_LICENSE);
		aggregationCls.addProperty(rightsProp);
		
		return alig;
	}

	private static void addSchemaorgEquivalentClasses(Alignment alig) {
		alig.getClassAlignment(Edm.ProvidedCHO).addAlignedWith("http://schema.org/CreativeWork",
				"http://schema.org/MusicComposition", "http://schema.org/FAQPage", "http://schema.org/MedicalScholarlyArticle", 
						"http://schema.org/TechArticle", "http://schema.org/WebApplication", "http://schema.org/ContactPage", 
						"http://schema.org/Dataset", "http://schema.org/Recipe", "http://schema.org/Manuscript", 
						"http://schema.org/RadioSeries", "http://schema.org/Newspaper", "http://schema.org/VideoGame", 
						"http://schema.org/WPFooter", "http://schema.org/Article", "http://schema.org/Conversation", 
						"http://schema.org/TextDigitalDocument", "http://schema.org/LiveBlogPosting", "http://schema.org/DataCatalog", 
						"http://schema.org/BackgroundNewsArticle", "http://schema.org/Question", "http://schema.org/MusicPlaylist", 
						"http://schema.org/MenuSection", "http://schema.org/Atlas", "http://schema.org/Sculpture", 
						"http://schema.org/BookSeries", "http://schema.org/BlogPosting", "http://schema.org/RadioClip", 
						"http://schema.org/ComicIssue", "http://schema.org/Blog", "http://schema.org/ReportageNewsArticle", 
						"http://schema.org/MusicRelease", "http://schema.org/NoteDigitalDocument", "http://schema.org/VideoObject", 
						"http://schema.org/ItemPage", "http://schema.org/ComicCoverArt", "http://schema.org/ProfilePage", 
						"http://schema.org/SiteNavigationElement", "http://schema.org/ComicSeries", "http://schema.org/SocialMediaPosting", 
						"http://schema.org/MusicRecording", "http://schema.org/HowToTip", "http://schema.org/AdvertiserContentArticle", 
						"http://schema.org/Answer", "http://schema.org/Comment", "http://schema.org/Thesis", 
						"http://schema.org/PresentationDigitalDocument", "http://schema.org/Clip", "http://schema.org/APIReference", 
						"http://schema.org/SheetMusic", "http://schema.org/ArchiveComponent", "http://schema.org/Legislation", 
						"http://schema.org/AudioObject", "http://schema.org/Map", "http://schema.org/TVClip", 
						"http://schema.org/WPHeader", "http://schema.org/DiscussionForumPosting", "http://schema.org/HowToDirection", 
						"http://schema.org/Review", "http://schema.org/Periodical", "http://schema.org/Audiobook", 
						"http://schema.org/MediaObject", "http://schema.org/EmailMessage", "http://schema.org/ImageObject", 
						"http://schema.org/CreativeWorkSeries", "http://schema.org/VideoGameClip", "http://schema.org/SoftwareApplication", 
						"http://schema.org/Season", "http://schema.org/Message", "http://schema.org/WPSideBar", 
						"http://schema.org/Table", "http://schema.org/ClaimReview", "http://schema.org/AboutPage", 
						"http://schema.org/Drawing", "http://schema.org/PublicationIssue", "http://schema.org/LegislationObject", 
						"http://schema.org/TVSeason", "http://schema.org/MusicAlbum", "http://schema.org/Menu", 
						"http://schema.org/MovieSeries", "http://schema.org/AskPublicNewsArticle", "http://schema.org/DigitalDocument", 
						"http://schema.org/Movie", "http://schema.org/ImageGallery", "http://schema.org/Poster", 
						"http://schema.org/NewsArticle", "http://schema.org/VideoGameSeries", "http://schema.org/DataFeed", 
						"http://schema.org/DataDownload", "http://schema.org/ReviewNewsArticle", "http://schema.org/ExercisePlan", 
						"http://schema.org/WebPage", "http://schema.org/Episode", "http://schema.org/MusicVideoObject", 
						"http://schema.org/Quotation", "http://schema.org/VisualArtwork", "http://schema.org/EmployerReview", 
						"http://schema.org/Play", "http://schema.org/SatiricalArticle", "http://schema.org/CategoryCodeSet", 
						"http://schema.org/TVEpisode", "http://schema.org/MobileApplication", "http://schema.org/Collection", 
						"http://schema.org/Painting", "http://schema.org/AnalysisNewsArticle", "http://schema.org/Report", 
						"http://schema.org/CriticReview", "http://schema.org/HowToSection", "http://schema.org/CorrectionComment", 
						"http://schema.org/Chapter", "http://schema.org/3DModel", "http://schema.org/OpinionNewsArticle", 
						"http://schema.org/ShortStory", "http://schema.org/VideoGallery", "http://schema.org/CheckoutPage", 
						"http://schema.org/UserReview", "http://schema.org/CollectionPage", "http://schema.org/SearchResultsPage", 
						"http://schema.org/WebSite", "http://schema.org/WebPageElement", "http://schema.org/RadioEpisode", 
						"http://schema.org/Course", "http://schema.org/ComicStory", "http://schema.org/ScholarlyArticle", 
						"http://schema.org/WPAdBlock", "http://schema.org/SpreadsheetDigitalDocument", "http://schema.org/Code", 
						"http://schema.org/MedicalWebPage", "http://schema.org/Game", "http://schema.org/SoftwareSourceCode", 
						"http://schema.org/Diet", "http://schema.org/QAPage", "http://schema.org/CompleteDataFeed", 
						"http://schema.org/HowToStep", "http://schema.org/HowTo", "http://schema.org/Barcode", 
						"http://schema.org/DefinedTermSet", "http://schema.org/TVSeries", "http://schema.org/Claim", 
						"http://schema.org/Photograph", "http://schema.org/MovieClip", "http://schema.org/RadioSeason", 
						"http://schema.org/CreativeWorkSeason", "http://schema.org/PublicationVolume", "http://schema.org/EducationalOccupationalCredential", 
						"http://schema.org/Book", "http://schema.org/CoverArt");

		
		alig.getClassAlignment(Edm.Agent).addAlignedWith("http://schema.org/Person", "http://schema.org/Patient", 
							"http://schema.org/Organization",
							"http://schema.org/CollegeOrUniversity", "http://schema.org/Restaurant", "http://schema.org/DaySpa", 
							"http://schema.org/BarOrPub", "http://schema.org/LiquorStore", "http://schema.org/JewelryStore", 
							"http://schema.org/MusicStore", "http://schema.org/MovingCompany", "http://schema.org/BankOrCreditUnion", 
							"http://schema.org/SelfStorage", "http://schema.org/RealEstateAgent", "http://schema.org/ElectronicsStore", 
							"http://schema.org/AutoDealer", "http://schema.org/EmergencyService", "http://schema.org/NewsMediaOrganization", 
							"http://schema.org/GroceryStore", "http://schema.org/FireStation", "http://schema.org/MedicalClinic", 
							"http://schema.org/SkiResort", "http://schema.org/Bakery", "http://schema.org/HealthAndBeautyBusiness", 
							"http://schema.org/Dentist", "http://schema.org/Project", "http://schema.org/RoofingContractor", 
							"http://schema.org/MovieTheater", "http://schema.org/NailSalon", "http://schema.org/AutoBodyShop", 
							"http://schema.org/ConvenienceStore", "http://schema.org/HousePainter", "http://schema.org/PerformingGroup", 
							"http://schema.org/HomeAndConstructionBusiness", "http://schema.org/WorkersUnion", "http://schema.org/ProfessionalService", 
							"http://schema.org/SportingGoodsStore", "http://schema.org/AutoRental", "http://schema.org/ComputerStore", 
							"http://schema.org/HardwareStore", "http://schema.org/Distillery", "http://schema.org/Physician", 
							"http://schema.org/TravelAgency", "http://schema.org/EducationalOrganization", "http://schema.org/FinancialService", 
							"http://schema.org/LibrarySystem", "http://schema.org/TennisComplex", "http://schema.org/ExerciseGym", 
							"http://schema.org/HealthClub", "http://schema.org/PoliceStation", "http://schema.org/Attorney", 
							"http://schema.org/Preschool", "http://schema.org/StadiumOrArena", "http://schema.org/GardenStore", 
							"http://schema.org/Plumber", "http://schema.org/ShoppingCenter", "http://schema.org/IceCreamShop", 
							"http://schema.org/BeautySalon", "http://schema.org/FoodEstablishment", "http://schema.org/ResearchProject", 
							"http://schema.org/Resort", "http://schema.org/BedAndBreakfast", "http://schema.org/EmploymentAgency", 
							"http://schema.org/HVACBusiness", "http://schema.org/Airline", "http://schema.org/Motel", 
							"http://schema.org/Electrician", "http://schema.org/SportsOrganization", "http://schema.org/AccountingService", 
							"http://schema.org/MedicalBusiness", "http://schema.org/Pharmacy", "http://schema.org/OutletStore", 
							"http://schema.org/ComedyClub", "http://schema.org/Florist", "http://schema.org/ShoeStore", 
							"http://schema.org/MiddleSchool", "http://schema.org/SportsTeam", "http://schema.org/PawnShop", 
							"http://schema.org/Notary", "http://schema.org/FurnitureStore", "http://schema.org/Store", 
							"http://schema.org/AutoWash", "http://schema.org/InternetCafe", "http://schema.org/Corporation", 
							"http://schema.org/HighSchool", "http://schema.org/SportsActivityLocation", "http://schema.org/AutomatedTeller", 
							"http://schema.org/ClothingStore", "http://schema.org/GovernmentOrganization", "http://schema.org/GasStation", 
							"http://schema.org/HomeGoodsStore", "http://schema.org/GovernmentOffice", "http://schema.org/EntertainmentBusiness", 
							"http://schema.org/LodgingBusiness", "http://schema.org/DryCleaningOrLaundry", "http://schema.org/GolfCourse", 
							"http://schema.org/GeneralContractor", "http://schema.org/Hotel", "http://schema.org/Optician", 
							"http://schema.org/MobilePhoneStore", "http://schema.org/DepartmentStore", "http://schema.org/Consortium", 
							"http://schema.org/TheaterGroup", "http://schema.org/AnimalShelter", "http://schema.org/AutoRepair", 
							"http://schema.org/PublicSwimmingPool", "http://schema.org/HobbyShop", "http://schema.org/BowlingAlley", 
							"http://schema.org/MedicalOrganization", "http://schema.org/ArtGallery", "http://schema.org/OfficeEquipmentStore", 
							"http://schema.org/MotorcycleRepair", "http://schema.org/AdultEntertainment", "http://schema.org/LocalBusiness", 
							"http://schema.org/ChildCare", "http://schema.org/SportsClub", "http://schema.org/WholesaleStore", 
							"http://schema.org/TattooParlor", "http://schema.org/ElementarySchool", "http://schema.org/MensClothingStore", 
							"http://schema.org/Casino", "http://schema.org/MotorcycleDealer", "http://schema.org/RecyclingCenter", 
							"http://schema.org/BookStore", "http://schema.org/PostOffice", "http://schema.org/DiagnosticLab", 
							"http://schema.org/ToyStore", "http://schema.org/Brewery", "http://schema.org/FastFoodRestaurant", 
							"http://schema.org/PetStore", "http://schema.org/Hostel", "http://schema.org/Winery", 
							"http://schema.org/ArchiveOrganization", "http://schema.org/Library", "http://schema.org/DanceGroup", 
							"http://schema.org/AutomotiveBusiness", "http://schema.org/NGO", "http://schema.org/FundingScheme", 
							"http://schema.org/VeterinaryCare", "http://schema.org/Campground", "http://schema.org/AutoPartsStore", 
							"http://schema.org/BikeStore", "http://schema.org/Locksmith", "http://schema.org/NightClub", 
							"http://schema.org/School", "http://schema.org/TouristInformationCenter", "http://schema.org/RadioStation", 
							"http://schema.org/MovieRentalStore", "http://schema.org/Hospital", "http://schema.org/LegalService", 
							"http://schema.org/AmusementPark", "http://schema.org/HairSalon", "http://schema.org/MusicGroup", 
							"http://schema.org/CafeOrCoffeeShop", "http://schema.org/TelevisionStation", "http://schema.org/FundingAgency", 
							"http://schema.org/InsuranceAgency", "http://schema.org/TireShop");
		alig.getClassAlignment(Edm.WebResource).addAlignedWith("http://schema.org/WebPage",
							"http://schema.org/SearchResultsPage", "http://schema.org/AboutPage", "http://schema.org/FAQPage", 
							"http://schema.org/MedicalWebPage", "http://schema.org/ItemPage", "http://schema.org/ProfilePage", 
							"http://schema.org/QAPage", "http://schema.org/VideoGallery", "http://schema.org/CheckoutPage", 
							"http://schema.org/ImageGallery", "http://schema.org/ContactPage", "http://schema.org/CollectionPage", 
							"http://schema.org/MediaObject",
							"http://schema.org/Audiobook", "http://schema.org/ImageObject", "http://schema.org/DataDownload", 
							"http://schema.org/VideoObject", "http://schema.org/LegislationObject", "http://schema.org/AudioObject", 
							"http://schema.org/MusicVideoObject", "http://schema.org/Barcode"); 

		
		alig.getClassAlignment(Edm.Place).addAlignedWith("http://schema.org/Place",
							"http://schema.org/MeetingRoom", "http://schema.org/DaySpa", "http://schema.org/BarOrPub", 
							"http://schema.org/JewelryStore", "http://schema.org/SelfStorage", "http://schema.org/AutoDealer", 
							"http://schema.org/GroceryStore", "http://schema.org/FireStation", "http://schema.org/SkiResort", 
							"http://schema.org/Dentist", "http://schema.org/Country", "http://schema.org/NailSalon", 
							"http://schema.org/BusStop", "http://schema.org/AutoBodyShop", "http://schema.org/PlaceOfWorship", 
							"http://schema.org/HousePainter", "http://schema.org/State", "http://schema.org/Courthouse", 
							"http://schema.org/ProfessionalService", "http://schema.org/SubwayStation", "http://schema.org/ComputerStore", 
							"http://schema.org/Distillery", "http://schema.org/Physician", "http://schema.org/TennisComplex", 
							"http://schema.org/ExerciseGym", "http://schema.org/CampingPitch", "http://schema.org/Canal", 
							"http://schema.org/Residence", "http://schema.org/CityHall", "http://schema.org/Landform", 
							"http://schema.org/GardenStore", "http://schema.org/Plumber", "http://schema.org/Apartment", 
							"http://schema.org/Suite", "http://schema.org/FoodEstablishment", "http://schema.org/CatholicChurch", 
							"http://schema.org/Resort", "http://schema.org/EmploymentAgency", "http://schema.org/City", 
							"http://schema.org/MedicalBusiness", "http://schema.org/OutletStore", "http://schema.org/ComedyClub", 
							"http://schema.org/Florist", "http://schema.org/EventVenue", "http://schema.org/SeaBodyOfWater", 
							"http://schema.org/Notary", "http://schema.org/FurnitureStore", "http://schema.org/Store", 
							"http://schema.org/BuddhistTemple", "http://schema.org/AutoWash", "http://schema.org/SportsActivityLocation", 
							"http://schema.org/Playground", "http://schema.org/HomeGoodsStore", "http://schema.org/CivicStructure", 
							"http://schema.org/EntertainmentBusiness", "http://schema.org/Mosque", "http://schema.org/Airport", 
							"http://schema.org/TouristDestination", "http://schema.org/GatedResidenceCommunity", "http://schema.org/AutoRepair", 
							"http://schema.org/HobbyShop", "http://schema.org/RiverBodyOfWater", "http://schema.org/MusicVenue", 
							"http://schema.org/Church", "http://schema.org/OfficeEquipmentStore", "http://schema.org/AdultEntertainment", 
							"http://schema.org/LocalBusiness", "http://schema.org/ChildCare", "http://schema.org/WholesaleStore", 
							"http://schema.org/TattooParlor", "http://schema.org/Casino", "http://schema.org/MotorcycleDealer", 
							"http://schema.org/RecyclingCenter", "http://schema.org/PostOffice", "http://schema.org/SingleFamilyResidence", 
							"http://schema.org/Brewery", "http://schema.org/Volcano", "http://schema.org/FastFoodRestaurant", 
							"http://schema.org/Hostel", "http://schema.org/ParkingFacility", "http://schema.org/Zoo", 
							"http://schema.org/HinduTemple", "http://schema.org/AutomotiveBusiness", "http://schema.org/TouristAttraction", 
							"http://schema.org/BikeStore", "http://schema.org/Park", "http://schema.org/NightClub", 
							"http://schema.org/TouristInformationCenter", "http://schema.org/RadioStation", "http://schema.org/Hospital", 
							"http://schema.org/Beach", "http://schema.org/Pond", "http://schema.org/AmusementPark", 
							"http://schema.org/GovernmentBuilding", "http://schema.org/Accommodation", "http://schema.org/TelevisionStation", 
							"http://schema.org/InsuranceAgency", "http://schema.org/Waterfall", "http://schema.org/Restaurant", 
							"http://schema.org/LiquorStore", "http://schema.org/MusicStore", "http://schema.org/MovingCompany", 
							"http://schema.org/BankOrCreditUnion", "http://schema.org/RealEstateAgent", "http://schema.org/ElectronicsStore", 
							"http://schema.org/Continent", "http://schema.org/EmergencyService", "http://schema.org/Cemetery", 
							"http://schema.org/BusStation", "http://schema.org/House", "http://schema.org/PublicToilet", 
							"http://schema.org/MedicalClinic", "http://schema.org/Bakery", "http://schema.org/HealthAndBeautyBusiness", 
							"http://schema.org/RoofingContractor", "http://schema.org/MovieTheater", "http://schema.org/ConvenienceStore", 
							"http://schema.org/Reservoir", "http://schema.org/Embassy", "http://schema.org/HomeAndConstructionBusiness", 
							"http://schema.org/SportingGoodsStore", "http://schema.org/AutoRental", "http://schema.org/HardwareStore", 
							"http://schema.org/TravelAgency", "http://schema.org/FinancialService", "http://schema.org/AdministrativeArea", 
							"http://schema.org/HealthClub", "http://schema.org/PoliceStation", "http://schema.org/Attorney", 
							"http://schema.org/HotelRoom", "http://schema.org/StadiumOrArena", "http://schema.org/ShoppingCenter", 
							"http://schema.org/IceCreamShop", "http://schema.org/BeautySalon", "http://schema.org/BedAndBreakfast", 
							"http://schema.org/HVACBusiness", "http://schema.org/Motel", "http://schema.org/Electrician", 
							"http://schema.org/AccountingService", "http://schema.org/Pharmacy", "http://schema.org/ShoeStore", 
							"http://schema.org/PawnShop", "http://schema.org/Museum", "http://schema.org/Synagogue", 
							"http://schema.org/BodyOfWater", "http://schema.org/InternetCafe", "http://schema.org/AutomatedTeller", 
							"http://schema.org/ClothingStore", "http://schema.org/LakeBodyOfWater", "http://schema.org/GasStation", 
							"http://schema.org/RVPark", "http://schema.org/LandmarksOrHistoricalBuildings", "http://schema.org/GovernmentOffice", 
							"http://schema.org/LodgingBusiness", "http://schema.org/DryCleaningOrLaundry", "http://schema.org/DefenceEstablishment", 
							"http://schema.org/GolfCourse", "http://schema.org/GeneralContractor", "http://schema.org/Hotel", 
							"http://schema.org/Optician", "http://schema.org/MobilePhoneStore", "http://schema.org/DepartmentStore", 
							"http://schema.org/OceanBodyOfWater", "http://schema.org/AnimalShelter", "http://schema.org/PerformingArtsTheater", 
							"http://schema.org/PublicSwimmingPool", "http://schema.org/Crematorium", "http://schema.org/BowlingAlley", 
							"http://schema.org/TaxiStand", "http://schema.org/ArtGallery", "http://schema.org/MotorcycleRepair", 
							"http://schema.org/Aquarium", "http://schema.org/SportsClub", "http://schema.org/TrainStation", 
							"http://schema.org/MensClothingStore", "http://schema.org/BookStore", "http://schema.org/ToyStore", 
							"http://schema.org/PetStore", "http://schema.org/Winery", "http://schema.org/ArchiveOrganization", 
							"http://schema.org/Library", "http://schema.org/Campground", "http://schema.org/LegislativeBuilding", 
							"http://schema.org/AutoPartsStore", "http://schema.org/Bridge", "http://schema.org/ApartmentComplex", 
							"http://schema.org/Locksmith", "http://schema.org/MovieRentalStore", "http://schema.org/LegalService", 
							"http://schema.org/Room", "http://schema.org/HairSalon", "http://schema.org/Mountain", 
							"http://schema.org/CafeOrCoffeeShop", "http://schema.org/TireShop");
	}

	
	private static void addSchemaorgEquivalentProperties(Alignment alig) {
		ClassAlignment agentAlig = alig.getClassAlignment(Edm.Agent);
		agentAlig.getPropertyAlignment(Skos.prefLabel).addAlignment(Schemaorg.name);
		agentAlig.getPropertyAlignment(Skos.altLabel).addAlignment(Schemaorg.alternateName);
		agentAlig.getPropertyAlignment(Skos.note).addAlignment(Schemaorg.description);
		agentAlig.getPropertyAlignment(Owl.sameAs).addAlignment(Schemaorg.sameAs);
		agentAlig.getPropertyAlignment(RdaGr2.professionOrOccupation).addAlignment(RdfReg.Schemaorg_JOB_TITLE);
		agentAlig.getPropertyAlignment(RdaGr2.dateOfBirth).addAlignment(Schemaorg.birthDate);
		agentAlig.getPropertyAlignment(RdaGr2.dateOfDeath).addAlignment(Schemaorg.deathDate);
		//TODO: needs revision
		agentAlig.getPropertyAlignment(Skos.prefLabel).addAlignment(Schemaorg.givenName);
		agentAlig.getPropertyAlignment(Skos.prefLabel).addAlignment(Schemaorg.familyName);

		ClassAlignment placeAlig = alig.getClassAlignment(Edm.Place);
		placeAlig.getPropertyAlignment(Skos.prefLabel).addAlignment(Schemaorg.name);
		placeAlig.getPropertyAlignment(Skos.altLabel).addAlignment(Schemaorg.alternateName);
		placeAlig.getPropertyAlignment(Skos.note).addAlignment(Schemaorg.description);
		placeAlig.getPropertyAlignment(Owl.sameAs).addAlignment(Schemaorg.sameAs);
		placeAlig.getPropertyAlignment(Wgs84.lat).addAlignment(Schemaorg.latitude);
		placeAlig.getPropertyAlignment(Wgs84.long_).addAlignment(Schemaorg.longitude);

		ClassAlignment conceptAlig = alig.getClassAlignment(Skos.Concept);
		conceptAlig.getPropertyAlignment(Skos.prefLabel).addAlignment(Schemaorg.name);
		conceptAlig.getPropertyAlignment(Skos.altLabel).addAlignment(Schemaorg.alternateName);
		conceptAlig.getPropertyAlignment(Skos.note).addAlignment(Schemaorg.description);
		conceptAlig.getPropertyAlignment(Owl.sameAs).addAlignment(Schemaorg.sameAs);
		
		ClassAlignment webResAlig = alig.getClassAlignment(Edm.WebResource);
		webResAlig.getPropertyAlignment(Dc.description).addAlignment(Schemaorg.description);
		webResAlig.getPropertyAlignment(Dc.description).addAlignment(Schemaorg.name);
		webResAlig.getPropertyAlignment(Dc.format).addAlignment(Schemaorg.encodingFormat);
		webResAlig.getPropertyAlignment(Dc.format).addAlignment(Schemaorg.fileFormat);
		webResAlig.getPropertyAlignment(DcTerms.extent).addAlignment(Schemaorg.height);
		webResAlig.getPropertyAlignment(DcTerms.extent).addAlignment(Schemaorg.width);
		
		ClassAlignment choAlig = alig.getClassAlignment(Edm.ProvidedCHO);				
		choAlig.getPropertyAlignment(Dc.subject).addAlignment(Schemaorg.about);
		choAlig.getPropertyAlignment(Dc.subject).addAlignment(Schemaorg.keywords);
		choAlig.getPropertyAlignment(Dc.title).addAlignment(Schemaorg.name);
		choAlig.getPropertyAlignment(Dc.language).addAlignment(Schemaorg.inLanguage);
		choAlig.getPropertyAlignment(Dc.description).addAlignment(Schemaorg.description);
		choAlig.getPropertyAlignment(Dc.subject).addAlignment(Schemaorg.contentLocation);
		choAlig.getPropertyAlignment(Dc.creator).addAlignment(Schemaorg.creator);
		choAlig.getPropertyAlignment(Dc.creator).addAlignment(Schemaorg.author);
		choAlig.getPropertyAlignment(Dc.contributor).addAlignment(Schemaorg.contributor);
		choAlig.getPropertyAlignment(Dc.contributor).addAlignment(Schemaorg.editor);
		choAlig.getPropertyAlignment(Dc.contributor).addAlignment(Schemaorg.illustrator);
		choAlig.getPropertyAlignment(Dc.contributor).addAlignment(Schemaorg.director);
		choAlig.getPropertyAlignment(Dc.rights).addAlignment(Schemaorg.copyrightHolder);
		choAlig.getPropertyAlignment(Dc.rights).addAlignment(Schemaorg.license);
		choAlig.getPropertyAlignment(Dc.type).addAlignment(Schemaorg.genre);
		choAlig.getPropertyAlignment(Dc.type).addAlignment(Schemaorg.artform);
		choAlig.getPropertyAlignment(Dc.description).addAlignment(Schemaorg.artMedium);
		choAlig.getPropertyAlignment(Dc.description).addAlignment(Schemaorg.pagination);
		choAlig.getPropertyAlignment(Dc.identifier).addAlignment(Schemaorg.identifier);
		choAlig.getPropertyAlignment(DcTerms.isPartOf).addAlignment(Schemaorg.isPartOf);
		choAlig.getPropertyAlignment(DcTerms.hasPart).addAlignment(Schemaorg.hasPart);
		choAlig.getPropertyAlignment(DcTerms.created).addAlignment(Schemaorg.dateCreated);
		choAlig.getPropertyAlignment(DcTerms.issued).addAlignment(Schemaorg.datePublished);
		choAlig.getPropertyAlignment(DcTerms.publisher).addAlignment(Schemaorg.publisher);
		choAlig.getPropertyAlignment(DcTerms.medium).addAlignment(Schemaorg.material);
		choAlig.getPropertyAlignment(DcTerms.medium).addAlignment(Schemaorg.artworkSurface);
		choAlig.getPropertyAlignment(DcTerms.spatial).addAlignment(Schemaorg.locationCreated);
		choAlig.getPropertyAlignment(DcTerms.extent).addAlignment(Schemaorg.height);
		choAlig.getPropertyAlignment(DcTerms.extent).addAlignment(Schemaorg.width);
		choAlig.getPropertyAlignment(DcTerms.extent).addAlignment(Schemaorg.numberOfPages);
		choAlig.getPropertyAlignment(DcTerms.spatial).addAlignment(Schemaorg.spatial);
		choAlig.getPropertyAlignment(DcTerms.temporal).addAlignment(Schemaorg.temporalCoverage);
		choAlig.getPropertyAlignment(Owl.sameAs).addAlignment(Schemaorg.sameAs);
		choAlig.getPropertyAlignment(Edm.realizes).addAlignment(Schemaorg.exampleOfWork);
		choAlig.getPropertyAlignment(Edm.currentLocation).addAlignment(Schemaorg.spatialCoverage);
		choAlig.getPropertyAlignment(Edm.hasType).addAlignment(Schemaorg.additionalType);
		
		ClassAlignment aggAlig = alig.getClassAlignment(Edm.ProvidedCHO);				
		aggAlig.getPropertyAlignment(Edm.provider).addAlignment(Schemaorg.provider);
		aggAlig.getPropertyAlignment(Edm.isShownBy).addAlignment(Schemaorg.associatedMedia);
		aggAlig.getPropertyAlignment(Edm.isShownBy).addAlignment(Schemaorg.audio);
		aggAlig.getPropertyAlignment(Edm.isShownBy).addAlignment(Schemaorg.image);
		aggAlig.getPropertyAlignment(Edm.isShownAt).addAlignment(Schemaorg.url);
		aggAlig.getPropertyAlignment(Edm.object).addAlignment(Schemaorg.thumbnailUrl);
	}
	
	private static void addWikidataEquivalentClasses(Alignment alig) {
		alig.getClassAlignment(Edm.Agent).addAlignedWith("http://www.wikidata.org/entity/Q127843",
				"http://www.wikidata.org/entity/Q23671509", "http://www.wikidata.org/entity/Q27038992",
				"http://www.wikidata.org/entity/Q43229", "http://www.wikidata.org/entity/Q1136342",
				"http://www.wikidata.org/entity/Q27008444", "http://www.wikidata.org/entity/Q27003353",
				"http://www.wikidata.org/entity/Q27106436", "http://www.wikidata.org/entity/Q3661311",
				"http://www.wikidata.org/entity/Q16735601", "http://www.wikidata.org/entity/Q5633421",
				"http://www.wikidata.org/entity/Q1494322", "http://www.wikidata.org/entity/Q5",
				"http://www.wikidata.org/entity/Q1030034", "http://www.wikidata.org/entity/Q207694",
				"http://www.wikidata.org/entity/Q81235", "http://www.wikidata.org/entity/Q1411287",
				"http://www.wikidata.org/entity/Q27057193", "http://www.wikidata.org/entity/Q1618899",
				"http://www.wikidata.org/entity/Q215627");
		alig.getClassAlignment(Edm.Place).addAlignedWith("http://www.wikidata.org/entity/Q618123",
				"http://www.wikidata.org/entity/Q17334923", "http://www.wikidata.org/entity/Q570116",
				"http://www.wikidata.org/entity/Q1395196", "http://www.wikidata.org/entity/Q699405");
		alig.getClassAlignment(Edm.TimeSpan).addAlignedWith("http://www.wikidata.org/entity/Q1190554");
		alig.getClassAlignment(Skos.Concept).addAlignedWith("http://www.wikidata.org/entity/Q11238641",
				"http://www.wikidata.org/entity/Q1912682", "http://www.wikidata.org/entity/Q5227292",
				"http://www.wikidata.org/entity/Q20937557", "http://www.wikidata.org/entity/Q9644",
				"http://www.wikidata.org/entity/Q2668072", "http://www.wikidata.org/entity/Q151885",
				"http://www.wikidata.org/entity/Q4402708", "http://www.wikidata.org/entity/Q2424752",
				"http://www.wikidata.org/entity/Q1172284", "http://www.wikidata.org/entity/Q27166169",
				"http://www.wikidata.org/entity/Q42889", "http://www.wikidata.org/entity/Q39888",
				"http://www.wikidata.org/entity/Q6554101", "http://www.wikidata.org/entity/Q7406919",
				"http://www.wikidata.org/entity/Q4897819", "http://www.wikidata.org/entity/Q35120",
				"http://www.wikidata.org/entity/Q628523", "http://www.wikidata.org/entity/Q12140",
				"http://www.wikidata.org/entity/Q658274", "http://www.wikidata.org/entity/Q265868",
				"http://www.wikidata.org/entity/Q1983062", "http://www.wikidata.org/entity/Q70990126",
				"http://www.wikidata.org/entity/Q7365", "http://www.wikidata.org/entity/Q9620",
				"http://www.wikidata.org/entity/Q41825", "http://www.wikidata.org/entity/Q629206",
				"http://www.wikidata.org/entity/Q2006410", "http://www.wikidata.org/entity/Q11423",
				"http://www.wikidata.org/entity/Q9609", "http://www.wikidata.org/entity/Q614112",
				"http://www.wikidata.org/entity/Q4936952", "http://www.wikidata.org/entity/Q211198",
				"http://www.wikidata.org/entity/Q30322502", "http://www.wikidata.org/entity/Q27165776",
				"http://www.wikidata.org/entity/Q382386", "http://www.wikidata.org/entity/Q27166344",
				"http://www.wikidata.org/entity/Q34770", "http://www.wikidata.org/entity/Q1141067",
				"http://www.wikidata.org/entity/Q3545415", "http://www.wikidata.org/entity/Q27166192",
				"http://www.wikidata.org/entity/Q9655", "http://www.wikidata.org/entity/Q10929058");
		alig.getClassAlignment(Skos.Concept).addAlignedWith("http://www.wikidata.org/entity/Q3331189",
				"http://www.wikidata.org/entity/Q41298", "http://www.wikidata.org/entity/Q3305213",
				"http://www.wikidata.org/entity/Q737498", "http://www.wikidata.org/entity/Q191067",
				"http://www.wikidata.org/entity/Q17537576", "http://www.wikidata.org/entity/Q11032");
		//http://www.europeana.eu/schemas/edm/WebResource
		//http://www.openarchives.org/ore/terms/Aggregation


	}
}
